package com.privalia.qa.specs;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

public class SqlDatabaseGSpec extends BaseGSpec {


    public SqlDatabaseGSpec(CommonG spec) {
        this.commonspec = spec;
    }


    /*
     * connects to database with parameters:
     *
     * @param database
     * @param host
     * @param port
     * @param user
     * @param password
     *
     * saves connection
     *
     */
    @Given("^I( securely)? connect with JDBC to database '(.+?)' on host '(.+?)' and port '(.+?)' with user '(.+?)' and password '(.+?)'?$")
    public void connectDatabase(String isSecured, String database, String host, String port, String user, String password) throws Exception {
        if (isSecured != null) {
            commonspec.getLogger().debug("opening secure database");
            this.commonspec.getSqlClient().connect();

        } else {
            commonspec.getLogger().debug("opening database");
            this.commonspec.getSqlClient().connect();
        }
    }

    /*
     * closes opened database
     *
     */
    @Then("^I close database connection$")
    public void connectDatabase() throws Exception {
        this.commonspec.getSqlClient().disconnect();
    }

}
