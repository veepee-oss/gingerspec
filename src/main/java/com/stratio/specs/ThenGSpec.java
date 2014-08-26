package com.stratio.specs;


import static com.stratio.tests.utils.matchers.ExceptionMatcher.hasClassAndMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import cucumber.api.java.en.Then;

public class ThenGSpec extends BaseGSpec {

	public ThenGSpec(CommonG spec) {
		this.commonspec = spec;
	}

	@Then("^an exception '(.*?)' thrown( with class '(.*?)'( and message like '(.*?)')?)?")
	public void assertExceptionNotThrown(String exception, String foo,
			String clazz, String bar, String exceptionMsg) throws ClassNotFoundException{
		commonspec.getLogger().info("Verifying thrown exceptions existance");

		List<Exception> exceptions = commonspec.getExceptions();
		if ("IS NOT".equals(exception)) {
			assertThat("Captured exception list is not empty", exceptions,
					hasSize(0));
		} else {
			assertThat("Captured exception list is empty", exceptions,
					hasSize(greaterThan((0))));
			Exception ex = exceptions.get(exceptions.size() - 1);
			if ((clazz != null) && (exceptionMsg != null)) {

				assertThat("Unexpected last exception class or message", ex,
						hasClassAndMessage(clazz, exceptionMsg));

			} else if (clazz != null) {
				assertThat("Unexpected last exception class",
						exceptions.get(exceptions.size() - 1).getClass()
								.getSimpleName(), equalTo(clazz));
			}

			commonspec.getExceptions().clear();
		}
	}
}