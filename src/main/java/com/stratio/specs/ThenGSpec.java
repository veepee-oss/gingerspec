package com.stratio.specs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

import com.stratio.specs.BaseGSpec;
import com.stratio.specs.CommonG;

import cucumber.api.java.en.Then;

public class ThenGSpec extends BaseGSpec {

	public ThenGSpec(CommonG spec) {
		this.commonspec = spec;
	}

	@Then("^an exception '(.*?)' thrown$")
	public void assertExceptionNotThrown(String exception) {
		commonspec.getLogger().info("Verifying thrown exceptions existance");

		if ("IS NOT".equals(exception)) {
			assertThat("Captured exception list is not empty",
					commonspec.getExceptions(), hasSize(0));
		} else {
			assertThat("Captured exception list is empty",
					commonspec.getExceptions(), not(hasSize(0)));
			commonspec.getExceptions().clear();
		}
	}
}