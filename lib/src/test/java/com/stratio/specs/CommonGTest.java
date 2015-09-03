package com.stratio.specs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jetty.http.HttpContent;
import org.hjson.ParseException;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;

import com.stratio.tests.utils.ThreadProperty;

import cucumber.api.DataTable;
import static org.mockito.Mockito.*;

import com.ning.http.client.Response;

import java.util.concurrent.Future;

public class CommonGTest {

    @Test
    public void replaceEmptyPlaceholdersTest() {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        assertThat(commong.replacePlaceholders("")).as("Replacing an empty placeholded string should not modify it").isEqualTo("");
    }

    @Test
    public void replaceSinglePlaceholdersTest() {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        System.setProperty("STRATIOBDD_ENV1", "33");
        System.setProperty("STRATIOBDD_ENV2", "aa");

        assertThat(commong.replacePlaceholders("${STRATIOBDD_ENV1}"))
        	.as("Unexpected replacement").isEqualTo("33");
        assertThat(commong.replacePlaceholders("${STRATIOBDD_ENV1}${STRATIOBDD_ENV2}"))
        	.as("Unexpected replacement").isEqualTo("33aa");
        assertThat(commong.replacePlaceholders("${STRATIOBDD_ENV1}:${STRATIOBDD_ENV2}"))
        	.as("Unexpected replacement").isEqualTo("33:aa");
        assertThat(commong.replacePlaceholders("|${STRATIOBDD_ENV1}|:|${STRATIOBDD_ENV2}|"))
        	.as("Unexpected replacement").isEqualTo("|33|:|aa|");
        assertThat(commong.replacePlaceholders("|${STRATIOBDD_ENV}|:|${STRATIOBDD_ENV2}|"))
        	.as("Unexpected replacement").isEqualTo("||:|aa|");
    }

    @Test
    public void replaceSinglePlaceholderCaseTest() {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        System.setProperty("STRATIOBDD_ENV1", "33");
        System.setProperty("STRATIOBDD_ENV2", "aA");

        assertThat(commong.replacePlaceholders("${STRATIOBDD_ENV1.toUpper}")).as("Unexpected replacement").isEqualTo("33");
        assertThat(commong.replacePlaceholders("${STRATIOBDD_ENV1.toLower}")).as("Unexpected replacement").isEqualTo("33");
        assertThat(commong.replacePlaceholders("${STRATIOBDD_ENV2.toUpper}")).as("Unexpected replacement").isEqualTo("AA");
        assertThat(commong.replacePlaceholders("${STRATIOBDD_ENV2.toLower}")).as("Unexpected replacement").isEqualTo("aa");
        assertThat(commong.replacePlaceholders("${STRATIOBDD_ENV1}${STRATIOBDD_ENV2.toLower}")).as("Unexpected replacement").isEqualTo("33aa");
        assertThat(commong.replacePlaceholders("${STRATIOBDD_ENV1}:${STRATIOBDD_ENV2.toUpper}")).as("Unexpected replacement").isEqualTo("33:AA");
        assertThat(commong.replacePlaceholders("|${STRATIOBDD_ENV.toUpper}|:|${STRATIOBDD_ENV2}|")).as("Unexpected replacement").isEqualTo("||:|aA|");
        assertThat(commong.replacePlaceholders("|${STRATIOBDD_ENV2}.toUpper")).as("Unexpected replacement").isEqualTo("|aA.toUpper");
    }
    
    @Test
    public void retrieveDataExceptionTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String baseData = "invalid.conf";
	String type = "string";
	
