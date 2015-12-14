package com.stratio.specs;

import static com.stratio.assertions.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hjson.JsonValue;
import org.openqa.selenium.WebElement;

import com.datastax.driver.core.Row;
import com.jayway.jsonpath.JsonPath;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;

/**
 * Generic Given Specs.
 * 
 */
public class GivenGSpec extends BaseGSpec {

    public static final int PAGE_LOAD_TIMEOUT = 120;
    public static final int IMPLICITLY_WAIT = 10;
    public static final int SCRIPT_TIMEOUT = 30;

    /**
     * Generic constructor.
     * 
     * @param spec
     */
    public GivenGSpec(CommonG spec) {
        this.commonspec = spec;

    }

    /**
     * Create a Mapping.
     * 
     * @param index_name: index name
     * @param scheme: the file of configuration (.conf) with the options of mappin
     * @param type: type of the changes in scheme (string or json)
     * @param table: table for create the index
     * @param magic_column: magic column where index will be saved
     * @param keyspace: keyspace used
     * @param modifications: data introduced for query fields defined on scheme
     * @throws Exception 
     * 
     */
    @Given("^I create a map with index name '(.+?)' with scheme '(.+?)' of type '(.+?)' in table '(.+?)' using magic_column '(.+?)' using keyspace '(.+?)' with:$")
    public void createCustomMapping(String index_name, String scheme, String type, String table, String magic_column, String keyspace, DataTable modifications) throws Exception {
        commonspec.getLogger().info("Creating a custom mapping", "");
        String retrievedData = commonspec.retrieveData(scheme, type);
        String modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();
        String query="CREATE CUSTOM INDEX "+index_name+" ON "+keyspace+"."+table+"("+magic_column+") "
                + "USING 'com.stratio.cassandra.lucene.Index' WITH OPTIONS = "+modifiedData;
        commonspec.getCassandraClient().executeQuery(query);
    }



    /**
     * Create a basic Index.
     * 
     * @param index_name: index name
     * @param table: the table where index will be created.
     * @param column: the column where index will be saved
     * @param keyspace: keyspace used
     * @throws Exception 
     * 
     */
    @Given("^I create a map with index name '(.+?)' in table '(.+?)' using magic_column '(.+?)' using keyspace '(.+?)'$")
    public void createBasicMapping(String index_name, String table, String column, String keyspace) throws Exception {
        commonspec.getLogger().info("Creating a basic index", "");
        String query="CREATE INDEX "+index_name+" ON "+table+" ("+column+");";
        commonspec.getCassandraClient().executeQuery(query);
    }

    /**
     * Create a Cassandra Keyspace.
     * 
     * @param keyspace
     */
    @Given("^I create a Cassandra keyspace named '(.+)'$")
    public void createCassandraKeyspace(String keyspace) {
        commonspec.getLogger().info("Creating a Cassandra keyspace", "");
        commonspec.getCassandraClient().createKeyspace(keyspace);
    }
    /**
     * Connect to cluster.
     * 
     * @param node: number of nodes
     * @param url: url where is started Cassandra cluster
     */
    @Given("^I connect to Cassandra cluster with '(.+)' nodes and this url '(.+)'$")
    public void connect(String node, String url) {
        System.setProperty("CASSANDRA_HOST", url);
        commonspec.getLogger().info("Connecting to cluster", "");
        commonspec.getCassandraClient().buildCluster();
        commonspec.getCassandraClient().connect();
    }


    /**
     * Execute a query with scheme over a cluster
     * 
     * @param scheme: the file of configuration (.conf) with the options of mappin
     * @param type: type of the changes in scheme (string or json)
     * @param table: table for create the index
     * @param magic_column: magic column where index will be saved
     * @param keyspace: keyspace used
     * @param modifications: query fields on scheme
     * @throws Exception
     */
    @Given("^I send a query with scheme '(.+?)' of type '(.+?)' with magic_column '(.+?)' from table: '(.+?)' using keyspace: '(.+?)' with:$")
    public void sendQueryOfType(String scheme, String type, String magic_column, String table, String keyspace, DataTable modifications) throws Exception {
        commonspec.getCassandraClient().useKeyspace(keyspace);  
        commonspec.getLogger().info("Starting a query of type ", "");
        String retrievedData = commonspec.retrieveData(scheme, type);
        String modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();
        String query="SELECT * FROM "+table+" WHERE "+magic_column+" = '"+modifiedData+"';";
        System.out.println("query: "+query);
        commonspec.setResults(commonspec.getCassandraClient().executeQuery(query));



    }

