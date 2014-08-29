package com.stratio.tests.utils;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchUtils {

	private final Logger logger = LoggerFactory
			.getLogger(ElasticSearchUtils.class);
	private String url;
	private CloseableHttpClient client;

	public ElasticSearchUtils() {
		String host = System.getProperty("ELASTICSEARCH_HOST", "127.0.0.1");
		String port = System.getProperty("ELASTICSEARCH_PORT", "9200");
		this.url = "http://" + host + ":" + port + "/";
	}

	public void create() {
		logger.info("Creating elasticsearch client");
		this.client = HttpClientBuilder.create().build();

	}

	public void dropIndexes() {
		HttpDelete httpRequest = new HttpDelete(this.url + "_all");
		try {
			this.client.execute(httpRequest);
		} catch (IOException e) {
			logger.error("Got exception when deleting ES indexes", e);
		}
	}

	public void dropIndex(String indexName) {
		HttpDelete httpRequest = new HttpDelete(this.url + indexName + "/");
		try {
			this.client.execute(httpRequest);
		} catch (IOException e) {
			logger.error("Got exception when deleting the ES index", e);
		}
	}

	public String queryIndex(String indexName) {
		HttpGet httpRequest = new HttpGet(this.url + indexName + "/_search");
		try {
			CloseableHttpResponse httpResponse = client.execute(httpRequest);
			HttpEntity responseEntity = httpResponse.getEntity();
			return EntityUtils.toString(responseEntity);
		} catch (ParseException | IOException e) {
			logger.error("Got exception when querying the ES index", e);
		}
		return "ERR";
	}

	public void close() {
		try {
			this.client.close();
		} catch (IOException e) {
			logger.error("Got exception when closing the ES client", e);
		}
	}
}
