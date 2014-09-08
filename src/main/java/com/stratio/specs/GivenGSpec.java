package com.stratio.specs;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;

public class GivenGSpec extends BaseGSpec {

    public GivenGSpec(CommonG spec) {
        this.commonspec = spec;
    }

    @Given("^I empty every existing elasticsearch index$")
    public void emptyElasticsearchIndexes() {
        commonspec.getLogger().info("Emptying es indexes");
        commonspec.getElasticSearchClient().emptyIndexes();
    }

    @Given("^I empty an elasticsearch index named '(.*?)'$")
    public void emptyElasticsearchIndex(String index) {
        commonspec.getLogger().info("Emptying an es index: {}", index);
        commonspec.getElasticSearchClient().emptyIndex(index);
    }

    @Given("^I drop every existing elasticsearch index$")
    public void dropElasticsearchIndexes() {
        commonspec.getLogger().info("Dropping es indexes");
        commonspec.getElasticSearchClient().dropIndexes();
    }

    @Given("^I drop an elasticsearch index named '(.*?)'$")
    public void dropElasticsearchIndex(String index) {
        commonspec.getLogger().info("Dropping an es index: {}", index);
        commonspec.getElasticSearchClient().dropIndex(index);
    }

    @Given("a C* script with name '(.*?)' and default keyspace '(.*?)'$")
    public void insertDataOnCassandraFromFile(String filename, String keyspace) {
        commonspec.getLogger().info("Inserting data on cassandra from file");
        commonspec.getCassandraClient().loadTestData(keyspace,
                "/scripts/" + filename);
    }

    @Given("^I drop an C* keyspace '(.*?)'$")
    public void dropCassandraKeyspace(String keyspace) {
        commonspec.getLogger().info("Dropping a C* keyspace", keyspace);
        commonspec.getCassandraClient().dropKeyspace(keyspace);
    }

    @Given("^I create an AeroSpike namespace '(.*?)' with table '(.*?)':$")
    public void createAeroSpikeTable(String nameSpace, String tableName,
            DataTable tab) {
        commonspec.getLogger().info("Creating a table on AeroSpike");
        if (commonspec.getAerospikeClient().isConnected()) {
            commonspec.getLogger().info("Creating a table on AeroSpike");
        }
        commonspec.getAerospikeClient().insertFromDataTable(nameSpace,
                tableName, tab);
    }

    @Given("^I create a MongoDB dataBase '(.*?)'$")
    public void createMongoDBDataBase(String databaseName) {
        commonspec.getLogger().info("Creating a database on MongoDB");
        commonspec.getMongoDBClient().connectToMongoDBDataBase(databaseName);

    }

    @Given("^I drop a MongoDB database '(.*?)'$")
    public void dropMongoDBDataBase(String databaseName) {
        commonspec.getLogger().info("Creating a database on MongoDB");
        commonspec.getMongoDBClient().dropMongoDBDataBase(databaseName);
    }

    @Given("^I insert into a MongoDB database '(.*?)' and table '(.*?)' this values:$")
    public void insertOnMongoTable(String dataBase, String tabName,
            DataTable table) {
        commonspec.getLogger().info("Inserting data in a database on MongoDB");
        commonspec.getMongoDBClient().connectToMongoDBDataBase(dataBase);
        commonspec.getMongoDBClient().insertIntoMongoDBCollection(tabName,
                table);
    }

    @Given("^I drop every document at a MongoDB database '(.*?)' and table '(.*?)'")
    public void truncateTableInMongo(String database, String table) {
        commonspec.getLogger().info("Truncating a table in MongoDB");
        commonspec.getMongoDBClient().connectToMongoDBDataBase(database);
        commonspec.getMongoDBClient().dropAllDataMongoDBCollection(table);
    }

}