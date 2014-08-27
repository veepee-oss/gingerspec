package com.stratio.specs;


import static com.stratio.tests.utils.matchers.ExceptionMatcher.hasClassAndMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import cucumber.api.DataTable;
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
	
	@Then("^a Casandra keyspace '(.*?)' exists$")
	public void assertKeyspaceOnCassandraExists(String keyspace){
		commonspec.getLogger().info("Verifying that the keyspace {} exists", keyspace);
		boolean result = commonspec.getCassandraClient().existsKeyspace(keyspace, false);
		if(result){
			assertThat("The keyspace " + keyspace + "exists on cassandra", true, equalTo(result));
		}else{
			assertThat("The keyspace " + keyspace + "does not exist on cassandra.", false, equalTo(result));
		}
	}
	
	@Then("^a Casandra keyspace '(.*?)' contains a table '(.*?)'$")
	public void assertTableExistsOnCassandraKeyspace(String keyspace, String tableName){
		
	}
	
	@Then("^a Casandra keyspace '(.*?)' contains a table '(.*?)' with '(.*?)' rows$")
	public void assertRowNumberOfTableOnCassandraKeyspace(String keyspace, String tableName, Integer number_rows){
		
	}
	
	@Then("^a Casandra keyspace '(.*?)' contains a table '(.*?)' with values:$")
	public void assertValuesOfTable(String Keyspace, String tableName, DataTable data){
		List<List<String>> a = data.raw();
		for (List<String> z: a) {
			
		}
	}
	
	
	
	
}