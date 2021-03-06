package lv.greenfrog.page_parser;

import com.digitalpebble.stormcrawler.Constants;
import com.digitalpebble.stormcrawler.Metadata;
import com.digitalpebble.stormcrawler.bolt.StatusEmitterBolt;
import com.digitalpebble.stormcrawler.persistence.Status;
import com.digitalpebble.stormcrawler.protocol.HttpHeaders;
import com.optimaize.langdetect.DetectedLanguage;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import lv.greenfrog.page_parser.exceptions.LanguageException;
import lv.greenfrog.page_parser.exceptions.MimeTypeException;
import lv.greenfrog.page_parser.exceptions.PageParserException;
import org.apache.commons.lang.StringUtils;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.mime.MediaType;
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
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.digitalpebble.stormcrawler.Constants.StatusStreamName;

public class PageParserBolt extends StatusEmitterBolt {

    private static final Logger log = LoggerFactory.getLogger(PageParserBolt.class);

    private BoilerpipeContentHandler handler;
    private LanguageDetector languageDetector;
    private Detector detector;

    @Override
    public void execute(Tuple input) {
        log.debug("Starting web page preprocessing");

        String text;
        String url = input.getStringByField("url");
        Metadata metadata = (Metadata) input.getValueByField("metadata");
        byte[] content = input.getBinaryByField("content");

        HtmlParser parser = new HtmlParser();

        try {
            String mimeType = getMimeType(url, metadata.getFirstValue(HttpHeaders.CONTENT_TYPE), content);
            if(!mimeType.toLowerCase().contains("html")) throw new MimeTypeException(mimeType, url);

            parser.parse(new ByteArrayInputStream(content),
                    new TeeContentHandler(handler),
                    new org.apache.tika.metadata.Metadata(),
                    new ParseContext());

            text = handler.getTextDocument().getText(true, false);

            List<DetectedLanguage> detectedLanguages = languageDetector.getProbabilities(text);
            if(detectedLanguages.size() == 0) throw new LanguageException(LanguageException.LANGUAGE_NOT_DETECTED);

            String language = detectedLanguages.get(0).getLocale().getLanguage();
            if(!"lv".equalsIgnoreCase(language)) throw new LanguageException(String.format(LanguageException.WRONG_LANGUAGE, language, url));

            collector.emit(input, new Values(url, content, metadata, text));
            collector.ack(input);

            log.debug("Web page preprocessing ended without exceptions. Result: {}", text);
        } catch (IOException | SAXException | TikaException e) {
            log.error("Exception parsing Web Page", e);

            metadata.setValue(Constants.STATUS_ERROR_MESSAGE, e.getMessage());
            collector.emit(StatusStreamName, input, new Values(url, metadata, Status.ERROR));
            collector.ack(input);
        }
        catch(PageParserException ppe){
            log.warn("Skipping web page. Reason: {}", ppe.getMessage());

            metadata.setValue(Constants.STATUS_ERROR_MESSAGE, ppe.getMessage());
            collector.emit(StatusStreamName, input, new Values(url, metadata, Status.ERROR));
            collector.ack(input);
        }
        handler.recycle();
    }

    public String getMimeType(String URL, String httpCT, byte[] content) {

        org.apache.tika.metadata.Metadata metadata = new org.apache.tika.metadata.Metadata();

        if (StringUtils.isNotBlank(httpCT)) {
            metadata.set(org.apache.tika.metadata.Metadata.CONTENT_TYPE, httpCT);
        }
        metadata.set(org.apache.tika.metadata.Metadata.RESOURCE_NAME_KEY, URL);
        metadata.set(org.apache.tika.metadata.Metadata.CONTENT_LENGTH, Integer.toString(content.length));
        try (InputStream stream = new ByteArrayInputStream(content)) {
            MediaType mt = detector.detect(stream, metadata);
            return mt.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected IOException", e);
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
        handler = new BoilerpipeContentHandler(new BodyContentHandler(-1), ArticleExtractor.getInstance());

        detector = TikaConfig.getDefaultConfig().getDetector();

        try {
            List<LanguageProfile> l = new LanguageProfileReader().readAllBuiltIn();
            languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard()).withProfiles(l).build();
        } catch (IOException e) {
            log.error("Exception initializing language detector.", e);
            throw new RuntimeException(e);
        }
    }
}
