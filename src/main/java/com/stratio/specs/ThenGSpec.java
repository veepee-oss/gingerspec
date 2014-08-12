package com.stratio.specs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;

import com.stratio.specs.BaseGSpec;
import com.stratio.specs.CommonG;

import cucumber.api.java.en.Then;

public class ThenGSpec extends BaseGSpec {

	public ThenGSpec(CommonG spec) {
		this.commonspec = spec;
	}

	@Then("^an exception '(.*?)' thrown( with class '(.*?)')?")
	public void assertExceptionNotThrown(String exception, String foo,
			String clazz) {
		commonspec.getLogger().info("Verifying thrown exceptions existance");

		if ("IS NOT".equals(exception)) {
			assertThat("Captured exception list is not empty",
					commonspec.getExceptions(), hasSize(0));
		} else {
			List<Exception> exceptions = commonspec.getExceptions();
			if (clazz != null) {
				assertThat("Unexpected last exception class",
						exceptions.get(exceptions.size() - 1).getClass()
								.getSimpleName(), equalTo(clazz));
			}
			assertThat("Captured exception list is empty", exceptions,
					not(hasSize(0)));
			commonspec.getExceptions().clear();
		}
	}
}