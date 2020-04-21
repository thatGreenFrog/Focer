package lv.greenfrog.crawler.queue;

import com.digitalpebble.stormcrawler.persistence.AbstractQueryingSpout;
import com.digitalpebble.stormcrawler.util.StringTabScheme;
import lv.greenfrog.crawler.queue.persistence.AbstractMapper;
import lv.greenfrog.crawler.queue.persistence.DomainsMapper;
import lv.greenfrog.crawler.queue.persistence.LinksMapper;
import lv.greenfrog.crawler.queue.persistence.entity.Domains;
import lv.greenfrog.crawler.queue.persistence.entity.Links;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.storm.spout.Scheme;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

public class DbSpout extends AbstractQueryingSpout {

    private static final Logger log = LoggerFactory.getLogger(DbSpout.class);

    private static final Scheme SCHEME = new StringTabScheme();

    private SqlSessionFactory sqlFactory;
    private boolean cleanDb;
    private final String[] seeds;

    public DbSpout(boolean cleanDb, String[] seeds) {
        this.cleanDb = cleanDb;
        this.seeds = seeds;
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        super.open(conf, context, collector);

        Configuration cnf = new Configuration();
        cnf.addMappers("lv.greenfrog.crawler.queue.persistence", AbstractMapper.class);
        Environment env = new Environment(
                "queue",
                new JdbcTransactionFactory(),
                new PooledDataSource(
                        "org.h2.Driver",
                        "jdbc:h2:C:\\crawler\\QUEUE",
                        null,
                        null
                )
        );
        cnf.setEnvironment(env);
        sqlFactory = new SqlSessionFactoryBuilder().build(cnf);

        if(cleanDb){
           SqlSession session = sqlFactory.openSession();
           LinksMapper lm = session.getMapper(LinksMapper.class);
           DomainsMapper dm = session.getMapper(DomainsMapper.class);

           lm.cleanTable();
           dm.cleanTable();
           Arrays.stream(seeds)
                   .forEach(s -> {
                       dm.insert(new Domains(null, s));
                       try {
                           lm.insert(new Links(null, dm.getByLinkDomain(s).getId(), s, MessageDigest.getInstance("SHA-1").digest(s.getBytes()), false, 1));
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
        SqlSession session = sqlFactory.openSession();
        buffer.add(session.getMapper(LinksMapper.class).getByScore().getLink(), null);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(SCHEME.getOutputFields());
    }

    @Override
    public void close() {
        super.close();
    }
}
