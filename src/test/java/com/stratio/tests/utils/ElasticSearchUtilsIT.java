package com.stratio.tests.utils;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;

public class ElasticSearchUtilsIT {
    private final Logger logger = LoggerFactory
            .getLogger(ElasticSearchUtilsIT.class);

    private ElasticSearchUtils es_utils;

    @BeforeMethod
    public void setSettingsTest() {
        es_utils = new ElasticSearchUtils();
        LinkedHashMap<String,Object> settings_map = new LinkedHashMap<String,Object>();
        settings_map.put("cluster.name",System.getProperty("ES_CLUSTER", "elasticsearch"));
        es_utils.setSettings(settings_map);
        assertThat(es_utils.getSettings().get("cluster.name")).as("Non empty Exception list on boot").isEqualTo(System
                .getProperty("ES_CLUSTER", "elasticsearch"));
    }

    @Test(expectedExceptions = java.net.UnknownHostException.class)
    public void connectErrorTest() throws UnknownHostException {
        LinkedHashMap<String,Object> settings_map = new LinkedHashMap<String,Object>();
        es_utils.setHost("www.badhost.ccom");
        es_utils.setSettings(settings_map);
        es_utils.connect();
        es_utils.getClient().close();
    }

    @Test
    public void connectTest() throws UnknownHostException {
        es_utils.connect();
        assertThat(es_utils.getClient().admin().cluster().prepareNodesInfo().all().execute()).isNotNull();
        es_utils.getClient().close();
    }

    @Test
    public void createIndexTest() throws UnknownHostException{
        es_utils.connect();
        if(es_utils.indexExists("testindex")){
            es_utils.dropSingleIndex("testindex");
        }
        es_utils.createSingleIndex("testindex");
        assertThat(es_utils.indexExists("testindex")).isTrue();
    }

    @Test
    public void dropIndexTest() throws UnknownHostException{
        es_utils.connect();
        if(!es_utils.indexExists("testindex")){
            es_utils.createSingleIndex("testindex");
        }
        es_utils.dropSingleIndex("testindex");
        assertThat(es_utils.indexExists("testindex")).isFalse();
        es_utils.getClient().close();
    }

    @Test
    public void createMappingTest() throws UnknownHostException,IOException,InterruptedException{
        es_utils.connect();
        if(es_utils.indexExists("testindex")){
            es_utils.dropSingleIndex("testindex");
        }
        ArrayList<XContentBuilder> mappingsource = new ArrayList<XContentBuilder>();
        XContentBuilder builder= null;

        builder = jsonBuilder()
                    .startObject()
                    .field("ident", 1)
                    .field("name", "test")
                    .field("money", 10.2)
                    .field("new", false).endObject();

        mappingsource.add(builder);
        es_utils.createMapping("testindex", "testmapping", mappingsource);
        Thread.sleep(2000);
        assertThat(es_utils.existsMapping("testindex","testmapping")).isTrue();
        es_utils.dropMapping("testindex","testmapping");
        assertThat(es_utils.existsMapping("testindex","testmapping")).isFalse();
        es_utils.getClient().close();
    }

    @Test
    public void searchFilterEquals() throws UnknownHostException,IOException,InterruptedException{
        es_utils.connect();
        if(es_utils.indexExists("testindex")){
            es_utils.dropSingleIndex("testindex");
        }
        ArrayList<XContentBuilder> mappingsource = new ArrayList<XContentBuilder>();
        XContentBuilder builder= null,builder2 = null;

            builder = jsonBuilder()
                    .startObject()
                    .field("ident", 1)
                    .field("name", "test")
                    .field("money", 10.2)
                    .field("new", false).endObject();
            builder2 = jsonBuilder()
                    .startObject()
                    .field("ident", 2)
                    .field("name", "test")
                    .field("money", 10.2)
                    .field("new", false).endObject();


        mappingsource.add(builder);
        mappingsource.add(builder2);
        es_utils.createMapping("testindex", "testmapping", mappingsource);
        Thread.sleep(2000);
        assertThat(es_utils.existsMapping("testindex","testmapping")).isTrue();
        int res = 0;
        try {
            res = es_utils.searchSimpleFilterElasticsearchQuery("testindex", "testmapping", "ident", "1", "equals")
                    .size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertThat(res).isEqualTo(1);
        es_utils.dropMapping("testindex","testmapping");
        es_utils.dropAllIndexes();
        assertThat(es_utils.existsMapping("testindex","testmapping")).isFalse();
        es_utils.getClient().close();
    }
}
