package lv.greenfrog.crawler;

import com.digitalpebble.stormcrawler.Constants;
import com.digitalpebble.stormcrawler.Metadata;
import com.digitalpebble.stormcrawler.bolt.StatusEmitterBolt;
import com.digitalpebble.stormcrawler.persistence.Status;
import com.digitalpebble.stormcrawler.util.CharsetIdentification;
import lv.greenfrog.crawler.persistence.DomainsMapper;
import lv.greenfrog.crawler.persistence.LinksMapper;
import lv.greenfrog.crawler.persistence.entity.Domains;
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

import static com.digitalpebble.stormcrawler.Constants.StatusStreamName;

public class LinkParser extends StatusEmitterBolt {

    private static final Logger log = LoggerFactory.getLogger(LinkParser.class);
    private MessageDigest md;

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
        SqlSession session = SessionManager.getSession();
        final int score = pageClass.equals("Negative") ? -1 : 1;
        anchor.stream()
                .filter(a -> !"nofollow".equalsIgnoreCase(a.attr("rel")) && a.attr("abs:href") != null && !a.attr("abs:href").isEmpty())
                .map(a -> a.attr("abs:href"))
                .forEach(url -> {
                    try {
                        URI uri = new URI(url);

                        if(uri.getHost() == null) throw new URISyntaxException("", ""); //e-mail and other crap
                        String domain = uri.getHost().startsWith("www.") ? uri.getHost().substring(4) : uri.getHost();

                        Domains newDomain = session.getMapper(DomainsMapper.class).getByLinkDomain(domain);
                        if (newDomain == null) {
                            newDomain = new Domains(null, domain);
                            session.getMapper(DomainsMapper.class).insert(newDomain);
                            newDomain = session.getMapper(DomainsMapper.class).getByLinkDomain(domain);
                        }

                        byte[] linkHash = md.digest(url.getBytes());
                        Links newLink = session.getMapper(LinksMapper.class).getByHash(linkHash);
                        if (newLink == null) {
                            newLink = new Links(null, newDomain.getId(), url, linkHash, false, score, null);
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

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);
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
