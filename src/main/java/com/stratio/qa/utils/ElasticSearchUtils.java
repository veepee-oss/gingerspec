/*
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.qa.utils;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ElasticSearchUtils {

    private String es_host;
    private int es_native_port;
    private Client client;
    private Settings settings;

    /**
     * Default constructor.
     */
    public ElasticSearchUtils() {
        this.es_host = System.getProperty("ES_NODE", "127.0.0.1");
        this.es_native_port = Integer.valueOf(System.getProperty("ES_NATIVE_PORT", "9300"));
    }

    public Settings getSettings() {
        return this.settings;
    }

    /**
     * Set settings about ES connector.
     *
     * @param settings : LinkedHashMap with all the settings about ES connection
     */
    public void setSettings(LinkedHashMap<String, Object> settings) {
        Settings.Builder builder = Settings.settingsBuilder();
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            builder.put(entry.getKey(), entry.getValue());
        }
        this.settings = builder.build();
    }

    public void setHost(String host) {
        this.es_host = host;
    }

    public void setNativePort(Integer port) {
        this.es_native_port = port;
    }

    /**
     * Connect to ES.
     */
    public void connect() throws java.net.UnknownHostException {
        this.client = TransportClient.builder().settings(this.settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(this.es_host),
                        this.es_native_port));
    }


    /**
     * Get ES client(Connected previously).
     *
     * @return es client
     */
    public Client getClient() {
        return this.client;
    }

    /**
     * Create an ES Index.
     *
     * @param indexName
     * @return true if the index has been created and false if the index has not been created.
     * @throws ElasticsearchException
     */
    public boolean createSingleIndex(String indexName) throws
            ElasticsearchException {
        CreateIndexRequest indexRequest = new CreateIndexRequest(indexName);
        CreateIndexResponse res = this.client.admin().indices().create(indexRequest).actionGet();
        return indexExists(indexName);
    }

    /**
     * Drop an ES Index
     *
     * @param indexName
     * @return true if the index exists
     * @throws ElasticsearchException
     */
    public boolean dropSingleIndex(String indexName) throws
            ElasticsearchException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
        DeleteIndexResponse res = this.client.admin().indices().delete(deleteIndexRequest).actionGet();
        return indexExists(indexName);
    }

    public boolean dropAllIndexes() {

        boolean result = true;
        ImmutableOpenMap<String, IndexMetaData> indexes = this.client.admin().cluster()
                .prepareState()
                .execute().actionGet()
                .getState().getMetaData().getIndices();

        for (String indexName : indexes.keys().toArray(String.class)) {
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
            DeleteIndexResponse res = this.client.admin().indices().delete(deleteIndexRequest).actionGet();
            result = indexExists(indexName);
        }
        return result;
    }

    /**
     * Check if an index exists in ES
     *
     * @param indexName
     * @return true if the index exists or false if the index does not exits.
     */
    public boolean indexExists(String indexName) {
        return this.client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
    }

    /**
     * Create a mapping over an index
     *
     * @param indexName
     * @param mappingName
     * @param mappingSource the data that has to be inserted in the mapping.
     */
    public void createMapping(String indexName, String mappingName, ArrayList<XContentBuilder> mappingSource) {
        IndicesExistsResponse existsResponse = this.client.admin().indices().prepareExists(indexName).execute()
                .actionGet();
        //If the index does not exists, it will be created without options
        if (!existsResponse.isExists()) {
            if (!createSingleIndex(indexName)) {
                throw new ElasticsearchException("Failed to create " + indexName
                        + " index.");
            }
        }
        BulkRequestBuilder bulkRequest = this.client.prepareBulk();
        for (int i = 0; i < mappingSource.size(); i++) {
            int aux = i + 1;

            IndexRequestBuilder res = this.client
                    .prepareIndex(indexName, mappingName, String.valueOf(aux)).setSource(mappingSource.get(i));
            bulkRequest.add(res);
        }
        bulkRequest.execute();
    }

    /**
     * Check if a mapping exists in an expecific index.
     *
     * @param indexName
     * @param mappingName
     * @return true if the mapping exists and false in other case
     */
    public boolean existsMapping(String indexName, String mappingName) {
        ClusterStateResponse resp = this.client.admin().cluster().prepareState().execute().actionGet();

        if (resp.getState().getMetaData().index(indexName) == null) {
            return false;
        }
        ImmutableOpenMap<String, MappingMetaData> mappings = resp.getState().getMetaData().index(indexName).getMappings();

        if (mappings.get(mappingName) != null) {
            return true;
        }
        return false;
    }

    /**
     * Simulate a SELET * FROM index.mapping WHERE (One simple filter)
     *
     * @param indexName
     * @param mappingName
     * @param columnName
     * @param value
     * @param filterType  [equals, gt, gte, lt, lte]
     * @return ArrayList with all the rows(One element of the ArrayList is a JSON document)
     * @throws Exception
     */
    public List<JSONObject> searchSimpleFilterElasticsearchQuery(String indexName, String mappingName, String
            columnName,
                                                                 Object value, String filterType) throws Exception {
        List<JSONObject> resultsJSON = new ArrayList<JSONObject>();
        QueryBuilder query;
        switch (filterType) {
            case "equals":
                query = QueryBuilders.termQuery(columnName, value);
                break;
            case "gt":
                query = QueryBuilders.rangeQuery(columnName).gt(value);
                break;
            case "gte":
                query = QueryBuilders.rangeQuery(columnName).gte(value);
                break;
            case "lt":
                query = QueryBuilders.rangeQuery(columnName).lt(value);
                break;
            case "lte":
                query = QueryBuilders.rangeQuery(columnName).lte(value);
                break;
            default:
                throw new Exception("Filter not implemented in the library");
        }

        SearchResponse response = this.client.prepareSearch(indexName)
                .setTypes(mappingName)
                .setSearchType(SearchType.QUERY_AND_FETCH)
                .setQuery(query)
                .execute()
                .actionGet();
        ImmutableOpenMap<Object, Object> aux = response.getContext();
        SearchHit[] results = response.getHits().getHits();
        for (SearchHit hit : results) {
            resultsJSON.add(new JSONObject(hit.getSourceAsString()));
        }
        return resultsJSON;
    }

    /**
     * Indexes a document.
     *
     * @param indexName
     * @param mappingName
     * @param id          unique identifier of the document
     * @param document
     * @throws Exception
     */
    public void indexDocument(String indexName, String mappingName, String id, XContentBuilder document)
            throws Exception {
        client.prepareIndex(indexName, mappingName, id).setSource(document).get();
    }

    /**
     * Deletes a document by its id.
     *
     * @param indexName
     * @param mappingName
     * @param id
     */
    public void deleteDocument(String indexName, String mappingName, String id) {
        client.prepareDelete(indexName, mappingName, id).get();
    }
}
