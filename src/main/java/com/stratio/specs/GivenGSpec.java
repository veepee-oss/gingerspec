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
	
	@Given("a Cassandra script with name '(.*?)' and default keyspace '(.*?)'$")
	public void InsertDataOnCassandraFromFile(String filename, String keyspace) {
		commonspec.getLogger().info("Inserting data on cassandra from file");
		commonspec.getCassandraClient().loadTestData(keyspace, "/scripts/" + filename);
	}
	
	@Given("^I drop an Cassandra keyspace '(.*?)'$")
	public void DropCassandraKeyspace(String keyspace) {
		commonspec.getLogger().info("Dropping a Cassandra Keyspace");
		commonspec.getCassandraClient().dropKeyspace(keyspace);
	}
	
	@Given("^a Aerospike namespace '(.*?)' with table '(.*?)':$")
	public void CreateAeroSpikeTable(String nameSpace, String tableName, DataTable tab) {
		commonspec.getLogger().info("Creating a table on AeroSpike");
		if(commonspec.getAerospikeClient().isConnected()){
			commonspec.getLogger().info("Creating a table on AeroSpike");
		}
		commonspec.getAerospikeClient().insertFromDataTable(nameSpace, tableName, tab);
	}	
	
}