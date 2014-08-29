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
	public void cassandraSetup() {
		commonspec.getLogger().info("Setting up C* client");
		commonspec.getCassandraClient().connect();
	}

	@Before(order = 10, value = "@MongoDB")
	public void mongoSetup() {
		commonspec.getLogger().info("Setting up MongoDB client");
	}

	@After("@C*")
	public void cassandraTeardown() {
		commonspec.getLogger().info("Shutdown  C* client");
		commonspec.getCassandraClient().disconnect();
	}

	@After("@MongoDB")
	public void mongoTeardown() {
		commonspec.getLogger().info("Shutdown MongoDB client");
	}
}