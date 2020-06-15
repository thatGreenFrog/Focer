package lv.greenfrog.crawler;

import com.digitalpebble.stormcrawler.Metadata;
import com.digitalpebble.stormcrawler.bolt.StatusEmitterBolt;
import com.digitalpebble.stormcrawler.util.CharsetIdentification;
import com.digitalpebble.stormcrawler.util.ConfUtils;
import lv.greenfrog.crawler.persistence.LinksMapper;
import lv.greenfrog.crawler.persistence.entity.Links;
import org.apache.ibatis.session.SqlSession;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.stream.Collectors;

import static com.digitalpebble.stormcrawler.Constants.StatusStreamName;

public class LinkParserBolt extends StatusEmitterBolt {

    private static final Logger log = LoggerFactory.getLogger(LinkParserBolt.class);
    private MessageDigest md;
    private String resourceFolder;
    private String blacklist;

    @Override
    public void execute(Tuple input) {
        String url = input.getStringByField("url");
        String pageClass = input.getStringByField("class");
        Metadata metadata = (Metadata) input.getValueByField("metadata");
        byte[] content = input.getBinaryByField("content");
        String text = input.getStringByField("text");

        String charset = CharsetIdentification.getCharset(metadata, content, -1);
        String html = Charset.forName(charset).decode(ByteBuffer.wrap(content)).toString();
        Document jDoc = Parser.htmlParser().parseInput(html, url);
        Elements anchors = jDoc.select("a[href]");
        saveLinks(anchors, pageClass);

        collector.emit(input, new Values(url, content, metadata, text, pageClass));
        collector.ack(input);
    }



    private void saveLinks(Elements anchor, String pageClass) {
        SqlSession session = SessionManager.getSession(resourceFolder);
        final int score = pageClass.equals("Negative") ? -1 : 1;
        anchor.stream()
                .filter(a -> !"nofollow".equalsIgnoreCase(a.attr("rel")) && a.attr("abs:href") != null && !a.attr("abs:href").isEmpty())
                .map(a -> a.attr("abs:href"))
                .forEach(url -> {
                    try {
                        URI uri = new URI(url);

                        if(uri.getHost() == null || uri.getHost().matches(blacklist)) throw new URISyntaxException("", ""); //e-mail and other crap
                        String domain = uri.getHost().startsWith("www.") ? uri.getHost().substring(4) : uri.getHost();

                        byte[] linkHash = md.digest(url.getBytes());
                        Links newLink = session.getMapper(LinksMapper.class).getByHash(linkHash);
                        if (newLink == null) {
                            newLink = new Links(null, url, linkHash, false, score);
                            session.getMapper(LinksMapper.class).insert(newLink);
                        } else if (!newLink.isVisited()) {
                            newLink.setScore(newLink.getScore() + score);
                            session.getMapper(LinksMapper.class).updateScore(newLink);
                        }
                        session.commit();
                    } catch (URISyntaxException e){
                        //ignore malformed url's
                    } catch (Exception e) {
                        session.rollback();
                        log.warn("Exception ({}) while saving links. Url: '{}'", e.getMessage(), url);
                    }
                });
        session.close();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);
        blacklist = ConfUtils.loadListFromConf("focer.blacklist", stormConf)
                .stream()
                .map(s -> String.format((".*%s.*"), s))
                .collect(Collectors.joining("|"));
        resourceFolder = ConfUtils.getString(stormConf, "focer.resourceFolder");
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            //never thrown
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("url", "content", "metadata", "text", "class"));
        declarer.declareStream(StatusStreamName, new Fields("url", "metadata", "status"));
    }
}
