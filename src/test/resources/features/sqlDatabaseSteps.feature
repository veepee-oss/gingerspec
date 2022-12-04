Feature: Steps for testing relational databases

  Steps related to working with SQL relational databases (currently supports postgresql and mysql). GingerSpec automatically
  detects if a database connection was left open at the end of each scenario and automatically tries to close it, so is not
  necessary to use the step "Then I close database connection" at the end.


  Rule: Opening and closing a connection to a database

    Scenario: Connect to mysql Database
      Given I connect with JDBC to database 'mysql' type 'mysql' on host '${MYSQL_HOST}' and port '3306' with user 'root' and password 'mysql'
      Then I close database connection

    Scenario: Connect to postgres Database
      Given I connect with JDBC to database 'postgres' type 'postgresql' on host '${POSTGRES_HOST}' and port '5432' with user 'postgres' and password 'postgres'
      Then I close database connection

    Scenario: Connect to clickhouse Database
      Given I connect with JDBC to database 'clickhouse' type 'clickhouse' on host '${CLICKHOUSE_HOST}' and port '8123' with user 'clickhouse' and password 'clickhouse'
      Then I close database connection

  Rule: Executing statements on the database

    Scenario: Executing a simple query on a MySQL database
      Given I connect with JDBC to database 'mysql' type 'mysql' on host '${MYSQL_HOST}' and port '3306' with user 'root' and password 'mysql'
      When I execute query 'CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);'
      Then I close database connection

    Scenario: Executing a simple query on a PostgreSQL database
      Given I connect with JDBC to database 'postgres' type 'postgresql' on host '${POSTGRES_HOST}' and port '5432' with user 'postgres' and password 'postgres'
      When I execute query 'CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);'
      Then I close database connection

    Scenario: Executing a simple query on a Clickhouse database
      Given I connect with JDBC to database 'clickhouse' type 'clickhouse' on host '${CLICKHOUSE_HOST}' and port '8123' with user 'clickhouse' and password 'clickhouse'
      When I execute query 'CREATE TABLE IF NOT EXISTS weather1 (city String, temp_lo Int, temp_hi Int, prcp Float, date Date) ENGINE = Memory;'
      Then I close database connection

    Scenario: Execute a query from a file in a MySQL database
      Given I connect with JDBC to database 'mysql' type 'mysql' on host '${MYSQL_HOST}' and port '3306' with user 'root' and password 'mysql'
      When I execute query from 'sql/createWeather.sql'
      Then I check that table 'weather1' is equal to
        | city      | temp_lo | temp_hi | prcp | date       |
        | Caracas   | 15      | 43      | 0.0  | 2004-11-29 |
        | Barcelona | 5       | 37      | 0.4  | 2014-11-29 |
        | Madrid    | 8       | 37      | 0.4  | 2016-11-30 |
      And I execute query 'DELETE FROM weather1 WHERE city = 'Madrid''
      Then I check that table 'weather1' is equal to
        | city      | temp_lo | temp_hi | prcp | date       |
        | Caracas   | 15      | 43      | 0.0  | 2004-11-29 |
        | Barcelona | 5       | 37      | 0.4  | 2014-11-29 |
      Then I close database connection

    Scenario: Execute a query from a file in a PostgreSQL database
      Given I connect with JDBC to database 'postgres' type 'postgresql' on host '${POSTGRES_HOST}' and port '5432' with user 'postgres' and password 'postgres'
      When I execute query from 'sql/createWeather.sql'
      Then I check that table 'weather1' is equal to
        | city      | temp_lo | temp_hi | prcp | date       |
        | Caracas   | 15      | 43      | 0.0  | 2004-11-29 |
        | Barcelona | 5       | 37      | 0.4  | 2014-11-29 |
        | Madrid    | 8       | 37      | 0.4  | 2016-11-30 |
      And I execute query 'DELETE FROM weather1 WHERE city = 'Madrid''
      Then I check that table 'weather1' is equal to
        | city      | temp_lo | temp_hi | prcp | date       |
        | Caracas   | 15      | 43      | 0.0  | 2004-11-29 |
        | Barcelona | 5       | 37      | 0.4  | 2014-11-29 |
      Then I close database connection

    Scenario: Execute a query from a file in a Clickhouse database
      Given I connect with JDBC to database 'clickhouse' type 'clickhouse' on host '${CLICKHOUSE_HOST}' and port '8123' with user 'clickhouse' and password 'clickhouse'
      When I execute query from 'sql/createWeather.clickhouse.sql'
      Then I check that table 'weather1' is equal to
        | city      | temp_lo | temp_hi | prcp | date       |
        | Caracas   | 15      | 43      | 0.0  | 2004-11-29 |
        | Barcelona | 5       | 37      | 0.4  | 2014-11-29 |
        | Madrid    | 8       | 37      | 0.4  | 2016-11-30 |
      And I execute query 'DELETE FROM weather1 WHERE city = 'Madrid''
      Then I check that table 'weather1' is equal to
        | city      | temp_lo | temp_hi | prcp | date       |
        | Caracas   | 15      | 43      | 0.0  | 2004-11-29 |
        | Barcelona | 5       | 37      | 0.4  | 2014-11-29 |
      Then I close database connection

  Rule: Getting data from the database

    Scenario: Executing SELECT statements on a MySQL database
      Given I connect with JDBC to database 'mysql' type 'mysql' on host '${MYSQL_HOST}' and port '3306' with user 'root' and password 'mysql'
      Then I execute query 'CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);'
      Then I execute query 'TRUNCATE weather1'
      Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('San Francisco', 15, 43, 0.0, '2004-11-29');'
      Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Kyiv', 5, 37, 0.4, '2014-11-29');'
      Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Paris', 8, 37, 0.4, '2016-11-30');'
      When I query the database with 'SELECT * FROM weather1;'
      Then I check that result is:
        | city          | temp_lo | temp_hi | prcp | date       |
        | San Francisco | 15      | 43      | 0.0  | 2004-11-29 |
        | Kyiv          | 5       | 37      | 0.4  | 2014-11-29 |
        | Paris         | 8       | 37      | 0.4  | 2016-11-30 |
      Then I check that table 'weather1' is equal to
        | city          | temp_lo | temp_hi | prcp | date       |
        | San Francisco | 15      | 43      | 0.0  | 2004-11-29 |
        | Kyiv          | 5       | 37      | 0.4  | 2014-11-29 |
        | Paris         | 8       | 37      | 0.4  | 2016-11-30 |
      Then I close database connection

    Scenario: Executing SELECT statements on a PostgreSQL database
      Given I connect with JDBC to database 'postgres' type 'postgresql' on host '${POSTGRES_HOST}' and port '5432' with user 'postgres' and password 'postgres'
      Then I execute query 'CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);'
      Then I execute query 'TRUNCATE weather1'
      Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Caracas', 15, 43, 0.0, '2004-11-29');'
      Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Barcelona', 5, 37, 0.4, '2014-11-29');'
      Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Madrid', 8, 37, 0.4, '2016-11-30');'
      When I query the database with 'SELECT * FROM weather1;'
      Then I check that result is:
        | city      | temp_lo | temp_hi | prcp | date       |
        | Caracas   | 15      | 43      | 0.0  | 2004-11-29 |
        | Barcelona | 5       | 37      | 0.4  | 2014-11-29 |
        | Madrid    | 8       | 37      | 0.4  | 2016-11-30 |
      Then I check that table 'weather1' is equal to
        | city      | temp_lo | temp_hi | prcp | date       |
        | Caracas   | 15      | 43      | 0.0  | 2004-11-29 |
        | Barcelona | 5       | 37      | 0.4  | 2014-11-29 |
        | Madrid    | 8       | 37      | 0.4  | 2016-11-30 |
      Then I close database connection

    Scenario: Executing SELECT statements on a Clickhouse database
      Given I connect with JDBC to database 'clickhouse' type 'clickhouse' on host '${CLICKHOUSE_HOST}' and port '8123' with user 'clickhouse' and password 'clickhouse'
      Then I execute query 'CREATE TABLE IF NOT EXISTS weather1 (city String, temp_lo Int, temp_hi Int, prcp Float, date Date) ENGINE = Memory;'
      Then I execute query 'TRUNCATE TABLE weather1;'
      Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Caracas', 15, 43, 0.0, '2004-11-29');'
      Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Barcelona', 5, 37, 0.4, '2014-11-29');'
      Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Madrid', 8, 37, 0.4, '2016-11-30');'
      When I query the database with 'SELECT * FROM weather1;'
      Then I check that result is:
        | city      | temp_lo | temp_hi | prcp | date       |
        | Caracas   | 15      | 43      | 0.0  | 2004-11-29 |
        | Barcelona | 5       | 37      | 0.4  | 2014-11-29 |
        | Madrid    | 8       | 37      | 0.4  | 2016-11-30 |
      Then I check that table 'weather1' is equal to
        | city      | temp_lo | temp_hi | prcp | date       |
        | Caracas   | 15      | 43      | 0.0  | 2004-11-29 |
        | Barcelona | 5       | 37      | 0.4  | 2014-11-29 |
        | Madrid    | 8       | 37      | 0.4  | 2016-11-30 |
      Then I close database connection

    Scenario: Execute a SELECT query from a file in a MySQL database
      Given I connect with JDBC to database 'mysql' type 'mysql' on host '${MYSQL_HOST}' and port '3306' with user 'root' and password 'mysql'
      Then I execute query 'CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);'
      Then I execute query 'TRUNCATE weather1'
      Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('San Francisco', 15, 43, 0.0, '2004-11-29');'
      Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Kyiv', 5, 37, 0.4, '2014-11-29');'
      Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Paris', 8, 37, 0.4, '2016-11-30');'
      When I execute query from 'sql/selectWeather.sql'
      Then I check that result is:
        | city          | temp_lo | temp_hi | prcp | date       |
        | San Francisco | 15      | 43      | 0.0  | 2004-11-29 |
        | Kyiv          | 5       | 37      | 0.4  | 2014-11-29 |
        | Paris         | 8       | 37      | 0.4  | 2016-11-30 |
      Then I close database connection

    Scenario: Execute a SELECT query from a file in a PostgreSQL database
      Given I connect with JDBC to database 'postgres' type 'postgresql' on host '${POSTGRES_HOST}' and port '5432' with user 'postgres' and password 'postgres'
      Then I execute query 'CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);'
      Then I execute query 'TRUNCATE weather1'
      Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Caracas', 15, 43, 0.0, '2004-11-29');'
      Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Barcelona', 5, 37, 0.4, '2014-11-29');'
      Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Madrid', 8, 37, 0.4, '2016-11-30');'
      When I execute query from 'sql/selectWeather.sql'
      Then I check that result is:
        | city      | temp_lo | temp_hi | prcp | date       |
        | Caracas   | 15      | 43      | 0.0  | 2004-11-29 |
        | Barcelona | 5       | 37      | 0.4  | 2014-11-29 |
        | Madrid    | 8       | 37      | 0.4  | 2016-11-30 |
      Then I close database connection

    Scenario: Execute a SELECT query from a file in a Clickhouse database
      Given I connect with JDBC to database 'clickhouse' type 'clickhouse' on host '${CLICKHOUSE_HOST}' and port '8123' with user 'clickhouse' and password 'clickhouse'
      Then I execute query 'CREATE TABLE IF NOT EXISTS weather1 (city String, temp_lo Int, temp_hi Int, prcp Float, date Date) ENGINE = Memory;'
      Then I execute query 'TRUNCATE TABLE weather1;'
      Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Caracas', 15, 43, 0.0, '2004-11-29');'
      Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Barcelona', 5, 37, 0.4, '2014-11-29');'
      Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Madrid', 8, 37, 0.4, '2016-11-30');'
      When I execute query from 'sql/selectWeather.sql'
      Then I check that result is:
        | city      | temp_lo | temp_hi | prcp | date       |
        | Caracas   | 15      | 43      | 0.0  | 2004-11-29 |
        | Barcelona | 5       | 37      | 0.4  | 2014-11-29 |
        | Madrid    | 8       | 37      | 0.4  | 2016-11-30 |
      Then I close database connection


  Rule: Verify the database or the returned data

    Scenario: Verify if a table exists in a MySQL Database
      Given I connect with JDBC to database 'mysql' type 'mysql' on host '${MYSQL_HOST}' and port '3306' with user 'root' and password 'mysql'
      When I execute query 'CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);'
      Then table 'weather1' exists
      Then table 'weather2' doesn't exists
      When I execute query 'DROP TABLE weather1;'
      When I execute query 'CREATE TABLE IF NOT EXISTS weather2 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);'
      Then table 'weather1' doesn't exists
      Then table 'weather2' exists
      Then I execute query 'DROP TABLE weather2;'
      Then I close database connection

    Scenario: Verify if a table exists in a PostgreSQL Database
      Given I connect with JDBC to database 'postgres' type 'postgresql' on host '${POSTGRES_HOST}' and port '5432' with user 'postgres' and password 'postgres'
      When I execute query 'CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);'
      Then table 'weather1' exists
      Then table 'weather2' doesn't exists
      When I execute query 'DROP TABLE weather1;'
      When I execute query 'CREATE TABLE IF NOT EXISTS weather2 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);'
      Then table 'weather1' doesn't exists
      Then table 'weather2' exists
      Then I execute query 'DROP TABLE weather2;'
      Then I close database connection

    Scenario: Verify if a table exists in a Clickhouse Database
      Given I connect with JDBC to database 'clickhouse' type 'clickhouse' on host '${CLICKHOUSE_HOST}' and port '8123' with user 'clickhouse' and password 'clickhouse'
      When I execute query 'CREATE TABLE IF NOT EXISTS weather1 (city String, temp_lo Int, temp_hi Int, prcp Float, date Date) ENGINE = Memory;'
      Then table 'weather1' exists
      Then table 'weather2' doesn't exists
      When I execute query 'DROP TABLE weather1;'
      When I execute query 'CREATE TABLE IF NOT EXISTS weather2 (city String, temp_lo Int, temp_hi Int, prcp Float, date Date) ENGINE = Memory;'
      Then table 'weather1' doesn't exists
      Then table 'weather2' exists
      Then I execute query 'DROP TABLE weather2;'
      Then I close database connection

    Scenario: Store the value returned by a query in an environment variable (MySQL database)
      Given I connect with JDBC to database 'mysql' type 'mysql' on host '${MYSQL_HOST}' and port '3306' with user 'root' and password 'mysql'
      When I execute query from 'sql/createWeather.sql'
      When I execute query from 'sql/selectWeather.sql'
      Then I save the value of the row number '1' and the column with name 'city' in environment variable 'CITY'
      Then I save the value of the row number '2' and the column with name 'temp_hi' in environment variable 'TEMP_BARCELONA'
      Then '${CITY}' matches 'Caracas'
      Then '${TEMP_BARCELONA}' matches '37'
      Then I close database connection

    Scenario: Store the value returned by a query in an environment variable (PostgreSQL database)
      Given I connect with JDBC to database 'postgres' type 'postgresql' on host '${POSTGRES_HOST}' and port '5432' with user 'postgres' and password 'postgres'
      When I execute query from 'sql/createWeather.sql'
      When I execute query from 'sql/selectWeather.sql'
      Then I save the value of the row number '1' and the column with name 'city' in environment variable 'CITY'
      Then I save the value of the row number '2' and the column with name 'temp_hi' in environment variable 'TEMP_BARCELONA'
      Then '${CITY}' matches 'Caracas'
      Then '${TEMP_BARCELONA}' matches '37'
      Then I close database connection

    Scenario: Store the value returned by a query in an environment variable (Clickhouse database)
      Given I connect with JDBC to database 'clickhouse' type 'clickhouse' on host '${CLICKHOUSE_HOST}' and port '8123' with user 'clickhouse' and password 'clickhouse'
      When I execute query from 'sql/createWeather.clickhouse.sql'
      When I execute query from 'sql/selectWeather.sql'
      Then I save the value of the row number '1' and the column with name 'city' in environment variable 'CITY'
      Then I save the value of the row number '2' and the column with name 'temp_hi' in environment variable 'TEMP_BARCELONA'
      Then '${CITY}' matches 'Caracas'
      Then '${TEMP_BARCELONA}' matches '37'
      Then I close database connection

    Scenario: Verify amount ot rows returned from last query (MySQL database)
      Given I connect with JDBC to database 'mysql' type 'mysql' on host '${MYSQL_HOST}' and port '3306' with user 'root' and password 'mysql'
      And I execute query from 'sql/createWeather.sql'
      When I execute query from 'sql/selectWeather.sql'
      Then The last sql query returned at least '1' rows
      Then The last sql query returned exactly '3' rows
      Then The last sql query returned more than '2' rows
      Then The last sql query returned less than '4' rows
      Then I close database connection

    Scenario: Verify amount ot rows returned from last query (PostgreSQL database)
      Given I connect with JDBC to database 'postgres' type 'postgresql' on host '${POSTGRES_HOST}' and port '5432' with user 'postgres' and password 'postgres'
      And I execute query from 'sql/createWeather.sql'
      When I execute query from 'sql/selectWeather.sql'
      Then The last sql query returned at least '1' rows
      Then The last sql query returned exactly '3' rows
      Then The last sql query returned more than '2' rows
      Then The last sql query returned less than '4' rows
      Then I close database connection

    Scenario: Verify amount ot rows returned from last query (Clickhouse database)
      Given I connect with JDBC to database 'clickhouse' type 'clickhouse' on host '${CLICKHOUSE_HOST}' and port '8123' with user 'clickhouse' and password 'clickhouse'
      And I execute query from 'sql/createWeather.clickhouse.sql'
      When I execute query from 'sql/selectWeather.sql'
      Then The last sql query returned at least '1' rows
      Then The last sql query returned exactly '3' rows
      Then The last sql query returned more than '2' rows
      Then The last sql query returned less than '4' rows
      Then I close database connection

    Scenario: Saving the amount of rows returned by last query in a variable for future use (MySQL database)
      Given I connect with JDBC to database 'mysql' type 'mysql' on host '${MYSQL_HOST}' and port '3306' with user 'root' and password 'mysql'
      And I execute query from 'sql/createWeather.sql'
      When I execute query from 'sql/selectWeather.sql'
      Then I save the amount of rows returned by the last query in environment variable 'ROWS'
      And '${ROWS}' is '3'
      Then I close database connection

    Scenario: Saving the amount of rows returned by last query in a variable for future use (PostgreSQL database)
      Given I connect with JDBC to database 'postgres' type 'postgresql' on host '${POSTGRES_HOST}' and port '5432' with user 'postgres' and password 'postgres'
      And I execute query from 'sql/createWeather.sql'
      When I execute query from 'sql/selectWeather.sql'
      Then I save the amount of rows returned by the last query in environment variable 'ROWS'
      And '${ROWS}' is '3'
      Then I close database connection

    Scenario: Saving the amount of rows returned by last query in a variable for future use (Clickhouse database)
      Given I connect with JDBC to database 'clickhouse' type 'clickhouse' on host '${CLICKHOUSE_HOST}' and port '8123' with user 'clickhouse' and password 'clickhouse'
      And I execute query from 'sql/createWeather.clickhouse.sql'
      When I execute query from 'sql/selectWeather.sql'
      Then I save the amount of rows returned by the last query in environment variable 'ROWS'
      And '${ROWS}' is '3'
      Then I close database connection