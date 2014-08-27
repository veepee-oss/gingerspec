package com.stratio.specs;

import com.stratio.specs.BaseGSpec;
import com.stratio.tests.utils.CassandraUtils;

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

	@Before("@C*")
	public void cassandraSetup() {
		commonspec.getLogger().info("Setting up C* client");
		commonspec.setCassandraClient(new CassandraUtils());
	}

	@Before("@MongoDB")
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