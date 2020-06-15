package lv.greenfrog.crawler;

import com.digitalpebble.stormcrawler.ConfigurableTopology;
import com.digitalpebble.stormcrawler.Constants;
import com.digitalpebble.stormcrawler.bolt.*;
import com.digitalpebble.stormcrawler.persistence.StdOutStatusUpdater;
import lv.greenfrog.crawler.spout.DbSpout;
import lv.greenfrog.page_parser.PageParserBolt;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

public class FocerCrawlTopology extends ConfigurableTopology {

    public static void main(String[] args) {
        ConfigurableTopology.start(new FocerCrawlTopology(), args);
    }

    @Override
    protected int run(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("queue", new DbSpout());
        builder.setBolt("partitioner", new URLPartitionerBolt())
                .shuffleGrouping("queue");
        builder.setBolt("fetch", new FetcherBolt())
                .fieldsGrouping("partitioner", new Fields("key"));
        builder.setBolt("parsePage", new PageParserBolt())
                .localOrShuffleGrouping("fetch");
        builder.setBolt("classify", new ClassifierBolt())
                .localOrShuffleGrouping("parsePage");
        builder.setBolt("parseLinks", new LinkParserBolt())
                .localOrShuffleGrouping("classify");
        Fields furl = new Fields("url");
        builder.setBolt("status", new StdOutStatusUpdater())
                .fieldsGrouping("fetch", Constants.StatusStreamName, furl)
                .fieldsGrouping("parsePage", Constants.StatusStreamName, furl)
                .fieldsGrouping("classify", Constants.StatusStreamName, furl)
                .fieldsGrouping("parseLinks", Constants.StatusStreamName, furl);
        return submit("crawl_tf_idf_final", conf, builder);
    }

}
