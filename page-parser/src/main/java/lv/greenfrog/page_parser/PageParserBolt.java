package lv.greenfrog.page_parser;

import com.digitalpebble.stormcrawler.Constants;
import com.digitalpebble.stormcrawler.Metadata;
import com.digitalpebble.stormcrawler.bolt.StatusEmitterBolt;
import com.digitalpebble.stormcrawler.persistence.Status;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.BoilerpipeContentHandler;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import static com.digitalpebble.stormcrawler.Constants.StatusStreamName;

public class PageParserBolt extends StatusEmitterBolt {

    private static final Logger log = LoggerFactory.getLogger(PageParserBolt.class);

    private BoilerpipeContentHandler handler;

    @Override
    public void execute(Tuple input) {
        log.debug("Starting web page preprocessing");

        String text;
        String url = input.getStringByField("url");
        Metadata metadata = (Metadata) input.getValueByField("metadata");
        byte[] content = input.getBinaryByField("content");

        HtmlParser parser = new HtmlParser();

        try {
            parser.parse(new ByteArrayInputStream(content),
                    new TeeContentHandler(handler),
                    new org.apache.tika.metadata.Metadata(),
                    new ParseContext());

            text = handler.getTextDocument().getText(true, false);

            collector.emit(input, new Values(url, content, metadata, text));
            collector.ack(input);
            log.debug("Web page preprocessing ended without exceptions. Result: {}", text);
        } catch (IOException | SAXException | TikaException e) {
            log.error("Exception parsing Web Page", e);

            metadata.setValue(Constants.STATUS_ERROR_MESSAGE, e.getMessage());
            collector.emit(StatusStreamName, input, new Values(url, metadata, Status.ERROR));
            collector.ack(input);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("url", "content", "metadata", "text"));
        declarer.declareStream(StatusStreamName, new Fields("url", "metadata", "status"));
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);
        handler = new BoilerpipeContentHandler(new BodyContentHandler(), ArticleExtractor.getInstance());
    }
}
