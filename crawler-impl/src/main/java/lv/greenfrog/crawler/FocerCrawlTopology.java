package lv.greenfrog.crawler;

import com.digitalpebble.stormcrawler.ConfigurableTopology;
import com.digitalpebble.stormcrawler.Constants;
import com.digitalpebble.stormcrawler.bolt.*;
import com.digitalpebble.stormcrawler.indexing.StdOutIndexer;
import com.digitalpebble.stormcrawler.persistence.StdOutStatusUpdater;
import lv.greenfrog.crawler.spout.DbSpout;
import lv.greenfrog.page_parser.PageParserBolt;
import net.sf.cglib.core.ClassInfo;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

public class FocerCrawlTopology extends ConfigurableTopology {

    public static void main(String[] args) {
        ConfigurableTopology.start(new FocerCrawlTopology(), args);
    }

    @Override
    protected int run(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();

        String[] testURLs = new String[] { "https://www.delfi.lv/auto/zinas/lamborgini-trakojosa-versa-gars-no-traktoru-razosanas-lidz-musdienu-superauto-definicijai.d?id=52106937" };

        builder.setSpout("spout", new DbSpout(true, testURLs));

        builder.setBolt("partitioner", new URLPartitionerBolt())
                .shuffleGrouping("spout");

        builder.setBolt("fetch", new FetcherBolt())
                .fieldsGrouping("partitioner", new Fields("key"));

        builder.setBolt("sitemap", new SiteMapParserBolt())
                .localOrShuffleGrouping("fetch");

        builder.setBolt("feeds", new FeedParserBolt())
                .localOrShuffleGrouping("sitemap");

        builder.setBolt("parse", new PageParserBolt())
                .localOrShuffleGrouping("feeds");

        builder.setBolt("classify", new ClassifierBolt())
                .localOrShuffleGrouping("parse");

        Fields furl = new Fields("url");

        // can also use MemoryStatusUpdater for simple recursive crawls
        builder.setBolt("status", new StdOutStatusUpdater())
                .fieldsGrouping("fetch", Constants.StatusStreamName, furl)
                .fieldsGrouping("sitemap", Constants.StatusStreamName, furl)
                .fieldsGrouping("feeds", Constants.StatusStreamName, furl)
                .fieldsGrouping("parse", Constants.StatusStreamName, furl)
                .fieldsGrouping("classify", Constants.StatusStreamName, furl);

        return submit("crawl", conf, builder);
    }

}
