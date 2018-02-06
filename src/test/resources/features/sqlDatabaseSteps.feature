Feature: SQL Database Steps

  Feature Steps related to working with SQL relational databases (currently supports postgresql and mysql)
          You can use the Docker images of mysql and postgresql for testing this feature
          docker pull mysql
          docker run -d -p 3306:3306 --name mysql -e MYSQL_ROOT_PASSWORD=mysql -d mysql:latest

          docker pull postgres
          docker run -d -p 5432:5432 --name postgres -e POSTGRES_PASSWORD=postgres -d postgres:latest

  Scenario: Connect to mysql Database
    Given I connect with JDBC to database 'mysql' type 'mysql' on host '172.17.0.1' and port '3306' with user 'root' and password 'mysql'
    Then I close database connection

  Scenario: Connect to postgres Database
    Given I connect with JDBC to database 'postgres' type 'postgresql' on host '172.17.0.1' and port '5432' with user 'postgres' and password 'postgres'
    Then I close database connection

  Scenario: Executing a query on a MySQL database
    Given I connect with JDBC to database 'mysql' type 'mysql' on host '172.17.0.1' and port '3306' with user 'root' and password 'mysql'
    When I execute query 'CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);'
    Then I close database connection

  Scenario: Executing a query on a PostgreSQL database
    Given I connect with JDBC to database 'postgres' type 'postgresql' on host '172.17.0.1' and port '5432' with user 'postgres' and password 'postgres'
    When I execute query 'CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);'
    Then I close database connection

  Scenario: Verify if a table exists in a MySQL Database
    Given I connect with JDBC to database 'mysql' type 'mysql' on host '172.17.0.1' and port '3306' with user 'root' and password 'mysql'
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
    Given I connect with JDBC to database 'postgres' type 'postgresql' on host '172.17.0.1' and port '5432' with user 'postgres' and password 'postgres'
    When I execute query 'CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);'
    Then table 'weather1' exists
    Then table 'weather2' doesn't exists
    When I execute query 'DROP TABLE weather1;'
    When I execute query 'CREATE TABLE IF NOT EXISTS weather2 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);'
    Then table 'weather1' doesn't exists
    Then table 'weather2' exists
    Then I execute query 'DROP TABLE weather2;'
    Then I close database connection

  Scenario: Executing SELECT statements on a MySQL database
    Given I connect with JDBC to database 'mysql' type 'mysql' on host '172.17.0.1' and port '3306' with user 'root' and password 'mysql'
    Then I execute query 'CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);'
    Then I execute query 'TRUNCATE weather1'
    Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('San Francisco', 15, 43, 0.0, '2004-11-29');'
    Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Kyiv', 5, 37, 0.4, '2014-11-29');'
    Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Paris', 8, 37, 0.4, '2016-11-30');'
    When I query the database with 'SELECT * FROM weather1;'
    Then I check that result is:
      | city           | temp_lo | temp_hi  | prcp  | date      |
      | San Francisco  |  15	 |    43	|  0.0	|2004-11-29 |
      | Kyiv	       |   5	 |    37	|  0.4	|2014-11-29 |
      | Paris	       |   8	 |    37	|  0.4	|2016-11-30 |
    Then I check that table 'weather1' is iqual to
      | city           | temp_lo | temp_hi  | prcp  | date      |
      | San Francisco  |  15	 |    43	|  0.0	|2004-11-29 |
      | Kyiv	       |   5	 |    37	|  0.4	|2014-11-29 |
      | Paris	       |   8	 |    37	|  0.4	|2016-11-30 |
    And I close database connection


  Scenario: Executing SELECT statements on a PostgreSQL database
    Given I connect with JDBC to database 'postgres' type 'postgresql' on host '172.17.0.1' and port '5432' with user 'postgres' and password 'postgres'
    Then I execute query 'CREATE TABLE IF NOT EXISTS weather1 (city varchar(80), temp_lo int, temp_hi int, prcp real, date date);'
    Then I execute query 'TRUNCATE weather1'
    Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Caracas', 15, 43, 0.0, '2004-11-29');'
    Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Barcelona', 5, 37, 0.4, '2014-11-29');'
    Then I execute query 'INSERT INTO weather1 (city, temp_lo, temp_hi, prcp, date) VALUES ('Madrid', 8, 37, 0.4, '2016-11-30');'
    When I query the database with 'SELECT * FROM weather1;'
    Then I check that result is:
      | city           | temp_lo | temp_hi  | prcp  | date      |
      | Caracas        |  15	 |    43	|  0.0	|2004-11-29 |
      | Barcelona      |   5	 |    37	|  0.4	|2014-11-29 |
      | Madrid	       |   8	 |    37	|  0.4	|2016-11-30 |
    Then I check that table 'weather1' is iqual to
      | city           | temp_lo | temp_hi  | prcp  | date      |
      | Caracas        |  15	 |    43	|  0.0	|2004-11-29 |
      | Barcelona      |   5	 |    37	|  0.4	|2014-11-29 |
      | Madrid	       |   8	 |    37	|  0.4	|2016-11-30 |
    And I close database connection
