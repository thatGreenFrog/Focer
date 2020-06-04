package lv.greenfrog.crawler.indexer;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.UUID;

public class SolrIndexer{

    private HttpSolrClient solrClient;

    public SolrIndexer(String solrUrl) {
        this.solrClient = new HttpSolrClient.Builder(solrUrl).build();
    }

    public void save(byte[] content, String text, String url, String className) throws IOException, SolrServerException {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("Content", content);
        doc.addField("id", UUID.randomUUID());
        doc.addField("url", url);
        doc.addField("Text", text);
        doc.addField("Class", className);
        solrClient.add(doc);
        solrClient.commit();
    }

    public boolean solrIsReachable() throws IOException, SolrServerException {
        return solrClient.ping().getStatus() == 0;
    }
}
