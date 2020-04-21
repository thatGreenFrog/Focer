package lv.greenfrog.crawler.queue.persistence.entity;

public class Domains {

    Integer id;

    String linkDomain;

    public Domains(Integer id, String linkDomain) {
        this.id = id;
        this.linkDomain = linkDomain;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLinkDomain() {
        return linkDomain;
    }

    public void setLinkDomain(String linkDomain) {
        this.linkDomain = linkDomain;
    }
}