	try {
	    commong.retrieveData(baseData, type);
	    org.testng.Assert.fail("Expected Exception");
	} catch (Exception e) {
	    assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(NullPointerException.class.toString());
	}
    }
    
    @Test
    public void retrieveDataStringTest() throws IOException {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String baseData = "retrieveDataStringTest.conf";
	String type = "string";
	
	String returnedData = commong.retrieveData(baseData, type);
	assertThat(returnedData).as("Invalid information read").isEqualTo("username=username&password=password");
    }
    
    @Test
    public void retrieveDataInvalidJsonTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String baseData = "retrieveDataInvalidJsonTest.conf";
	String type = "json";
	
	try {
	    commong.retrieveData(baseData, type);
	    org.testng.Assert.fail("Expected ParseException");
	} catch (Exception e) {
	    assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(ParseException.class.toString());
	}
    }
    
    @Test
    public void retrieveDataValidJsonTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String baseData = "retrieveDataValidJsonTest.conf";
	String type = "json";
    
	String returnedData = commong.retrieveData(baseData, type);
	assertThat(returnedData).as("Invalid information read").isEqualTo("{\"key1\":\"value1\",\"key2\":{\"key3\":\"value3\"}}");
    }
    
    @Test
    public void modifyDataInvalidModificationTypeStringTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "username=username&password=password";
	String type = "string";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("username=username", "REMOVE", "N/A")); 
	DataTable modifications = DataTable.create(rawData);
	
	try {
	    commong.modifyData(data, type, modifications);
	    org.testng.Assert.fail("Expected Exception");
	} catch (Exception e) {
	    assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
	    assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("Modification type does not exist: REMOVE");
	}
    }
    
    @Test
    public void modifyDataInvalidModificationTypeJsonTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "{\"key1\": \"value1\", \"key2\": {\"key3\": \"value3\"}}";
	String type = "json";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("username=username", "REMOVE", "N/A")); 
	DataTable modifications = DataTable.create(rawData);
	
	try {
	    commong.modifyData(data, type, modifications);
	    org.testng.Assert.fail("Expected Exception");
	} catch (Exception e) {
	    assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
	    assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("Modification type does not exist: REMOVE");
	}
    }
    
    @Test
    public void modifyDataDeleteStringTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "username=username&password=password";
	String expectedData = "password=password";
	String type = "string";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("username=username&", "DELETE", "N/A")); 
	DataTable modifications = DataTable.create(rawData);
	
	String modifiedData = commong.modifyData(data, type, modifications);
	assertThat(modifiedData).as("Unexpected modified data").isEqualTo(expectedData);		
    }
    
    @Test
    public void modifyDataAddStringTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "username=username&password=password";
	String expectedData = "username=username&password=password&config=config";
	String type = "string";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("N/A", "ADD", "&config=config")); 
	DataTable modifications = DataTable.create(rawData);
	
	String modifiedData = commong.modifyData(data, type, modifications);
	assertThat(modifiedData).as("Unexpected modified data").isEqualTo(expectedData);
    }
    
    @Test
    public void modifyDataUpdateStringTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "username=username&password=password";
	String expectedData = "username=NEWusername&password=password";
	String type = "string";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("username=username", "UPDATE", "username=NEWusername")); 
	DataTable modifications = DataTable.create(rawData);
	
	String modifiedData = commong.modifyData(data, type, modifications);
	assertThat(modifiedData).as("Unexpected modified data").isEqualTo(expectedData);	
    }
    
    @Test
    public void modifyDataDeleteJsonTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "{\"key1\": \"value1\", \"key2\": {\"key3\": \"value3\"}}";
	String expectedData = "{\"key2\":{\"key3\":\"value3\"}}";
	String type = "json";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "DELETE", "N/A")); 
	DataTable modifications = DataTable.create(rawData);
	
	String modifiedData = commong.modifyData(data, type, modifications);
	assertThat(modifiedData).as("Unexpected modified data").isEqualTo(expectedData);
    }
    
    @Test
    public void modifyDataAddJsonTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "{\"key1\": \"value1\", \"key2\": {\"key3\": \"value3\"}}";
	String expectedData = "{\"key2\":{\"key4\":\"value4\",\"key3\":\"value3\"},\"key1\":\"value1\"}";
	String type = "json";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("$.key2.key4", "ADD", "value4")); 
	DataTable modifications = DataTable.create(rawData);
	
	String modifiedData = commong.modifyData(data, type, modifications);
	assertThat(modifiedData).as("Unexpected modified data").isEqualTo(expectedData);
    }
    
    @Test
    public void modifyDataUpdateJsonTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String data = "{\"key1\": \"value1\", \"key2\": {\"key3\": \"value3\"}}";
	String expectedData = "{\"key2\":{\"key3\":\"NEWvalue3\"},\"key1\":\"value1\"}";
	String type = "json";
	List<List<String>> rawData = Arrays.asList(Arrays.asList("key2.key3", "UPDATE", "NEWvalue3")); 
	DataTable modifications = DataTable.create(rawData);
	
	String modifiedData = commong.modifyData(data, type, modifications);
	assertThat(modifiedData).as("Unexpected modified data").isEqualTo(expectedData);
    }
    
    @Test
    public void generateRequestNoAppURLTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String requestType = "MYREQUEST";
	String endPoint = "endpoint";
	String data = "data";
	String type = "string";
	
	try {
	    commong.generateRequest(requestType, endPoint, data, type);
	    org.testng.Assert.fail("Expected Exception");
	} catch (Exception e) {
	    assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
	    assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("Application URL has not been set");
	}
    }
    
    @Test
    public void generateRequestInvalidRequestTypeTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String requestType = "MYREQUEST";
	String endPoint = "endpoint";
	String data = "data";
	String type = "string";
	
	try {
	    commong.setURL("http://localhost:80");
	    commong.generateRequest(requestType, endPoint, data, type);
	    org.testng.Assert.fail("Expected Exception");
	} catch (Exception e) {
	    assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
	    assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("Operation not valid: MYREQUEST");
	}
    }
    
    @Test
    public void generateRequestNotImplementedRequestTypeTest() throws Exception {
	ThreadProperty.set("class", this.getClass().getCanonicalName());
	CommonG commong = new CommonG();
	String requestType = "TRACE";
	String endPoint = "endpoint";
	String data = "data";
	String type = "string";
	
	try {
	    commong.setURL("http://localhost:80");
	    commong.generateRequest(requestType, endPoint, data, type);
	    org.testng.Assert.fail("Expected Exception");
	} catch (Exception e) {
	    assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
	    assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("Operation not implemented: TRACE");
	}
    }
   
}