    /**
     * Checks the number of results after a query execution
     * 
     * @param resultNumber: number of rows obtained after a query execution
     * @throws Exception
     */

    @Given("^There are '(.+?)' results after executing the last query$")
    public void resultsMustBe(String resultNumber) throws Exception {
        if(commonspec.getResults()!=null){
            List<Row> rows = commonspec.getResults().all();
            assertThat(Integer.parseInt(resultNumber)).isEqualTo(rows.size())
            .overridingErrorMessage(resultNumber+" results were expected and "
            +rows.size()+" were found");
        }else{
            throw new Exception("You must send a query after get results");
        }
    }


    /**
     * Create table
     * 
     * @param table
     * @param datatable
     * @param keyspace
     * @throws Exception 
     */
    @Given("^I create table named: '(.+?)' using keyspace: '(.+?)' with:$")
    public void createTableWithData(String table, String keyspace, DataTable datatable) throws Exception {

        commonspec.getCassandraClient().useKeyspace(keyspace);        
        commonspec.getLogger().info("Starting a table creation", "");
        int attrLength=datatable.getGherkinRows().get(0).getCells().size();
        Map<String,String> columns =  new HashMap<String,String>();
        ArrayList<String> pk=new ArrayList<String>();

        for(int i=0; i<attrLength; i++){
            columns.put(datatable.getGherkinRows().get(0).getCells().get(i), 
            datatable.getGherkinRows().get(1).getCells().get(i));    
            if(datatable.getGherkinRows().get(2).getCells().get(i).equalsIgnoreCase("PK")){
                pk.add(datatable.getGherkinRows().get(0).getCells().get(i));
            }
        } 
        if(pk.isEmpty()){
            throw new Exception("A PK is needed");
        }
        commonspec.getCassandraClient().createTableWithData(table, columns, pk);
    }

    /**
     * Insert Data
     * 
     * @param table
     * @param datatable
     * @param keyspace
     * @throws Exception 
     */
    @Given("^I insert in keyspace '(.+?)' and table '(.+?)' with:$")
    public void insertData(String keyspace, String table, DataTable datatable) throws Exception {

        commonspec.getCassandraClient().useKeyspace(keyspace);        
        commonspec.getLogger().info("Starting a table creation", "");
        int attrLength=datatable.getGherkinRows().get(0).getCells().size();
        Map<String, Object> fields =  new HashMap<String,Object>();
        for(int e=1; e<datatable.getGherkinRows().size();e++){
            for(int i=0; i<attrLength; i++){
                fields.put(datatable.getGherkinRows().get(0).getCells().get(i), datatable.getGherkinRows().get(e).getCells().get(i));    

            }
            commonspec.getCassandraClient().insertData(keyspace+"."+table, fields);

        }
    }


    /**
     * Save value for future use
     * 
     * @param element key in the json response to be saved (i.e. $.fragments[0].id)
     * @param attribute STATIC attribute in the class where to store the value
     * 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws ClassNotFoundException 
     * @throws InstantiationException 
     * @throws InvocationTargetException 
     * @throws NoSuchMethodException 
     */
    @Given("^I save element '(.+?)' in attribute '(.+?)'$")
    public void saveElement(String element, String attribute) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        commonspec.getLogger().info("Saving element: {} in attribute: {}", element, attribute);

        String json = commonspec.getResponse().getResponse();
        String hjson = JsonValue.readHjson(json).asObject().toString();
        String value = JsonPath.parse(hjson).read(element);

