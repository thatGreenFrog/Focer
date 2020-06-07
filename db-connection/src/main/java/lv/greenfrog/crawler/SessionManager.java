package lv.greenfrog.crawler;

import lv.greenfrog.crawler.persistence.LinksMapper;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import java.io.File;

public class SessionManager {

    private static SqlSessionFactory factory;

    public static SqlSession getSession(String resourceFolder){
        if(factory == null){
            Configuration cnf = new Configuration();
            cnf.addMappers("lv.greenfrog.crawler.persistence", LinksMapper.class);
            Environment env = new Environment(
                    "queue",
                    new JdbcTransactionFactory(),
                    new PooledDataSource(
                            "org.h2.Driver",
                            String.format("jdbc:h2:%s%s%s%s%s", resourceFolder, File.separator, "db", File.separator, "QUEUE"),
                            null,
                            null
                    )
            );
            cnf.setEnvironment(env);
            factory = new SqlSessionFactoryBuilder().build(cnf);
        }
        return factory.openSession();
    }
}
