package com.stratio.specs;

import static org.assertj.core.api.Assertions.assertThat;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;

/**
 * Generic Given Specs.
 * 
 * @author Hugo Dominguez
 * @author Javier Delgado
 * 
 */
public class GivenGSpec extends BaseGSpec {

    /**
     * Generic constructor.
     * 
     * @param spec
     */
    public GivenGSpec(CommonG spec) {
        this.commonspec = spec;
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
    @Given("^I empty an elasticsearch index named '(.*?)'$")
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
    @Given("^I drop an elasticsearch index named '(.*?)'$")
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
    @Given("a C* script with name '(.*?)' and default keyspace '(.*?)'$")
    public void insertDataOnCassandraFromFile(String filename, String keyspace) {
        commonspec.getLogger().info("Inserting data on cassandra from file");
        commonspec.getCassandraClient().loadTestData(keyspace, "/scripts/" + filename);
    }

    /**
     * Drop a Cassandra Keyspace.
     * 
     * @param keyspace
     */
    @Given("^I drop an C* keyspace '(.*?)'$")
    public void dropCassandraKeyspace(String keyspace) {
        commonspec.getLogger().info("Dropping a C* keyspace", keyspace);
        commonspec.getCassandraClient().dropKeyspace(keyspace);
    }

    /**
     * Create a AeroSpike namespace, table and the data of the table.
     * 
     * @param nameSpace
     * @param tableName
     * @param tab
     */
    @Given("^I create an AeroSpike namespace '(.*?)' with table '(.*?)':$")
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
    @Given("^I create a MongoDB dataBase '(.*?)'$")
    public void createMongoDBDataBase(String databaseName) {
        commonspec.getLogger().info("Creating a database on MongoDB");
        commonspec.getMongoDBClient().connectToMongoDBDataBase(databaseName);

    }

    /**
     * Drop MongoDB Database.
     * 
     * @param databaseName
     */
    @Given("^I drop a MongoDB database '(.*?)'$")
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
    @Given("^I insert into a MongoDB database '(.*?)' and table '(.*?)' this values:$")
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
    @Given("^I drop every document at a MongoDB database '(.*?)' and table '(.*?)'")
    public void truncateTableInMongo(String database, String table) {
        commonspec.getLogger().info("Truncating a table in MongoDB");
        commonspec.getMongoDBClient().connectToMongoDBDataBase(database);
        commonspec.getMongoDBClient().dropAllDataMongoDBCollection(table);
    }

    /**
     * Browse to {@code url} using the current browser.
     * 
     * @param url
     */
    @Given("^I browse to '(.*?)'$")
    public void seleniumBrowse(String url) {
        assertThat(url).isNotEmpty();
        String newUrl = commonspec.replacePlaceholders(url);
        commonspec.getLogger().info("Browsing to {} with {}", newUrl, commonspec.getBrowserName());

        commonspec.getDriver().get("http://" + newUrl);
    }

    /**
     * Maximizes current browser window. Mind the current resolution could break a test.
     * 
     */
    @Given("^I maximize the browser$")
    public void seleniumMaximize(String url) {
        commonspec.getDriver().manage().window().maximize();
    }
}