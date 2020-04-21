package lv.greenfrog.crawler.queue.persistence.entity;

public class Links {

    Integer id;

    Integer idDomain;

    String link;

    byte[] linkHash;

    boolean visited;

    int score;

    public Links(Integer id, Integer idDomain, String link, byte[] linkHash, boolean visited, int score) {
        this.id = id;
        this.idDomain = idDomain;
        this.link = link;
        this.linkHash = linkHash;
        this.visited = visited;
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdDomain() {
        return idDomain;
    }

    public void setIdDomain(Integer idDomain) {
        this.idDomain = idDomain;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public byte[] getLinkHash() {
        return linkHash;
    }

    public void setLinkHash(byte[] linkHash) {
        this.linkHash = linkHash;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}
