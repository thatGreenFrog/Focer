package lv.greenfrog.crawler.persistence.entity;

public class Links {

    Integer id;

    String link;

    byte[] linkHash;

    boolean visited;

    Integer score;

    public Links(Integer id, String link, byte[] linkHash, boolean visited, Integer score) {
        this.id = id;
        this.link = link;
        this.linkHash = linkHash;
        this.visited = visited;
        this.score = score;
    }

    public Integer getScore() {
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