        commonspec.setPreviousElement(attribute, value);
    }

    /**
     * Empty all the indexes of ElasticSearch.
     */
    @Given("^I empty every existing elasticsearch index$")
    public void emptyElasticsearchIndexes() {
        commonspec.getLogger().info("Emptying es indexes");
        commonspec.getElasticSearchClient().emptyIndexes();
    }


    /**
     * Empty a specific index of ElasticSearch.
     * 
     * @param index
     */
    @Given("^I empty an elasticsearch index named '(.+?)'$")
    public void emptyElasticsearchIndex(String index) {
        commonspec.getLogger().info("Emptying an es index: {}", index);
        commonspec.getElasticSearchClient().emptyIndex(index);
    }

    /**
     * Drop all the ElasticSearch indexes.
     */
    @Given("^I drop every existing elasticsearch index$")
    public void dropElasticsearchIndexes() {
        commonspec.getLogger().info("Dropping es indexes");
        commonspec.getElasticSearchClient().dropIndexes();
    }

    /**
     * Drop an specific index of ElasticSearch.
     * 
     * @param index
     */
    @Given("^I drop an elasticsearch index named '(.+?)'$")
    public void dropElasticsearchIndex(String index) {
        commonspec.getLogger().info("Dropping an es index: {}", index);
        commonspec.getElasticSearchClient().dropIndex(index);
    }

    /**
     * Execute a cql file over a Cassandra keyspace.
     * 
     * @param filename
     * @param keyspace
     */
    @Given("a Cassandra script with name '(.+?)' and default keyspace '(.+?)'$")
    public void insertDataOnCassandraFromFile(String filename, String keyspace) {
        commonspec.getLogger().info("Inserting data on cassandra from file");
        commonspec.getCassandraClient().loadTestData(keyspace, "/scripts/" + filename);
    }

    /**
     * Drop a Cassandra Keyspace.
     * 
     * @param keyspace
     */
    @Given("^I drop a Cassandra keyspace '(.+)'$")
    public void dropCassandraKeyspace(String keyspace) {
        commonspec.getLogger().info("Dropping a Cassandra keyspace", keyspace);
        commonspec.getCassandraClient().dropKeyspace(keyspace);
    }


    /**
     * Create a AeroSpike namespace, table and the data of the table.
     * 
     * @param nameSpace
     * @param tableName
     * @param tab
     */
    @Given("^I create an AeroSpike namespace '(.+?)' with table '(.+?)':$")
    public void createAeroSpikeTable(String nameSpace, String tableName, DataTable tab) {
        commonspec.getLogger().info("Creating a table on AeroSpike");
        if (commonspec.getAerospikeClient().isConnected()) {
            commonspec.getLogger().info("Creating a table on AeroSpike");
        }
        commonspec.getAerospikeClient().insertFromDataTable(nameSpace, tableName, tab);
    }

    /**
     * Create a MongoDB dataBase.
     * 
     * @param databaseName
     */
    @Given("^I create a MongoDB dataBase '(.+?)'$")
    public void createMongoDBDataBase(String databaseName) {
        commonspec.getLogger().info("Creating a database on MongoDB");
        commonspec.getMongoDBClient().connectToMongoDBDataBase(databaseName);

    }

    /**
     * Drop MongoDB Database.
     * 
     * @param databaseName
     */
    @Given("^I drop a MongoDB database '(.+?)'$")
    public void dropMongoDBDataBase(String databaseName) {
        commonspec.getLogger().info("Creating a database on MongoDB");
        commonspec.getMongoDBClient().dropMongoDBDataBase(databaseName);
    }

    /**
     * Insert data in a MongoDB table.
     * 
     * @param dataBase
     * @param tabName
     * @param table
     */
    @Given("^I insert into a MongoDB database '(.+?)' and table '(.+?)' this values:$")
    public void insertOnMongoTable(String dataBase, String tabName, DataTable table) {
        commonspec.getLogger().info("Inserting data in a database on MongoDB");
        commonspec.getMongoDBClient().connectToMongoDBDataBase(dataBase);
        commonspec.getMongoDBClient().insertIntoMongoDBCollection(tabName, table);
    }

    /**
     * Truncate table in MongoDB.
     * 
     * @param database
     * @param table
     */
    @Given("^I drop every document at a MongoDB database '(.+?)' and table '(.+?)'")
    public void truncateTableInMongo(String database, String table) {
        commonspec.getLogger().info("Truncating a table in MongoDB");
        commonspec.getMongoDBClient().connectToMongoDBDataBase(database);
        commonspec.getMongoDBClient().dropAllDataMongoDBCollection(table);
    }

    /**
     * Browse to {@code url} using the current browser.
     * 
     * @param url
     * @throws Exception 
     */
    @Given("^I browse to '(.+?)'$")
    public void seleniumBrowse(String path) throws Exception {
        assertThat(path).isNotEmpty();

        if (commonspec.getWebHost() == null) {
            throw new Exception("Web host has not been set");
        }

        if (commonspec.getWebPort() == null) {
            throw new Exception("Web port has not been set");
        }

        String webURL = "http://" + commonspec.getWebHost() + commonspec.getWebPort();	

        commonspec.getLogger().info("Browsing to {}{} with {}", webURL, path, commonspec.getBrowserName());
        commonspec.getDriver().get(webURL + path);
        commonspec.setParentWindow(commonspec.getDriver().getWindowHandle());
    }

    /**
     * Set app host, port and url {@code host, @code port}
     * 
     * @param host
     * @param port
     * 
     */
    @Given("^My app is running in '([^:]+?)(:.+?)?'$")
    public void setupApp(String host, String port) {
        assertThat(host).isNotEmpty();
        assertThat(port).isNotEmpty();

        if (port == null) {
            port = ":80";
        }

        commonspec.setWebHost(host);
        commonspec.setWebPort(port);
        commonspec.setRestHost(host);
        commonspec.setRestPort(port);

        commonspec.getLogger().info("Set URL to http://{}{}/", host, port);
    }


    /**
     * Browse to {@code webHost, @code webPort} using the current browser.
     *
     * @param url
     * @throws MalformedURLException 
     */
    @Given("^I set web base url to '([^:]+?)(:.+?)?'$")
    public void setupWeb(String webHost, String webPort) throws MalformedURLException {
        assertThat(webHost).isNotEmpty();
        assertThat(webPort).isNotEmpty();

        if (webPort == null) {
            webPort = ":80";
        }

        commonspec.setWebHost(webHost);
        commonspec.setWebPort(webPort);

        commonspec.getLogger().info("Set web base URL to http://{}{}", webHost, webPort);  
    }

    /**
     * Send requests to {@code restHost @code restPort}.
     * 
     * @param restHost
     * @param restPort
     */
    @Given("^I send requests to '([^:]+?)(:.+?)?'$")
    public void setupRestClient(String restHost, String restPort) {
        assertThat(restHost).isNotEmpty();
        assertThat(restPort).isNotEmpty();

        if (restHost == null) {
            restHost = "localhost";
        }

        if (restPort == null) {
            restPort = ":80";
        }

        commonspec.setRestHost(restHost);
        commonspec.setRestPort(restPort);
        commonspec.getLogger().info("Sending requests to http://{}{}", restHost, restPort);
    }

    /**
     * Maximizes current browser window. Mind the current resolution could break a test.
     * 
     */
    @Given("^I maximize the browser$")
    public void seleniumMaximize(String url) {
        commonspec.getDriver().manage().window().maximize();
    }

    /**
     * Switches to a frame/ iframe.
     * 
     */
    @Given("^I switch to the iframe on index '(\\d+?)' $")
    public void seleniumSwitchFrame(Integer index) {

        assertThat(commonspec.getPreviousWebElements()).as("There are less found elements than required")
        .hasAtLeast(index);

        WebElement elem = commonspec.getPreviousWebElements().getPreviousWebElements().get(index);
        commonspec.getDriver().switchTo().frame(elem);
    }

    /**
     * Switches to a parent frame/ iframe.
     * 
     */
    @Given("^I switch to a parent frame$")
    public void seleniumSwitchAParentFrame() {
        commonspec.getDriver().switchTo().parentFrame();
    }

    /**
     * Switches to the frames main container.
     * 
     */
    @Given("^I switch to the main frame container$")
    public void seleniumSwitchParentFrame() {
        commonspec.getDriver().switchTo().frame(commonspec.getParentWindow());
    }
}