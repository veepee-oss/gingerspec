package com.stratio.specs;

import static com.stratio.assertions.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import com.ning.http.client.Response;
import com.stratio.cucumber.converter.ArrayListConverter;

import cucumber.api.DataTable;
import cucumber.api.Transform;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;

public class WhenGSpec extends BaseGSpec {

    public static final int DEFAULT_TIMEOUT = 1000;

    /**
     * Default constructor.
     * 
     * @param spec
     */
    public WhenGSpec(CommonG spec) {
        this.commonspec = spec;
    }

    /**
     * Wait seconds.
     * 
     * @param seconds
     * @throws InterruptedException
     */
    @When("^I wait '(\\d+?)' seconds?$")
    public void idleWait(Integer seconds) throws InterruptedException {
        commonspec.getLogger().info("Idling a while");
        Thread.sleep(seconds * DEFAULT_TIMEOUT);
    }

    /**
     * Searchs for two webelements dragging the first one to the second
     * 
     * @param source
     * @param destination
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws ClassNotFoundException 
     */
    @When("^I drag '([^:]*?):([^:]*?)' and drop it to '([^:]*?):([^:]*?)'$")
    public void seleniumDrag(String smethod, String source, String dmethod, String destination) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        commonspec.getLogger().info("Dragging element");

        Actions builder = new Actions(commonspec.getDriver());

        List<WebElement> sourceElement = commonspec.locateElement(smethod, source, 1);
        List<WebElement> destinationElement = commonspec.locateElement(dmethod, destination, 1);

