package com.privalia.qa.specs;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class SqlDatabaseGSpec extends BaseGSpec {


    public SqlDatabaseGSpec(CommonG spec) {
        this.commonspec = spec;
    }

    /**
     * Connects to the DB
     * @param isSecured
     * @param database
     * @param dataBaseType
     * @param host
     * @param port
     * @param user
     * @param password
     */
    @Given("^I( securely)? connect with JDBC to database '(.+?)' type '(mysql|postgresql)' on host '(.+?)' and port '(.+?)' with user '(.+?)' and password '(.+?)'?$")
    public void connectDatabase(String isSecured, String database, String dataBaseType, String host, String port, String user, String password) {
        try {
            if (isSecured != null) {
                commonspec.getLogger().debug("opening secure database");
                this.commonspec.getSqlClient().connect(host, Integer.parseInt(port), dataBaseType, database, Boolean.parseBoolean(isSecured), user, password);
            } else {
                commonspec.getLogger().debug("opening database");
                this.commonspec.getSqlClient().connect(host, Integer.parseInt(port), dataBaseType, database, Boolean.parseBoolean(isSecured), user, password);
            }
        } catch (ClassNotFoundException | SQLException e) {
            commonspec.getLogger().error("There was a problem connecting to the DB\n{}", e.toString());
        }

        assertThat(this.commonspec.getSqlClient().connectionStatus()).isEqualTo(true);

    }

    /**
     * Close the Database connection
     */
    @Then("^I close database connection$")
    public void connectDatabase() {
        try {
            this.commonspec.getSqlClient().disconnect();
        } catch (SQLException e) {
            commonspec.getLogger().error("Could not close DB connection\n{}", e.toString());
        }

        assertThat(this.commonspec.getSqlClient().connectionStatus()).isEqualTo(false);
    }

    @When("^I execute query '(.+?)'$")
    public void executeQuery(String query) {

        int result;
        try {
            result = this.commonspec.getSqlClient().executeQuery(query);
        } catch (SQLException e) {
            assertThat(e.getMessage()).as("A problem was found while executing the query").isEmpty();
        }

    }

    @Then("^table '(.+?)' exists$")
    public void verifyTableExists(String tableName) {

        assertThat(this.verifyTable(tableName)).as(String.format("The table %s is not present in the database", tableName)).isTrue();
    }

    @Then("^table '(.+?)' doesn't exists$")
    public void verifyTableDoesNotExists(String tableName) {

        assertThat(this.verifyTable(tableName)).as(String.format("The table %s is present in the database", tableName)).isFalse();
    }

    @When("^I query the database with '(.+?)'$")
    public void executeSelectQuery(String query) {

        List<Map<String, Object>> result = null;
        try {
            result = this.commonspec.getSqlClient().executeSelectQuery(query);
        } catch (SQLException e) {
            assertThat(e.getMessage()).as("A problem was found while executing the query").isEmpty();
        }

    }

    @Then("^I check that result is:$")
    public void compareTable(String tableName, DataTable dataTable){



    }

    private boolean verifyTable(String tableName) {

        boolean exists;

        try {
            exists = this.commonspec.getSqlClient().verifyTable(tableName);
        } catch (SQLException e) {
            commonspec.getLogger().error("A problem was found when checking if {} exists: \n{}", tableName, e.toString());
            exists = false;
        }
        return exists;
    }

}
