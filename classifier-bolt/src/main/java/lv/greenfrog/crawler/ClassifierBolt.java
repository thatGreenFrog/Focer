package lv.greenfrog.crawler;

import com.digitalpebble.stormcrawler.bolt.StatusEmitterBolt;
import lv.greenfrog.crawler.indexer.SolrIndexer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.io.IOException;
import java.util.Map;

import static com.digitalpebble.stormcrawler.Constants.StatusStreamName;

public class ClassifierBolt extends StatusEmitterBolt {

    private SolrIndexer solrIndexer;

    @Override
    public void execute(Tuple input) {
        try {
            solrIndexer.save("rando title", input.getBinaryByField("content"));
        } catch (IOException | SolrServerException e) {
            e.printStackTrace();
        }

        collector.ack(input);

    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);
        solrIndexer = new SolrIndexer("http://192.168.0.125:8983/solr/crawl");
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("url", "content", "metadata", "text"));
        declarer.declareStream(StatusStreamName, new Fields("url", "metadata", "status"));
    }
}
