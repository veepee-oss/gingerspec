/*
 * Copyright (C) 2018 Privalia (http://privalia.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.privalia.qa.specs;

import com.privalia.qa.utils.ThreadProperty;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.datatable.DataTable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Steps definitions for working with relational databases (postgresql and mysql)
 *
 * @author Jose Fernandez
 */
public class SqlDatabaseGSpec extends BaseGSpec {


    public SqlDatabaseGSpec(CommonG spec) {
        this.commonspec = spec;
    }


    /**
     * Attempts to establish a connection with the given database.
     * <p>
     * The DriverManager attempts to select an appropriate driver from the set of registered JDBC drivers.
     * All subsequent steps that interact with the database will be performed on this connection. You can also use the
     * step {@link #disconnectDatabase()} to close this connection at the end of your scenarios, however, it is recommended
     * to include the @sql tag in your scenarios to allow gingerspec to do this automatically. The current supported types of
     * databases are postgresql/mysql
     * <pre>{@code
     * Example:
     *
     * Scenario: Connecting to a mysql database with user/password
     *      Given I connect with JDBC to database 'databaseName' type 'mysql' on host '121.0.0.1' and port '3306' with user 'root' and password 'P@$$W0RD'
     *
     * Scenario: If the database does not have a password
     *      Given I connect with JDBC to database 'databaseName' type 'mysql' on host '121.0.0.1' and port '3306' with user 'root'
     *
     * Scenario: Connecting to a postgresql database
     *      Given I connect with JDBC to database 'databaseName' type 'postgresql' on host '121.0.0.1' and port '5432' with user 'postgres' and password 'P@$$W0RD'
     * }</pre>
     *
     * @see #disconnectDatabase()
     * @param isSecured     True if secure connection
     * @param database      Name of the remote database
     * @param dataBaseType  Database type (currently MYSQL/POSTGRESQL)
     * @param host          URL of remote host
     * @param port          Database port
     * @param user          Database user
     * @param password      Database password
     */
    @Given("^I( securely)? connect with JDBC to database '(.+?)' type '(mysql|postgresql)' on host '(.+?)' and port '(.+?)' with user '(.+?)'( and password '(.+?)')?$")
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
            fail("There was a problem connecting to the DB: " + e.getMessage());
        }

        assertThat(this.commonspec.getSqlClient().connectionStatus()).as("There was a problem connecting to the DB: The connection status is 'false'").isEqualTo(true);

    }

    /**
     * Close the Database connection
     * <p>
     * Closes the active database connection. To create a database connection use the step {@link #connectDatabase(String, String, String, String, String, String, String)}
     * You can use this step to close the database connection at the end of your scenarios, however, it is recommended to include
     * the @sql tag in your scenarios to allow gingerspec to do this automatically.
     * <pre>{@code
     * Example:
     *
     * Scenario: Closing an existing database connection
     *      Given I connect with JDBC to database 'databaseName' type 'mysql' on host '121.0.0.1' and port '3306' with user 'root' and password 'P@$$W0RD'
     *      Then I close database connection
     * }</pre>
     *
     * @see #connectDatabase(String, String, String, String, String, String, String)
     */
    @Then("^I close database connection$")
    public void disconnectDatabase() {
        try {
            this.commonspec.getSqlClient().disconnect();
        } catch (SQLException e) {
            fail("Could not close DB connection" + e.getMessage());
        }

        assertThat(this.commonspec.getSqlClient().connectionStatus()).as("Could not close DB connection. Connection status is 'true'").isEqualTo(false);
    }

    /**
     * Executes the given SQL statement, which may be an INSERT, UPDATE, or DELETE statement
     * <p>
     * The given SQL statement must be an INSERT, UPDATE, or DELETE statement or an SQL statement
     * that returns nothing, such as an SQL DDL statement. To execute an statement that does return a
     * ResultSet (rows), such as a <code>SELECT</code> statement, use the step {@link #executeSelectQuery(String)}
     * <pre>{@code
     * Example:
     *
     * Scenario: Create a table (CREATE statement returns nothing, as well as TRUNCATE, INSERT)
     *      Given I connect with JDBC to database 'databaseName' type 'mysql' on host '121.0.0.1' and port '3306' with user 'root' and password 'P@$$W0RD'
     *      And I execute query 'CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);'
     *      And I execute query 'TRUNCATE weather1'
     *      When I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('San Francisco', 15, 43, 0.0, '2004-11-29');'
     *      Then I close database connection
     * }</pre>
     *
     * @see #executeSelectQuery(String)
     * @see #connectDatabase(String, String, String, String, String, String, String)
     * @see #disconnectDatabase()
     * @param query An SQL Data Manipulation Language (DML) statement, such as INSERT, UPDATE or DELETE;
     *              or an SQL statement that returns nothing, such as a DDL statement.
     */
    @When("^I execute query '(.+?)'$")
    public void executeQuery(String query) {

        int result;
        try {
            result = this.commonspec.getSqlClient().executeUpdateQuery(query);
        } catch (SQLException e) {
            fail("A problem was found while executing the query: " + e.getMessage());
        }

    }

    /**
     * Verify if a table exists
     * <pre>{@code
     * Example:
     *
     * Scenario: Create a table and then verify it was created
     *      Given I connect with JDBC to database 'databaseName' type 'mysql' on host '121.0.0.1' and port '3306' with user 'root' and password 'P@$$W0RD'
     *      When I execute query 'CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);'
     *      Then table 'weather1' exists
     *      Then I close database connection
     * }</pre>
     *
     * @see #connectDatabase(String, String, String, String, String, String, String)
     * @see #executeQuery(String)
     * @see #disconnectDatabase()
     * @param tableName Table name
     */
    @Then("^table '(.+?)' exists$")
    public void verifyTableExists(String tableName) {

        assertThat(this.verifyTable(tableName)).as(String.format("The table %s is not present in the database", tableName)).isTrue();
    }

    /**
     * Verify if a table does not exists
     * <pre>{@code
     * Example:
     *
     * Scenario: DROP a table and verify it was deleted
     *      Given I connect with JDBC to database 'databaseName' type 'mysql' on host '121.0.0.1' and port '3306' with user 'root' and password 'P@$$W0RD'
     *      When I execute query 'DROP TABLE weather1;'
     *      Then table 'weather1' doesn't exists
     *      Then I close database connection
     * }</pre>
     *
     * @see #connectDatabase(String, String, String, String, String, String, String)
     * @see #executeQuery(String)
     * @see #disconnectDatabase()
     * @param tableName Table name
     */
    @Then("^table '(.+?)' doesn't exists$")
    public void verifyTableDoesNotExists(String tableName) {

        assertThat(this.verifyTable(tableName)).as(String.format("The table %s is present in the database", tableName)).isFalse();
    }

    /**
     * Executes an SQL statement which returns a ResultSet, such as a <code>SELECT</code> statement
     * <p>
     * This step is for executing a SQL statement which returns a ResultSet object, typically a
     * static SQL <code>SELECT</code> statement. The result is stored in a local variable that
     * can be read by future steps in the same scenario. Other types of statements (statements that dont
     * return a result such as INSERT, UPDATE, DROP, CREATE, etc) must be executed with {@link #executeQuery(String)}
     * <pre>{@code
     * Example:
     *
     * Scenario: Select all fields from table
     *      Given I connect with JDBC to database 'databaseName' type 'mysql' on host '121.0.0.1' and port '3306' with user 'root' and password 'P@$$W0RD'
     *      When I query the database with 'SELECT * FROM weather1;'
     * }</pre>
     *
     * @see #connectDatabase(String, String, String, String, String, String, String)
     * @see #compareTable(DataTable)
     * @param query An SQL statement to be sent to the database, typically a static SQL SELECT statement
     */
    @When("^I query the database with '(.+?)'$")
    public void executeSelectQuery(String query) {

        List<List<String>> result = null;
        try {
            result = this.commonspec.getSqlClient().executeSelectQuery(query);
            this.commonspec.setPreviousSqlResult(result);
        } catch (SQLException e) {
            fail("A problem was found while executing the query: " + e.getMessage());
        }

    }

    /**
     * Verifies the results of a SELECT query against a {@link DataTable}
     * <p>
     * This step compares the result of a previous SELECT operation to the given datatable.
     * The datatable must contain the result as it is expected from the database. If the given SELECT
     * statement did not return any rows, only the columns names will be returned as a single row
     * <pre>{@code
     * Example:
     *
     * Scenario: Check the result of a SELECT statement
     *      Given I connect with JDBC to database 'databaseName' type 'mysql' on host '121.0.0.1' and port '3306' with user 'root' and password 'P@$$W0RD'
     *      When I query the database with 'SELECT * FROM weather1;'
     *      Then I check that result is:
     *        | city      | temp_lo | temp_hi | prcp | date       |
     *        | Caracas   | 15      | 43      | 0.0  | 2004-11-29 |
     *        | Barcelona | 5       | 37      | 0.4  | 2014-11-29 |
     *        | Madrid    | 8       | 37      | 0.4  | 2016-11-30 |
     *      Then I close database connection
     * }</pre>
     *
     * @see #connectDatabase(String, String, String, String, String, String, String)
     * @see #executeSelectQuery(String)
     * @see #disconnectDatabase()
     * @param dataTable     list of cases to assert in a table format
     */
    @Then("^I check that result is:$")
    public void compareTable(DataTable dataTable) {

        List<List<String>> previousResult = this.commonspec.getPreviousSqlResult();
        assertThat(previousResult).as("The last SQL query returned a null result").isNotNull();
        assertThat(previousResult.size()).as("The last SQL query did not returned any rows").isNotEqualTo(0);
        assertThat(dataTable.asLists()).as("The returned and the expected results do not match.").isEqualTo(previousResult);

    }

    /**
     * Verify if the content of a table matches the given {@link DataTable}
     * <p>
     * This step verifies the whole content of the table specified. That is like performing a <code>SELECT * FROM</code> statement
     * on the table and then using the datatable to check the result. This, of course, makes sense on tables that dont contain too many rows
     * <pre>{@code
     * Example:
     *
     * Scenario: Checking the content of a table
     *      Given I connect with JDBC to database 'databaseName' type 'mysql' on host '121.0.0.1' and port '3306' with user 'root' and password 'P@$$W0RD'
     *      Then I check that table 'weather1' is iqual to
     *        | city      | temp_lo | temp_hi | prcp | date       |
     *        | Caracas   | 15      | 43      | 0.0  | 2004-11-29 |
     *        | Barcelona | 5       | 37      | 0.4  | 2014-11-29 |
     *        | Madrid    | 8       | 37      | 0.4  | 2016-11-30 |
     *      Then I close database connection
     * }</pre>
     *
     * @see #connectDatabase(String, String, String, String, String, String, String)
     * @see #disconnectDatabase()
     * @param tableName Table name
     * @param dataTable {@link DataTable} to match against
     */
    @Then("^I check that table '(.+?)' is equal to$")
    @Then("^I check that table '(.+?)' is iqual to$")
    public void verifyTableContent(String tableName, DataTable dataTable) {

        this.verifyTable(tableName);
        this.executeSelectQuery("SELECT * FROM " + tableName);
        this.compareTable(dataTable);

    }

    /**
     * Executes an SQL from a file.
     * <p>
     * The SQL could be of any kind (a typical SELECT or a SQL Data
     * Manipulation Language (DML) statement, such as INSERT, UPDATE or DELETE) or even SQL Scripts.
     * If the SQL returns a {@link java.sql.ResultSet}, it is stored internally so further steps can use it
     * <pre>{@code
     * Example:
     *
     * Scenario: Execute query from a file:
     *      Given I connect with JDBC to database 'databaseName' type 'mysql' on host '121.0.0.1' and port '3306' with user 'root' and password 'P@$$W0RD'
     *      When I execute query from 'sql/selectWeather.sql'
     *      Then I check that result is:
     *        | city          | temp_lo | temp_hi | prcp | date       |
     *        | San Francisco | 15      | 43      | 0.0  | 2004-11-29 |
     *        | Kyiv          | 5       | 37      | 0.4  | 2014-11-29 |
     *        | Paris         | 8       | 37      | 0.4  | 2016-11-30 |
     *      Then I close database connection
     * }</pre>
     * @see #connectDatabase(String, String, String, String, String, String, String)
     * @see #compareTable(DataTable)
     * @see #disconnectDatabase()
     * @param baseData      File location (typically schemas/myfile.sql)
     * @throws IOException  IOException
     */
    @Then("^I execute query from '(.+?)'")
    public void executeQueryFromFile(String baseData) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(baseData);
        Reader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

        try {

            boolean r = this.commonspec.getSqlClient().executeQuery(reader);

            if (r) {
                this.commonspec.setPreviousSqlResult(this.commonspec.getSqlClient().getPreviousSqlResult());
            }

        } catch (SQLException e) {
            fail("A problem was found while executing the query: " + e.getMessage());
        } finally {
            reader.close();
        }
    }


    /**
     * Save a specific element (by row and column) in an environmental variable
     * <p>
     * The first row (row number 0) corresponds to the column names. If the previous SELECT statement did
     * not return any rows, only the columns names will be returned as a single row
     * <pre>{@code
     * Example:
     *
     * Scenario: Saving values from a previous sql result
     *      Given I connect with JDBC to database 'databaseName' type 'mysql' on host '121.0.0.1' and port '3306' with user 'root' and password 'P@$$W0RD'
     *      When I execute query from 'sql/selectWeather.sql'
     *      Then I check that result is:
     *        | city      | temp_lo | temp_hi | prcp | date       |
     *        | Caracas   | 15      | 43      | 0.0  | 2004-11-29 |
     *        | Barcelona | 5       | 37      | 0.4  | 2014-11-29 |
     *      Then I save the value of the row number '1' and the column with name 'city' in environment variable 'CITY'
     *      Then I save the value of the row number '2' and the column with name 'temp_hi' in environment variable 'TEMP_BARCELONA'
     *      Then '!{CITY}' matches 'Caracas'
     *      Then '!{TEMP_BARCELONA}' matches '37'
     * }</pre>
     * @see #connectDatabase(String, String, String, String, String, String, String)
     * @see #executeQueryFromFile(String)
     * @see UtilsGSpec#checkValue(String, String, String)
     * @param rowNumber  the row number
     * @param columnName the column name
     * @param envVar     the env var Name
     */
    @Then("^I save the value of the row number '(\\d+?)' and the column with name '(.+?)' in environment variable '(.+?)'$")
    public void saveSqlResultInVariable(int rowNumber, String columnName, String envVar) {

        List<List<String>> previousResult = this.commonspec.getPreviousSqlResult();
        assertThat(previousResult).as("The last SQL query returned a null result").isNotNull();
        assertThat(previousResult.size()).as("The last SQL query did not return any rows").isNotEqualTo(0);
        assertThat(previousResult.get(0).contains(columnName)).as("The last SQL query did not have a column with name " + columnName).isTrue();

        int columnNUmber = previousResult.get(0).indexOf(columnName);

        assertThat(previousResult.size() - 1 >= rowNumber).as("The column " + columnName + " only contains " + (previousResult.size() - 1) + " elements").isTrue();
        ThreadProperty.set(envVar, previousResult.get(rowNumber).get(columnNUmber).trim());

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
