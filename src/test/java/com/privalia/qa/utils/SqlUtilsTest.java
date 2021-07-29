/*
 * Copyright (c) 2021, Veepee
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose
 * with or without fee is hereby  granted, provided that the above copyright notice
 * and this permission notice appear in all copies.
 *
 * THE SOFTWARE  IS PROVIDED "AS IS"  AND THE AUTHOR DISCLAIMS  ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING  ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS.  IN NO  EVENT  SHALL THE  AUTHOR  BE LIABLE  FOR  ANY SPECIAL,  DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 * OF USE, DATA  OR PROFITS, WHETHER IN AN ACTION OF  CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR  IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
*/

package com.privalia.qa.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Uses the mysql and postgresql docker images for testing. Create the containers with this parameters
 * before running this test suite
 *
 * docker pull mysql
 * docker run -d -p 3306:3306 --name mysql -e MYSQL_ROOT_PASSWORD=mysql -d mysql:latest
 *
 * docker pull postgres
 * docker run -d -p 5432:5432 --name postgres -e POSTGRES_PASSWORD=postgres -d postgres:latest
 */
public class SqlUtilsTest {

    private SqlUtils sql;
    private final Logger logger = LoggerFactory
            .getLogger(SqlUtilsTest.class);

    @Test(enabled = false)
    public void connectMySqlTest() throws SQLException, ClassNotFoundException {

        this.connectToMysqlDb();
        assertThat(this.sql.connectionStatus()).isTrue();
    }

    @Test(enabled = false)
    public void connectPostgresTest() throws SQLException, ClassNotFoundException {

        this.connectToPostgreDb();
        assertThat(this.sql.connectionStatus()).isTrue();
    }

    @Test(enabled = false)
    public void executeQueryMysqlTest() throws SQLException, ClassNotFoundException {

        this.connectToMysqlDb();
        int result = this.sql.executeUpdateQuery("CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);");
        assertThat(result).isEqualTo(0);
    }

    @Test(enabled = false)
    public void executeQueryPostgreTest() throws SQLException, ClassNotFoundException {

        this.connectPostgresTest();
        int result = this.sql.executeUpdateQuery("CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);");
        assertThat(result).isEqualTo(0);
    }

    @Test(enabled = false)
    public void executeSelectQueryMysqlTest() throws SQLException, ClassNotFoundException {

        this.connectToMysqlDb();
        this.sql.executeUpdateQuery("CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);");
        this.sql.executeUpdateQuery("TRUNCATE weather1");
        this.sql.executeUpdateQuery("INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('San Francisco', 15, 43, 0.0, '2004-11-29');");
        this.sql.executeUpdateQuery("INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Kyiv', 5, 37, 0.4, '2014-11-29');");
        this.sql.executeUpdateQuery("INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Paris', 8, 37, 0.4, '2016-11-30');");

        List<List<String>> result = this.sql.executeSelectQuery("SELECT * FROM weather1;");
        assertThat(result.size()).isEqualTo(4); //-> 3 Lists of results plus 1 List of column names

        this.sql.executeUpdateQuery("TRUNCATE weather1");
        result = this.sql.executeSelectQuery("SELECT * FROM weather1;");
        assertThat(result.size()).isEqualTo(1);// returns only the list of column names

    }

    @Test(enabled = false)
    public void executeSelectQueryPostgreTest() throws SQLException, ClassNotFoundException {

        this.connectPostgresTest();
        this.sql.executeUpdateQuery("CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);");
        this.sql.executeUpdateQuery("TRUNCATE weather1");
        this.sql.executeUpdateQuery("INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('San Francisco', 15, 43, 0.0, '2004-11-29');");
        this.sql.executeUpdateQuery("INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Kyiv', 5, 37, 0.4, '2014-11-29');");
        this.sql.executeUpdateQuery("INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Paris', 8, 37, 0.4, '2016-11-30');");

        List<List<String>> result = this.sql.executeSelectQuery("SELECT * FROM weather1;");
        assertThat(result.size()).isEqualTo(4);  //-> 3 Lists of results plus 1 List of column names

        this.sql.executeUpdateQuery("TRUNCATE weather1");
        result = this.sql.executeSelectQuery("SELECT * FROM weather1;");
        assertThat(result.size()).isEqualTo(1);// returns only the list of column names

    }

    @Test(enabled = false)
    public void verifyTableExistsMysqlTest() throws SQLException, ClassNotFoundException {

        this.connectMySqlTest();
        this.sql.executeUpdateQuery("CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);");
        assertThat(this.sql.verifyTable("weather1")).isTrue();
        this.sql.executeUpdateQuery("DROP TABLE weather1;");
        assertThat(this.sql.verifyTable("weather1")).isFalse();
    }

    @Test(enabled = false)
    public void verifyTableExistsPostgreTest() throws SQLException, ClassNotFoundException {

        this.connectPostgresTest();
        this.sql.executeUpdateQuery("CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);");
        assertThat(this.sql.verifyTable("weather1")).isTrue();
        this.sql.executeUpdateQuery("DROP TABLE weather1;");
        assertThat(this.sql.verifyTable("weather1")).isFalse();
    }


    private void connectToMysqlDb() throws SQLException, ClassNotFoundException {
        logger.debug(String.format("Connecting to %s DB", "mysql"));
        this.sql = new SqlUtils();
        this.sql.connect("172.17.0.1", 3306, "mysql", "mysql", false, "root", "mysql");
    }

    private void connectToPostgreDb() throws SQLException, ClassNotFoundException {
        logger.debug(String.format("Connecting to %s DB", "postgres"));
        this.sql = new SqlUtils();
        this.sql.connect("172.17.0.1", 5432, "postgresql", "postgres", false, "postgres", "postgres");
    }

    @AfterMethod
    public void disconnect() throws SQLException {
        logger.debug("Closing database connection");
        this.sql.disconnect();
        assertThat(this.sql.connectionStatus()).isFalse();
    }


}
