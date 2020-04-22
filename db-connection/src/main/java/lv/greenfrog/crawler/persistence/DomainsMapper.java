package lv.greenfrog.crawler.persistence;

import lv.greenfrog.crawler.persistence.entity.Domains;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface DomainsMapper extends AbstractMapper<Domains> {

    @Select("SELECT * FROM DOMAINS")
    @Results( value = {
            @Result(property = "id", column = "Id"),
            @Result(property = "linkDomain", column = "LinkDomain"),
    })
    List<Domains> getAll();

    @Select("SELECT * FROM DOMAINS WHERE ID = #{id}")
    @Results( value = {
            @Result(property = "id", column = "Id"),
            @Result(property = "linkDomain", column = "LinkDomain"),
    })
    Domains getById(int id);

    @Select("SELECT * FROM DOMAINS WHERE LINKDOMAIN = #{linkDomain}")
    @Results( value = {
            @Result(property = "id", column = "Id"),
            @Result(property = "linkDomain", column = "LinkDomain"),
    })
    Domains getByLinkDomain(String linkDomain);

    @Insert("INSERT INTO DOMAINS (LINKDOMAIN) VALUES (#{linkDomain})")
    void insert(Domains domain);

    @Delete("DELETE FROM DOMAINS")
    void cleanTable();

}
