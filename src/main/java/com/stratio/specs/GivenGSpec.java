package com.stratio.specs;

import cucumber.api.java.en.Given;

public class GivenGSpec extends BaseGSpec {

	public GivenGSpec(CommonG spec) {
		this.commonspec = spec;
	}

	@Given("^I drop every existing elasticsearch index$")
	public void DropElasticsearchIndexes() {
		commonspec.getLogger().info("Dropping es indexes");
	}
}