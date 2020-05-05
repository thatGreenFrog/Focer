package lv.greenfrog.crawler;

import com.digitalpebble.stormcrawler.Constants;
import com.digitalpebble.stormcrawler.Metadata;
import com.digitalpebble.stormcrawler.bolt.StatusEmitterBolt;
import com.digitalpebble.stormcrawler.persistence.Status;
import lv.greenfrog.crawler.classifier.Classifier;
import lv.greenfrog.crawler.classifier.PreProcessor;
import lv.greenfrog.crawler.indexer.SolrIndexer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.storm.shade.org.apache.commons.collections.FastArrayList;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static com.digitalpebble.stormcrawler.Constants.StatusStreamName;

public class ClassifierBolt extends StatusEmitterBolt {

    private static final Logger log = LoggerFactory.getLogger(ClassifierBolt.class);

    private SolrIndexer solrIndexer;
    private Classifier classifier;
    private PreProcessor preProcessor;

    @Override
    public void execute(Tuple input) {
        String text = input.getStringByField("text");
        String url = input.getStringByField("url");
        byte[] content = input.getBinaryByField("content");
        Metadata metadata = (Metadata) input.getValueByField("metadata");
        try {
            String className = classifier.classify(preProcessor.preProcess(text));


            collector.emit(input, new Values(url, content, metadata, text, className));
            collector.ack(input);
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            metadata.setValue(Constants.STATUS_ERROR_MESSAGE, e.getMessage());
            collector.emit(StatusStreamName, input, new Values(url, metadata, Status.ERROR));
            collector.ack(input);
        }

        collector.ack(input);

    }

    @SuppressWarnings("unchecked")
    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);
        solrIndexer = new SolrIndexer((String) stormConf.getOrDefault("focer.solr", "http://192.168.0.125:8983/solr/crawl"));
        try {
            classifier = new Classifier((String) stormConf.getOrDefault("focer.resourceFolder", "D:\\GitHub\\Focer\\resources"));
            preProcessor = new PreProcessor();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("url", "content", "metadata", "text", "class"));
        declarer.declareStream(StatusStreamName, new Fields("url", "metadata", "status"));
    }
}
