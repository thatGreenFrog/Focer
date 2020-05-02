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

    public void save(String title, byte[] content) throws IOException, SolrServerException {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("Title", title);
        doc.addField("Content", content);
        doc.addField("id", UUID.randomUUID());
        solrClient.add(doc);
        solrClient.commit();
    }
}
