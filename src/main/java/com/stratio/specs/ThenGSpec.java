package com.stratio.specs;

import com.stratio.specs.BaseGSpec;
import com.stratio.specs.CommonG;

import cucumber.api.java.en.Then;

public class ThenGSpec extends BaseGSpec {

	public ThenGSpec(CommonG spec) {
		this.commonspec = spec;
	}

	@Then("^nop$")
	public void assertNop() {
		commonspec.getLogger().info("NOP");

	}
}