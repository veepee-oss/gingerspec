package com.stratio.tests.utils;

import static org.elasticsearch.common.settings.ImmutableSettings.*;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.index.query.RangeFilterBuilder;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsAction;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stratio.exceptions.DBException;

import gherkin.deps.com.google.gson.Gson;
import gherkin.deps.com.google.gson.GsonBuilder;

public class ElasticSearchUtils {

	private final Logger logger = LoggerFactory
			.getLogger(ElasticSearchUtils.class);

	private String es_host;
	private int es_port;
	private int es_native_port;
	private String cluster;
    private Client client;
    private Settings settings;
    private CreateIndexRequest indexRequest;

	/**
	 * Default constructor.
	 */
	public ElasticSearchUtils() {
		this.es_host = System.getProperty("ES_NODE", "127.0.0.1");
		this.es_port = Integer.valueOf(System.getProperty("ES_PORT", "9200"));
		this.es_native_port = Integer.valueOf(System.getProperty("ES_NATIVE_PORT", "9300"));
		this.cluster = System.getProperty("ES_CLUSTER", "elasticsearch");
	}

    /**
     * Set settings about ES connector.
     *
     * @param settings : LinkedHashMap with all the settings about ES connection
     */
	public void setSettings(LinkedHashMap<String, Object> settings){
        ImmutableSettings.Builder builder = ImmutableSettings.settingsBuilder();
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            builder.put(entry.getKey(),entry.getValue());
        }
        this.settings = builder.build();
    }

    public Settings getSettings(){
        return this.settings;
    }

    /**
     * Connect to ES.
     */
    public void connect() throws java.net.UnknownHostException{
        this.client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(this.es_host),
                        this.es_native_port));
    }

    /**
     * Get ES client(Connected previously).
     * @return es client
     */
    public Client getClient(){
        return this.client;
    }

    /**
     * Create an ES Index.
     * @param indexName
     * @return true if the index has been created and false if the index has not been created.
     * @throws ElasticsearchException
     */
    public boolean createSingleIndex(String indexName) throws
            ElasticsearchException
    {
        CreateIndexRequest indexRequest = new CreateIndexRequest(indexName);
        CreateIndexResponse res = this.client.admin().indices().create(indexRequest).actionGet();
        return indexExists(indexName);
    }

    /**
     * Create an ES Index with options.
     * @param indexName
     * @param settings : LinkedHashMap with all the settings of the ES indexes.
     * @return true if the index has been created and false if the index has not been created.
     * @throws ElasticsearchException
     */
    public boolean createSingleIndex(String indexName,LinkedHashMap<String, Object> settings) throws
    ElasticsearchException
    {
        ImmutableSettings.Builder indexSettings = ImmutableSettings.settingsBuilder();
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            indexSettings.put(entry.getKey(),entry.getValue());
        }
        CreateIndexRequest indexRequest = new CreateIndexRequest(indexName, indexSettings.build());
        CreateIndexResponse res = this.client.admin().indices().create(indexRequest).actionGet();
        return indexExists(indexName);
    }

    /**
     * Drop an ES Index
     * @param indexName
     * @return
     * @throws ElasticsearchException
     */
    public boolean dropSingleIndex(String indexName) throws
            ElasticsearchException
    {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
        DeleteIndexResponse res = this.client.admin().indices().delete(deleteIndexRequest).actionGet();
        return indexExists(indexName);
    }

    public boolean dropAllIndexes(){
        ImmutableOpenMap<String, ImmutableOpenMap<String, AliasMetaData>> indexes =  this.client.admin().cluster()
                .prepareState()
                .execute().actionGet()
                .getState().getMetaData().aliases();
        return false;


    }

    /**
     * Check if an index exists in ES
     * @param indexName
     * @return true if the index exists or false if the index does not exits.
     */
    public boolean indexExists(String indexName){
        return this.client.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
    }

    /**
     * Create a mapping over an index
     * @param indexName
     * @param mappingName
     * @param mappingSource the data that has to be inserted in the mapping.
     */
    public void createMapping(String indexName, String mappingName, ArrayList<XContentBuilder> mappingSource){
         IndicesExistsResponse existsResponse = this.client.admin().indices().prepareExists(indexName).execute()
                .actionGet();
        //If the index does not exists, it will be created without options
        if(!existsResponse.isExists()){
            if(!createSingleIndex(indexName)){
                throw new ElasticsearchException("Failed to create " + indexName
                        + " index.");
            }
        }
        BulkRequestBuilder bulkRequest = this.client.prepareBulk();
        for(int i = 0; i < mappingSource.size(); i++){
            int aux = i + 1;

            IndexRequestBuilder res = this.client
                    .prepareIndex(indexName, mappingName, String.valueOf(aux)).setSource(mappingSource.get(i));
            bulkRequest.add(res);
        }
        bulkRequest.execute();
    }

    /**
     * Drop a mapping of an index
     * @param indexName
     * @param mappingName
     */
    public void dropMapping(String indexName, String mappingName){
        if(existsMapping(indexName,mappingName) ){
            DeleteMappingRequest deleteMapping = new DeleteMappingRequest(indexName).types(mappingName);
            DeleteMappingResponse actionGet = this.client.admin().indices().deleteMapping(deleteMapping).actionGet();
        }
    }

    /**
     * Check if a mapping exists in an expecific index.
     * @param indexName
     * @param mappingName
     * @return
     */
    public boolean existsMapping(String indexName, String mappingName){
        ClusterStateResponse resp = this.client.admin().cluster().prepareState().execute().actionGet();
        ImmutableOpenMap<String, MappingMetaData> mappings = resp.getState().getMetaData().index(indexName).getMappings();
        if(mappings.get(mappingName) != null){
            return true;
        }
        return false;
    }

    /**
     * Simulate a SELET * FROM index.mapping WHERE (One simple filter)
     * @param indexName
     * @param mappingName
     * @param columnName
     * @param value
     * @param filterType [equals, gt, gte, lt, lte]
     * @return ArrayList with all the rows(One element of the ArrayList is a JSON document)
     * @throws Exception
     */
    public List<JSONObject> searchSimpleFilterElasticsearchQuery(String indexName, String mappingName, String
            columnName,
            Object value, String filterType) throws Exception {
        List<JSONObject> resultsJSON = new ArrayList<JSONObject>();
        org.elasticsearch.index.query.RangeFilterBuilder range;
        QueryBuilder query;
        switch (filterType){
        case "equals":
            query = QueryBuilders.termQuery(columnName,value);
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
        ImmutableOpenMap<Object,Object> aux = response.getContext();
        SearchHit[] results = response.getHits().getHits();
        for(SearchHit hit : results) {
            resultsJSON.add(new JSONObject(hit.getSourceAsString()));
        }
        return resultsJSON;
    }


}
