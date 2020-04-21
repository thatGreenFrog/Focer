package lv.greenfrog.crawler.queue.persistence;

import lv.greenfrog.crawler.queue.persistence.entity.Links;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface LinksMapper extends AbstractMapper<Links> {

    @Select("SELECT * FROM LINKS")
    @Results(value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "idDomain", column = "IDDOMAIN"),
            @Result(property = "link", column = "LINK"),
            @Result(property = "linkHash", column = "LINKHASH"),
            @Result(property = "visited", column = "VISITED"),
            @Result(property = "score", column = "SCORE")
    })
    List<Links> getAll();

    @Select("SELECT TOP 1 * FROM LINKS ORDER BY SCORE DESC")
    @Results(value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "idDomain", column = "IDDOMAIN"),
            @Result(property = "link", column = "LINK"),
            @Result(property = "linkHash", column = "LINKHASH"),
            @Result(property = "visited", column = "VISITED"),
            @Result(property = "score", column = "SCORE")
    })
    Links getByScore();

    @Select("SELECT * FROM LINKS WHERE ID = #{id}")
    @Results(value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "idDomain", column = "IDDOMAIN"),
            @Result(property = "link", column = "LINK"),
            @Result(property = "linkHash", column = "LINKHASH"),
            @Result(property = "visited", column = "VISITED"),
            @Result(property = "score", column = "SCORE")
    })
    Links getById(Integer id);

    @Select("SELECT * FROM LINKS WHERE ID = #{id} AND VISITED = #{visited}")
    @Results(value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "idDomain", column = "IDDOMAIN"),
            @Result(property = "link", column = "LINK"),
            @Result(property = "linkHash", column = "LINKHASH"),
            @Result(property = "visited", column = "VISITED"),
            @Result(property = "score", column = "SCORE")
    })
    Links getByIdAndVisited(Integer id, boolean visited);

    @Select("SELECT * FROM LINKS WHERE LINKHASH = #{hash}")
    @Results(value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "idDomain", column = "IDDOMAIN"),
            @Result(property = "link", column = "LINK"),
            @Result(property = "linkHash", column = "LINKHASH"),
            @Result(property = "visited", column = "VISITED"),
            @Result(property = "score", column = "SCORE")
    })
    Links getByHash(byte[] hash);

    @Insert("INSERT INTO LINKS (IDDOMAIN, LINK, LINKHASH, VISITED) VALUES (#{idDomain}, #{link}, #{linkHash}, 0)")
    void insert(Links link);

    @Update("UPDATE LINKS SET VISITED = #{visited} WHERE ID = #{id}")
    void updateVisited(Links link);

    @Update("UPDATE LINKS SET SCORE = #{score} WHERE ID = #{id}")
    void updateScore(Links link);

    @Delete("DELETE FROM LINKS")
    void cleanTable();

}