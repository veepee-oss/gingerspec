package com.stratio.specs;

import com.stratio.specs.BaseGSpec;

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
}