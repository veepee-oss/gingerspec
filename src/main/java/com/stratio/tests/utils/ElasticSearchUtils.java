package com.stratio.tests.utils;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Javier Delgado
 * @author Hugo Dominguez
 * 
 */
public class ElasticSearchUtils {

    private final Logger logger = LoggerFactory.getLogger(ElasticSearchUtils.class);
    private String url;
    private CloseableHttpClient client;

    /**
     * Default constructor.
     */
    public ElasticSearchUtils() {
        String host = System.getProperty("ELASTICSEARCH_HOST", "127.0.0.1");
        String port = System.getProperty("ELASTICSEARCH_PORT", "9200");
        this.url = "http://" + host + ":" + port + "/";
    }

    /**
     * Connect.
     */
    public void connect() {
        logger.debug("Creating elasticsearch client");
        this.client = HttpClientBuilder.create().build();

    }

    /**
     * Delete all elasticSearch Indexed.
     */
    public void emptyIndexes() {
        logger.debug("Emptying every entry at every elasticsearch index at {}", this.url);
        HttpDelete httpRequest = new HttpDelete(this.url + "_all/*/");
        try {
            CloseableHttpResponse response = this.client.execute(httpRequest);
            response.close();
        } catch (IOException e) {
            logger.error("Got exception when deleting ES indexes", e);
        }
    }

    /**
     * Empty the index.
     * 
     * @param indexName
     */
    public void emptyIndex(String indexName) {
        logger.debug("Emptying elasticsearch index {} at {}", indexName, this.url);
        HttpDelete httpRequest = new HttpDelete(this.url + indexName + "/*/");
        try {
            CloseableHttpResponse response = this.client.execute(httpRequest);
            response.close();
        } catch (IOException e) {
            logger.error("Got exception when deleting ES indexes", e);
        }
    }

    /**
     * Drop all the indexes.
     */
    public void dropIndexes() {
        logger.debug("Dropping every elasticsearch index at {}", this.url);
        HttpDelete httpRequest = new HttpDelete(this.url + "_all");
        try {
            CloseableHttpResponse response = this.client.execute(httpRequest);
            response.close();
        } catch (IOException e) {
            logger.error("Got exception when deleting ES indexes", e);
        }
    }

    /**
     * Drop an elasticSearch index.
     * 
     * @param indexName
     */
    public void dropIndex(String indexName) {
        logger.debug("Dropping index {} at elasticsearch at {}", indexName, this.url);
        HttpDelete httpRequest = new HttpDelete(this.url + indexName + "/");
        try {
            CloseableHttpResponse response = this.client.execute(httpRequest);
            response.close();
        } catch (IOException e) {
            logger.error("Got exception when deleting the ES index", e);
        }
    }

    /**
     * Build an elasticSearch query(format: http)
     * 
     * @param indexName
     * @param type
     * @param query
     * @return
     */
    public String queryIndex(String indexName, String type, String query) {
        logger.debug("Querying index {} in type {}, at elasticsearch at {}", indexName, type, this.url);
        HttpGet httpRequest = new HttpGet(this.url + indexName + "/" + type + "/_search?q=" + query);
        try {
            CloseableHttpResponse httpResponse = this.client.execute(httpRequest);
            HttpEntity responseEntity = httpResponse.getEntity();
            String response = EntityUtils.toString(responseEntity);
            httpResponse.close();
            return response;
        } catch (ParseException | IOException e) {
            logger.error("Got exception when querying the ES index", e);
        }
        return "ERR";
    }

    /**
     * Disconnect from elasticSearch.
     */
    public void disconnect() {
        try {
            this.client.close();
        } catch (IOException e) {
            logger.error("Got exception when closing the ES client", e);
        }
    }
}
