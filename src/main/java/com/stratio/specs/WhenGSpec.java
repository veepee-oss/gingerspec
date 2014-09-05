package com.stratio.specs;

import com.stratio.specs.BaseGSpec;
import com.stratio.specs.CommonG;

import cucumber.api.java.en.When;

public class WhenGSpec extends BaseGSpec {

	public WhenGSpec(CommonG spec) {
		this.commonspec = spec;
	}

	@When("^I wait '(.*?)' seconds?$")
	public void idleWait(Integer seconds) throws InterruptedException {
		commonspec.getLogger().info("Idling a while");
		Thread.sleep(seconds * 1000);
	}
}
