package lv.greenfrog.crawler.queue.persistence;

import java.util.List;

public interface AbstractMapper<T> {

    List<T> getAll();

    T getById(Integer id);

    void insert(T instance);

    void cleanTable();

}
