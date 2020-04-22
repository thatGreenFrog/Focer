package lv.greenfrog.crawler;

import lv.greenfrog.crawler.persistence.AbstractMapper;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

public class SessionManager {

    private static SqlSessionFactory factory;

    public static SqlSession getSession(){
        if(factory == null){
            Configuration cnf = new Configuration();
            cnf.addMappers("lv.greenfrog.crawler.persistence", AbstractMapper.class);
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
            factory = new SqlSessionFactoryBuilder().build(cnf);
        }
        return factory.openSession();
    }
}
