package com.stratio.specs;

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
}