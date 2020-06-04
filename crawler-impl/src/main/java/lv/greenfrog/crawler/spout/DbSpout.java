package lv.greenfrog.crawler.spout;

import com.digitalpebble.stormcrawler.Metadata;
import com.digitalpebble.stormcrawler.persistence.AbstractQueryingSpout;
import com.digitalpebble.stormcrawler.util.ConfUtils;
import com.digitalpebble.stormcrawler.util.StringTabScheme;
import lv.greenfrog.crawler.SessionManager;
import lv.greenfrog.crawler.persistence.DomainsMapper;
import lv.greenfrog.crawler.persistence.LinksMapper;
import lv.greenfrog.crawler.persistence.entity.Domains;
import lv.greenfrog.crawler.persistence.entity.Links;
import org.apache.ibatis.session.SqlSession;
import org.apache.storm.shade.org.apache.jute.Utils;
import org.apache.storm.spout.Scheme;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DbSpout extends AbstractQueryingSpout {

    private static final Logger log = LoggerFactory.getLogger(DbSpout.class);

    private static final Scheme SCHEME = new StringTabScheme();

    private String resourceFolder;

    @SuppressWarnings("unchecked")
    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        super.open(conf, context, collector);
        resourceFolder = ConfUtils.getString(conf, "focer.resourceFolder");
        boolean cleanDb = ConfUtils.getBoolean(conf, "focer.cleanDb", false);

        SqlSession session = SessionManager.getSession(resourceFolder);
        LinksMapper lm = session.getMapper(LinksMapper.class);
        if(cleanDb || lm.getAll().isEmpty()){
            List<String> seeds = ConfUtils.loadListFromConf("focer.seeds", conf);
            DomainsMapper dm = session.getMapper(DomainsMapper.class);

            lm.cleanTable();
            dm.cleanTable();
            seeds.forEach(s -> {
                       dm.insert(new Domains(null, s));
                       try {
                           lm.insert(new Links(null, dm.getByLinkDomain(s).getId(), s, MessageDigest.getInstance("SHA-1").digest(s.getBytes()), false, 0, null));
                       } catch (NoSuchAlgorithmException e) {
                           log.error("Error hashing link: {}", s, e);
                       }
                   });

            session.commit();
            session.close();
        }

    }

    @Override
    protected void populateBuffer() {
        SqlSession session = SessionManager.getSession(resourceFolder);
        Links link = session.getMapper(LinksMapper.class).getByScore();
        if(link != null) {
            List linkData = SCHEME.deserialize(ByteBuffer.wrap((link.getLink() + "\t" + link.getMetadata()).getBytes()));
            buffer.add((String)linkData.get(0), (Metadata)linkData.get(1));
            session.getMapper(LinksMapper.class).updateVisited(link);
            session.commit();
            session.close();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(SCHEME.getOutputFields());
    }
}
