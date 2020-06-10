package lv.greenfrog.crawler;

import com.digitalpebble.stormcrawler.Constants;
import com.digitalpebble.stormcrawler.Metadata;
import com.digitalpebble.stormcrawler.bolt.StatusEmitterBolt;
import com.digitalpebble.stormcrawler.persistence.Status;
import com.digitalpebble.stormcrawler.util.ConfUtils;
import lv.greenfrog.crawler.classifier.AbstractFocerClassifier;
import lv.greenfrog.crawler.indexer.SolrIndexer;
import org.apache.commons.lang.StringUtils;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.digitalpebble.stormcrawler.Constants.StatusStreamName;

public class ClassifierBolt extends StatusEmitterBolt {

    private static final Logger log = LoggerFactory.getLogger(ClassifierBolt.class);
    private boolean indexNegative;

    private SolrIndexer solrIndexer;
    private Map<String, AbstractFocerClassifier> classifiers;

    @Override
    public void execute(Tuple input) {
        String text = input.getStringByField("text");
        String url = input.getStringByField("url");
        byte[] content = input.getBinaryByField("content");
        Metadata metadata = (Metadata) input.getValueByField("metadata");
        try {

            String className = classifiers.get("b").classify(text);

            if(className.equals("Positive")){

                className = classifiers.get("m").classify(text);

            }

            if(StringUtils.equals("Negative", className) || indexNegative)
                solrIndexer.save(content, text, url, className);

            collector.emit(input, new Values(url, content, metadata, text, className));
            collector.ack(input);
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            metadata.setValue(Constants.STATUS_ERROR_MESSAGE, e.getMessage());
            collector.emit(StatusStreamName, input, new Values(url, metadata, Status.ERROR));
            collector.ack(input);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);
        indexNegative = ConfUtils.getBoolean(stormConf, "focer.IndexNegative", false);
        solrIndexer = new SolrIndexer(ConfUtils.getString(stormConf, "focer.solr"));
        try {
            String resourceFolder = ConfUtils.getString(stormConf, "focer.resourceFolder");
            classifiers = new HashMap<>();

            classifiers.put("b",
                    new AbstractFocerClassifier(
                            resourceFolder,
                            "binary",
                            ConfUtils.getInt(stormConf, "focer.maxNgramBinary", 3),
                            ConfUtils.getInt(stormConf, "focer.binaryDocCount", 1000)
                    )
            );

            classifiers.put("m",
                    new AbstractFocerClassifier(
                            resourceFolder,
                            "multi",
                            ConfUtils.getInt(stormConf, "focer.maxNgramMulti", 3),
                            ConfUtils.getInt(stormConf, "focer.multiDocCount", 1000)
                    )
            );
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
