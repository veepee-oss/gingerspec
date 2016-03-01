package com.stratio.tests.utils;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class ElasticSearchUtilsIT {
    private final Logger logger = LoggerFactory
            .getLogger(ElasticSearchUtilsIT.class);
    @Test
    public void setSettingsTest() {
        ElasticSearchUtils es_utils = new ElasticSearchUtils();
        LinkedHashMap<String,Object> settings_map = new LinkedHashMap<String,Object>();
        settings_map.put("cluster.name",System.getProperty("ES_CLUSTER", "elasticsearch"));
        es_utils.setSettings(settings_map);
        assertThat("Non empty Exception list on boot",es_utils.getSettings().get("cluster.name"), equalTo(System
                .getProperty("ES_CLUSTER", "elasticsearch")));
        es_utils = null;
    }

    @Test
    public void connectTest(){
        ElasticSearchUtils es_utils = new ElasticSearchUtils();
        LinkedHashMap<String,Object> settings_map = new LinkedHashMap<String,Object>();
        settings_map.put("cluster.name",System.getProperty("ES_CLUSTER", "elasticsearch"));
        es_utils.setSettings(settings_map);
        try {
            es_utils.connect();
            es_utils.getClient().close();
            es_utils = null;
            assertTrue(true);
        } catch (UnknownHostException e) {
            Assert.assertTrue("An exception has been thrown: " + e.toString(),false);
        }
    }

    @Test
    public void createIndexTest(){
        ElasticSearchUtils es_utils = new ElasticSearchUtils();
        LinkedHashMap<String,Object> settings_map = new LinkedHashMap<String,Object>();
        settings_map.put("cluster.name",System.getProperty("ES_CLUSTER", "elasticsearch"));
        es_utils.setSettings(settings_map);
        try {
            es_utils.connect();
        }
        catch (UnknownHostException e) {
            Assert.assertTrue("An exception has been thrown: " + e.toString(),false);
        }
        if(es_utils.indexExists("testindex")){
            es_utils.dropSingleIndex("testindex");
        }
        boolean res = false;
        try {
            res = es_utils.createSingleIndex("testindex");
        }catch(ElasticsearchException e){
            Assert.assertTrue("An exception has been thrown: " + e.toString(),false);
        }
        assertTrue(es_utils.indexExists("testindex"));
    }

    @Test
    public void dropIndexTest(){
        ElasticSearchUtils es_utils = new ElasticSearchUtils();
        LinkedHashMap<String,Object> settings_map = new LinkedHashMap<String,Object>();
        settings_map.put("cluster.name",System.getProperty("ES_CLUSTER", "elasticsearch"));
        es_utils.setSettings(settings_map);
        try {
            es_utils.connect();
        }
        catch (UnknownHostException e) {
            Assert.assertTrue("An exception has been thrown: " + e.toString(),false);
        }
        if(!es_utils.indexExists("testindex")){
            es_utils.createSingleIndex("testindex");
        }
         try {
            es_utils.dropSingleIndex("testindex");
        }catch(ElasticsearchException e){
            Assert.assertTrue("An exception has been thrown: " + e.toString(),false);
        }
        assertFalse(es_utils.indexExists("testindex"));
        es_utils.getClient().close();
    }

    @Test
    public void createMappingTest(){
        ElasticSearchUtils es_utils = new ElasticSearchUtils();
        LinkedHashMap<String,Object> settings_map = new LinkedHashMap<String,Object>();
        settings_map.put("cluster.name",System.getProperty("ES_CLUSTER", "elasticsearch"));
        es_utils.setSettings(settings_map);
        try {
            es_utils.connect();
        }
        catch (UnknownHostException e) {
            Assert.assertTrue("An exception has been thrown: " + e.toString(),false);
        }
        if(es_utils.indexExists("testindex")){
            es_utils.dropSingleIndex("testindex");
        }
        ArrayList<XContentBuilder> mappingsource = new ArrayList<XContentBuilder>();
        XContentBuilder builder= null;
        try {
            builder = jsonBuilder()
                    .startObject()
                    .field("ident", 1)
                    .field("name", "test")
                    .field("money", 10.2)
                    .field("new", false).endObject();

        } catch (IOException e) {
            Assert.assertTrue("An exception has been thrown: " + e.toString(), false);
        }
            mappingsource.add(builder);

        try {
            es_utils.createMapping("testindex", "testmapping", mappingsource);
           // Thread.sleep(2000);
        }catch(ElasticsearchException e){
            Assert.assertTrue("An exception has been thrown: " + e.toString(),false);
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(es_utils.existsMapping("testindex","testmapping"));
        es_utils.dropMapping("testindex","testmapping");
        Assert.assertFalse(es_utils.existsMapping("testindex","testmapping"));
        es_utils.getClient().close();
    }


    @Test
    public void searchFilterEquals(){
        ElasticSearchUtils es_utils = new ElasticSearchUtils();
        LinkedHashMap<String,Object> settings_map = new LinkedHashMap<String,Object>();
        settings_map.put("cluster.name",System.getProperty("ES_CLUSTER", "elasticsearch"));
        es_utils.setSettings(settings_map);
        try {
            es_utils.connect();
        }
        catch (UnknownHostException e) {
            Assert.assertTrue("An exception has been thrown: " + e.toString(),false);
        }
        if(es_utils.indexExists("testindex")){
            es_utils.dropSingleIndex("testindex");
        }
        ArrayList<XContentBuilder> mappingsource = new ArrayList<XContentBuilder>();
        XContentBuilder builder= null,builder2 = null;
        try {
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

        } catch (IOException e) {
            Assert.assertTrue("An exception has been thrown: " + e.toString(), false);
        }
        mappingsource.add(builder);
        mappingsource.add(builder2);
        try {
            es_utils.createMapping("testindex", "testmapping", mappingsource);
            // Thread.sleep(2000);
        }catch(ElasticsearchException e){
            Assert.assertTrue("An exception has been thrown: " + e.toString(),false);
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Assert.assertTrue("An exception has been thrown: " + e.toString(), false);
        }
        Assert.assertTrue(es_utils.existsMapping("testindex","testmapping"));
        int res = 0;
        try {
            res = es_utils.searchSimpleFilterElasticsearchQuery("testindex", "testmapping", "ident", "1", "equals")
                    .size();
        }catch(Exception e){
            Assert.assertTrue("An exception has been thrown: " + e.toString(),false);

        }
        Assert.assertEquals(res, 1);
        es_utils.dropMapping("testindex","testmapping");
        es_utils.dropAllIndexes();
        Assert.assertFalse(es_utils.existsMapping("testindex","testmapping"));
        es_utils.getClient().close();
    }

}
