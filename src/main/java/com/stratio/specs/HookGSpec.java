package com.stratio.specs;

import cucumber.api.java.After;
import cucumber.api.java.Before;

public class HookGSpec extends BaseGSpec {

	public HookGSpec(CommonG spec) {
		this.commonspec = spec;
	}

	@Before(order = 0)
	public void globalSetup() {
		commonspec.getLogger().info("Clearing exception list");
		commonspec.getExceptions().clear();
	}

	@Before(order = 10, value = "@C*")
	public void cassandraSetup() throws Exception {
		commonspec.getLogger().info("Setting up C* client");
		commonspec.getCassandraClient().connect();
	}

	@Before(order = 10, value = "@MongoDB")
	public void mongoSetup() {
		commonspec.getLogger().info("Setting up MongoDB client");
		commonspec.getMongoDBClient().connectToMongoDB();
	}

	@Before(order = 10, value = "@elasticsearch")
	public void elasticsearchSetup() {
		commonspec.getLogger().info("Setting up elasticsearch client");
		commonspec.getElasticSearchClient().connect();
	}

	@Before(order = 10, value = "@Aerospike")
	public void aerospikeSetup() {
		commonspec.getLogger().info("Setting up Aerospike client");
		commonspec.getAerospikeClient().connect();
	}

	@After(order = 20, value = "@C*")
	public void cassandraTeardown() {
		commonspec.getLogger().info("Shutdown  C* client");
		commonspec.getCassandraClient().disconnect();
	}

	@After(order = 20, value = "@MongoDB")
	public void mongoTeardown() {
		commonspec.getLogger().info("Shutdown MongoDB client");
		commonspec.getMongoDBClient().disconnect();
	}

	@After(order = 20, value = "@elasticsearch")
	public void elasticsearchTeardown() {
		commonspec.getLogger().info("Shutdown elasticsearch client");
		commonspec.getElasticSearchClient().disconnect();
	}

	@After(order = 20, value = "@Aerospike")
	public void aerospikeTeardown() {
		commonspec.getLogger().info("Shutdown Aerospike client");
		commonspec.getAerospikeClient().disconnect();
	}

	@After(order = 0)
	public void teardown() {
		commonspec.getLogger().info("Ended running hooks\n");
	}
}