        builder.dragAndDrop(sourceElement.get(0), destinationElement.get(0)).perform();
    }

    /**
     * Click on an numbered {@code url} previously found element.
     * 
     * @param index
     */
    @When("^I click on the element on index '(\\d+?)'$")
    public void seleniumClick(Integer index) {
        commonspec.getLogger().info("Clicking on element with index {}", index);

        assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
        	.hasAtLeast(index);
        commonspec.getPreviousWebElements().getPreviousWebElements().get(index).click();
    }
  
    /**
     * Clear the text on a numbered {@code index} previously found element.
     * 
     * @param index
     */
    @When("^I clear the content on text input at index '(\\d+?)'$")
    public void seleniumClear(Integer index) {
	commonspec.getLogger().info("Clearing text on element with index {}", index);
	
	assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
        	.hasAtLeast(index);
	
	assertThat(this.commonspec, commonspec.getPreviousWebElements().getPreviousWebElements().get(index)).isTextField(commonspec.getTextFieldCondition());
	
	commonspec.getPreviousWebElements().getPreviousWebElements().get(index).clear();	
    }
    
    
    /**
     * Type a {@code text} on an numbered {@code index} previously found element.
     * 
     * @param text
     * @param index
     */
    @When("^I type '(.+?)' on the element on index '(\\d+?)'$")
    public void seleniumType(String text, Integer index) {
        commonspec.getLogger().info("Typing on element with index {}", index);

        assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
		.hasAtLeast(index);
        while (text.length() > 0) {
            if (-1 == text.indexOf("\\n")) {
                commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(text);
                text = "";
            } else {
                commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(text.substring(0, text.indexOf("\\n")));
                commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(Keys.ENTER);
                text = text.substring(text.indexOf("\\n") + 2);
            }
        }
    }

    /**
     * Send a {@code strokes} list on an numbered {@code url} previously found element or to the driver. strokes examples are "HOME, END"
     * or "END, SHIFT + HOME, DELETE". Each element in the stroke list has to be an element from
     * {@link org.openqa.selenium.Keys} (NULL, CANCEL, HELP, BACK_SPACE, TAB, CLEAR, RETURN, ENTER, SHIFT, LEFT_SHIFT,
     * CONTROL, LEFT_CONTROL, ALT, LEFT_ALT, PAUSE, ESCAPE, SPACE, PAGE_UP, PAGE_DOWN, END, HOME, LEFT, ARROW_LEFT, UP,
     * ARROW_UP, RIGHT, ARROW_RIGHT, DOWN, ARROW_DOWN, INSERT, DELETE, SEMICOLON, EQUALS, NUMPAD0, NUMPAD1, NUMPAD2,
     * NUMPAD3, NUMPAD4, NUMPAD5, NUMPAD6, NUMPAD7, NUMPAD8, NUMPAD9, MULTIPLY, ADD, SEPARATOR, SUBTRACT, DECIMAL,
     * DIVIDE, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, META, COMMAND, ZENKAKU_HANKAKU) , a plus sign (+), a
     * comma (,) or spaces ( )
     * 
     * @param strokes
     * @param foo
     * @param index
     */
    @When("^I send '(.+?)'( on the element on index '(\\d+?)')?$")
    public void seleniumKeys(@Transform(ArrayListConverter.class) List<String> strokes, String foo, Integer index) {
	if (index == null) {
	    commonspec.getLogger().info("Sending keys to driver");
	} else {
	    commonspec.getLogger().info("Sending keys on element with index {}", index);

	    assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
	    	.hasAtLeast(index);
	}
	assertThat(strokes).isNotEmpty();
	
	for (String stroke : strokes) {
	    if (stroke.contains("+")) {
		List<Keys> csl = new ArrayList<Keys>();
		for (String strokeInChord : stroke.split("\\+")) {
		    csl.add(Keys.valueOf(strokeInChord.trim()));
		}                                
		Keys[] csa = csl.toArray(new Keys[csl.size()]);
		if (index == null) {
		    new Actions(commonspec.getDriver()).sendKeys(commonspec.getDriver().findElement(By.tagName("body")), csa).perform();
		} else {
		    commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(csa);
		}
	    } else {
		if (index == null) {
		    new Actions(commonspec.getDriver()).sendKeys(commonspec.getDriver().findElement(By.tagName("body")), Keys.valueOf(stroke)).perform();
		} else {
		    commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(Keys.valueOf(stroke));
		}
	    }
	}
    }
    
    /**
     * Choose an @{code option} from a select webelement found previously
     * 
     * @param option
     * @param index
     */
    @When("^I select '(.+?)' on the element on index '(\\d+?)'$")
    public void elementSelect(String option, Integer index) {
        commonspec.getLogger().info("Choosing option on select");

        Select sel = null;
        sel = new Select(commonspec.getPreviousWebElements().getPreviousWebElements().get(index));

        sel.selectByVisibleText(option);
    }

    /**
     * Choose no option from a select webelement found previously
     * 
     * @param index
     */
    @When("^I de-select every item on the element on index '(\\d+?)'$")
    public void elementDeSelect(Integer index) {
        commonspec.getLogger().info("Unselecting everything");

        Select sel = null;
        sel = new Select(commonspec.getPreviousWebElements().getPreviousWebElements().get(index));

        if (sel.isMultiple()) {
            sel.deselectAll();
        }
    }
    
    /**
     * Send a request of the type specified 
     * 
     * @param requestType type of request to be sent. Possible values:
     * GET|DELETE|POST|PUT|CONNECT|PATCH|HEAD|OPTIONS|REQUEST|TRACE
     * @param endPoint end point to be used
     * @param foo parameter generated by cucumber because of the optional expression
     * @param baseData path to file containing the schema to be used
     * @param element element to read from file (element should contain a json)
     * @param modifications DataTable containing the modifications to be done to the
     * base schema element. Syntax will be:
     * 		| <key path> | <type of modification> | <new value> |
     * where:
     *     key path: path to the key to be modified
     *     type of modification: DELETE|ADD|UPDATE
     *     new value: in case of UPDATE or ADD, new value to be used 
     * for example:
     * if the element read is {"key1": "value1", "key2": {"key3": "value3"}}
     * and we want to modify the value in "key3" with "new value3"
     * the modification will be:
     *  	| key2.key3 | UPDATE | "new value3" |
     * being the result of the modification: {"key1": "value1", "key2": {"key3": "new value3"}}
     * @throws Exception 
     */
    @When("^I send a '(.+?)' request to '(.+?)' based on '([^:]+?)'( as '(json|string)')? with:$")
    public void sendRequest(String requestType, String endPoint, String baseData, String foo, String type, DataTable modifications) throws Exception {
	// Retrieve data
	commonspec.getLogger().info("Retrieving data based on {} as {}", baseData, type);
	String retrievedData = commonspec.retrieveData(baseData, type);

	// Modify data
	commonspec.getLogger().info("Modifying data {} as {}", retrievedData, type);
	String modifiedData;
	modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();

	commonspec.getLogger().info("Generating request {} to {} with data {} as {}", requestType, endPoint, modifiedData, type);
	Future<Response> response = commonspec.generateRequest(requestType, endPoint, modifiedData, type);
			
	// Save response
	commonspec.getLogger().info("Saving response");
	commonspec.setResponse(requestType, response.get());
    }

    /**
     * Same sendRequest, but in this case, we do not receive a data table with modifications.
     * Besides, the data and request header are optional as well.
     * In case we want to simulate sending a json request with empty data, we just to avoid baseData
     * 
     * @param requestType
     * @param endPoint
     * @param foo
     * @param baseData
     * @param bar
     * @param type
     * 
     * @throws Exception
     */
    @When("^I send a '(.+?)' request to '(.+?)'( based on '([^:]+?)')?( as '(json|string)')?$")
    public void sendRequestNoDataTable (String requestType, String endPoint, String foo, String baseData, String bar, String type) throws Exception {
	Future<Response> response;
	
	if (baseData != null) {
	    // Retrieve data
	    String retrievedData = commonspec.retrieveData(baseData, type);
	    // Generate request
	    response = commonspec.generateRequest(requestType, endPoint, retrievedData, type);
	} else {
	    // Generate request
	    response = commonspec.generateRequest(requestType, endPoint, "", type);
	}
			
	// Save response
	commonspec.setResponse(requestType, response.get());
    }
    
    @When("^I attempt a login to '(.+?)' based on '([^:]+?)' as '(json|string)'$")
    public void loginUser(String endPoint, String baseData, String type) throws Exception {
	sendRequestNoDataTable("POST", endPoint, null, baseData, null, type);
    }
    
    @When("^I attempt a login to '(.+?)' based on '([^:]+?)' as '(json|string)' with:$")
    public void loginUser(String endPoint, String baseData, String type, DataTable modifications) throws Exception {
	sendRequest("POST", endPoint, baseData, "", type, modifications);
    }
    
    @When("^I attempt a logout to '(.+?)'$")
    public void logoutUser(String endPoint) throws Exception {
	sendRequestNoDataTable("GET", endPoint, null, "", null, "");
    }
    
    /**
     * Execute a query with schema over a cluster
     * 
     * @param fields: columns on which the query is executed. Example: "latitude,longitude" or "*" or "count(*)"
     * @param schema: the file of configuration (.conf) with the options of mappin. If schema is the word "empty", method will not add a where clause.
     * @param type: type of the changes in schema (string or json)
     * @param table: table for create the index
     * @param magic_column: magic column where index will be saved. If you don't need index, you can add the word "empty"
     * @param keyspace: keyspace used
     * @param modifications: all data in "where" clause. Where schema is "empty", query has not a where clause. So it is necessary to provide an empty table. Example:  ||.
     * @throws Exception
     */
    @When("^I execute a query over fields '(.+?)' with schema '(.+?)' of type '(json|string)' with magic_column '(.+?)' from table: '(.+?)' using keyspace: '(.+?)' with:$")
    public void sendQueryOfType( String fields, String schema, String type, String magic_column, String table, String keyspace, DataTable modifications){
        try {
        commonspec.setResultsType("cassandra");
        commonspec.getCassandraClient().useKeyspace(keyspace);  
        commonspec.getLogger().info("Starting a query of type "+commonspec.getResultsType());
       
        String query="";
    
        if(schema.equals("empty") && magic_column.equals("empty")){
            
         query="SELECT "+fields+" FROM "+ table +";";
         
        }else if(!schema.equals("empty") && magic_column.equals("empty")){
            String retrievedData = commonspec.retrieveData(schema, type);
            String modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();
            query="SELECT "+fields+" FROM "+ table +" WHERE "+modifiedData+";";


        }
        else{
            String retrievedData = commonspec.retrieveData(schema, type);
            String modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();
            query="SELECT " + fields + " FROM "+ table +" WHERE "+ magic_column +" = '"+ modifiedData +"';";

        }
        commonspec.getLogger().info("query: "+query);
        commonspec.setResults(commonspec.getCassandraClient().executeQuery(query));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            commonspec.getLogger().info("Exception captured");
            commonspec.getLogger().info(e.toString());
            commonspec.getExceptions().add(e);
        }


    }
    
    /**
     * Create a Cassandra index.
     * 
     * @param index_name: index name
     * @param schema: the file of configuration (.conf) with the options of mappin
     * @param type: type of the changes in schema (string or json)
     * @param table: table for create the index
     * @param magic_column: magic column where index will be saved
     * @param keyspace: keyspace used
     * @param modifications: data introduced for query fields defined on schema
     * 
     * 
     */
    @When("^I create a Cassandra index named '(.+?)' with schema '(.+?)' of type '(json|string)' in table '(.+?)' using magic_column '(.+?)' using keyspace '(.+?)' with:$")
    public void createCustomMapping(String index_name, String schema, String type, String table, String magic_column, String keyspace, DataTable modifications) throws Exception {
        commonspec.getLogger().info("Creating a custom mapping");
        String retrievedData = commonspec.retrieveData(schema, type);
        String modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();
        String query="CREATE CUSTOM INDEX "+ index_name +" ON "+ keyspace +"."+ table +"("+ magic_column +") "
                + "USING 'com.stratio.cassandra.lucene.Index' WITH OPTIONS = "+ modifiedData;
        System.out.println(query);
        commonspec.getCassandraClient().executeQuery(query);
    }
    
    /**
     * Drop table
     * 
     * @param table
     * @param keyspace
     *  
     */
    @When("^I drop a Cassandra table named '(.+?)' using keyspace '(.+?)'$")
    public void dropTableWithData(String table, String keyspace){
        try{
        commonspec.getCassandraClient().useKeyspace(keyspace);        
        commonspec.getLogger().info("Starting a table deletion");
          commonspec.getCassandraClient().dropTable(table);
        }catch (Exception e) {
            // TODO Auto-generated catch block
            commonspec.getLogger().info("Exception captured");
            commonspec.getLogger().info(e.toString());
            commonspec.getExceptions().add(e);
        }
        }

    /**
     * Truncate table
     * 
     * @param table
     * @param keyspace
     *  
     */
    @When("^I truncate a Cassandra table named '(.+?)' using keyspace '(.+?)'$")
    public void truncateTable(String table, String keyspace){
        try{
        commonspec.getCassandraClient().useKeyspace(keyspace);        
        commonspec.getLogger().info("Starting a table truncation");
          commonspec.getCassandraClient().truncateTable(table);
        }catch (Exception e) {
            // TODO Auto-generated catch block
            commonspec.getLogger().info("Exception captured");
            commonspec.getLogger().info(e.toString());
            commonspec.getExceptions().add(e);
        }
        }
    
}
