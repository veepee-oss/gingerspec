<div align="center">

```  

  /$$$$$$  /$$                                          /$$$$$$                               
 /$$__  $$|__/                                         /$$__  $$                              
| $$  \__/ /$$ /$$$$$$$   /$$$$$$   /$$$$$$   /$$$$$$ | $$  \__/  /$$$$$$   /$$$$$$   /$$$$$$$
| $$ /$$$$| $$| $$__  $$ /$$__  $$ /$$__  $$ /$$__  $$|  $$$$$$  /$$__  $$ /$$__  $$ /$$_____/
| $$|_  $$| $$| $$  \ $$| $$  \ $$| $$$$$$$$| $$  \__/ \____  $$| $$  \ $$| $$$$$$$$| $$      
| $$  \ $$| $$| $$  | $$| $$  | $$| $$_____/| $$       /$$  \ $$| $$  | $$| $$_____/| $$      
|  $$$$$$/| $$| $$  | $$|  $$$$$$$|  $$$$$$$| $$      |  $$$$$$/| $$$$$$$/|  $$$$$$$|  $$$$$$$
 \______/ |__/|__/  |__/ \____  $$ \_______/|__/       \______/ | $$____/  \_______/ \_______/
                         /$$  \ $$                              | $$                          
                        |  $$$$$$/                              | $$                          
                         \______/                               |__/                          

```
[![GingerSpec pipeline](https://github.com/veepee-oss/gingerspec/actions/workflows/maven-build.yml/badge.svg)](https://github.com/veepee-oss/gingerspec/actions/workflows/maven-build.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.privaliatech/gingerspec/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.privaliatech/gingerspec)
[![License](https://img.shields.io/badge/License-ISC-blue.svg)](https://opensource.org/licenses/ISC)
![GitHub Release Date](https://img.shields.io/github/release-date/veepee-oss/gingerspec)
[![javadoc](https://javadoc.io/badge2/com.github.privaliatech/gingerspec/javadoc.svg)](https://javadoc.io/doc/com.github.privaliatech/gingerspec)
</div>

<div align="center"><h2>Acceptance Test library. General purpose automation framework.</h2></div>

<p align="center">
    <a href="#project-introobjective">Intro</a> |
    <a href="#documentation">Documentation</a> |
    <a href="#requirements">Requirements</a> |
    <a href="#technologies">Technologies</a> |
    <a href="#using-gingerspec">Using GingerSpec</a> |
    <a href="#aspects">Aspects</a> |
    <a href="#steps">Steps</a> |
    <a href="#contributing-members-to-gingerspec">Contribute</a>
</p>

## Project Intro/Objective
The purpose of this project is to provide a generic BDT (behaviour driven testing) library with common BDD steps and extended gherkin language.

GingerSpec provides common functionality that can be reused by different test projects, It encourages code reusability, as we test (in most of the cases) the same, it will help our QA Community to get our objectives much faster. It focuses on the reuse of actions (also steps or key tabs) that implement low level functionality and that can be organized to create much more complex features.

<br>

<div align="center">
  <img src="https://github.com/veepee-oss/gingerspec/wiki/resources/gingerspec_highlights.png" alt="GingerSpec Highlights">
</div>

## Documentation
* [Project wiki](https://github.com/veepee-oss/gingerspec/wiki)
* [Steps documentation an examples](https://veepee-oss.github.io/gingerspec/)
* [Javadoc](https://javadoc.io/doc/com.github.privaliatech/gingerspec/latest/index.html)  

### Requirements
* Java 8+
* Maven 3+

### Technologies
* Cucumber for test definition   
* TestNG for execution    
* Selenium
* Appium     
* AssertJ
* SQL (PostgreSQL, MySQL, Clickhouse)
* rest-assured (Rest API testing)
* GraphQl
* Swagger (2.x, 3.x)
* SSH support
* Files manipulation
* WebServices (SOAP)
* Kafka & ZooKeeper

## Using GingerSpec

We **strongly** suggest to make use of the special archetype for GingerSpec based projects: [gingerspec-starter](https://github.com/PrivaliaTech/gingerspec-starter). Just run the following command in your terminal (replace the values for groupId, version and artifactId as needed):

``` 
mvn archetype:generate \
  -DarchetypeGroupId=com.github.privaliatech \
  -DarchetypeArtifactId=gingerspec-starter \
  -DgroupId=eu.vptech \
  -Dversion=1.0-SNAPSHOT \
  -DartifactId=myproject
``` 

This will create a ready-to-use project based on a template with best practices and examples that you can modify in the way you see fit for your needs


## Aspects  
  
As part of GingerSpec implementation, there are a couple of tags which may be useful for your scenarios:  

### :envelope: Slack integration: 
Send a message to a Slack channel(s) when your scenario fails. [Read more](https://github.com/veepee-oss/gingerspec/wiki/Gherkin-tags#slack-tag).

```gherkin
@slack[#mychannel]
    Scenario: A successful response with a valid body is returned
        Given I securely send requests to 'jsonplaceholder.typicode.com:443'
        When I send a 'GET' request to '/posts'
        Then the service response status must be '200'
```

### :bar_chart: Jira integration
Update tickets in jira based onn tests results and perform conditional execution based on status of tickets. [Read more](https://github.com/veepee-oss/gingerspec/wiki/Gherkin-tags#jira-tag).

```gherkin
@jira[QMS-990]
Scenario: A new element is inserted via a POST call
    Given I send requests to '${REST_SERVER_HOST}:3000'
    When I send a 'POST' request to '/posts' based on 'schemas/mytestdata.json' as 'json'
    Then the service response status must be '201'
```

### :no_entry: Ignore scenarios
Easily ignore scenarios using the @ignore tag. [Read more](https://github.com/veepee-oss/gingerspec/wiki/Gherkin-tags#ignore-tag).

```gherkin
  @ignore @toocomplex
  Scenario: Ignored scenario (too complex)
    Given I run '[ "THIS SHOULDNT HAVE BEEN RUN" = "!UNEXISTANT_VAR" ]' locally
    Given I run 'exit 1' locally
```

### :gear: Use and create variables
Use annd create variables directly in feature files. [Read more](https://github.com/veepee-oss/gingerspec/wiki/Gherkin-variables).

````gherkin
  Scenario: A new element is inserted via a POST call
    Given I send requests to '${REST_SERVER_HOST}:3000'
    When I send a 'POST' request to '/posts' based on 'schemas/mytestdata.json' as 'json'
    Then the service response status must be '201'
    And I save element '$.title' in environment variable 'TITLE'
    Then '${TITLE}' matches 'This is a test'
````

### :arrows_counterclockwise: Conditional execution
Allow the conditional execution of scenarios based on a given environment variable. [Read more](https://github.com/veepee-oss/gingerspec/wiki/Gherkin-tags#runonenv-tag).

```gherkin
@runOnEnv(SECS)
Scenario: Dummy scenario
     And I wait '${SECS}' seconds

@skipOnEnv(SECS_2)
Scenario: Dummy scenario
    And I wait '${SECS}' seconds
```

And [many others more!](https://github.com/veepee-oss/gingerspec/wiki/Gherkin-tags) :yum:
  
## Steps

GingerSpec contains tons of predefined Gherkin steps ready to use in your own features. The steps cover a wide range of functionality, from steps for testing Rest endpoints, perform front-end etsting using selenium, and even for testing kafka services!


_[Testing Rest services](https://veepee-oss.github.io/gingerspec/steps-for-testing-rEST-aPIs.html)_
```
  Scenario: A successful response with a valid body is returned
    Given I securely send requests to 'jsonplaceholder.typicode.com:443'
    When I send a 'GET' request to '/posts'
    Then the service response status must be '200'
    And I save element '$.[0].userId' in environment variable 'USER_ID'
    Then '${USER_ID}' matches '1'
    
  Scenario: Add the body to be sent directly
    Given I securely send requests to 'jsonplaceholder.typicode.com:443'
    When I send a 'POST' request to '/posts' with body
        """
            {
              "userId": 1,
              "title": "This is a test",
              "body": "This is a test"
            }
          """
    Then the service response status must be '201'
```


_[Testing a web page](https://veepee-oss.github.io/gingerspec/steps-for-testing-web-pages.html)_
```
  @web
  Scenario: Fill the form and click the submit button
    Given I go to 'http://demoqa.com/text-box'
    And I type 'John' on the element with 'id:userName'
    And I type 'john.smith@email.com' on the element with 'id:userEmail'
    And I type '123 fake address' on the element with 'id:currentAddress'
    When I scroll down until the element with 'id:submit' is visible
    And I click on the element with 'id:submit'
    Then at least '1' elements exists with 'id:output'
```

_[Testing database](https://veepee-oss.github.io/gingerspec/steps-for-testing-relational-databases.html)_
```
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
    Then I check that table 'weather1' is iqual to
      | city          | temp_lo | temp_hi | prcp | date       |
      | San Francisco | 15      | 43      | 0.0  | 2004-11-29 |
      | Kyiv          | 5       | 37      | 0.4  | 2014-11-29 |
      | Paris         | 8       | 37      | 0.4  | 2016-11-30 |

```
  

[And many many more!](https://veepee-oss.github.io/gingerspec/index.html)  

  
## Contributing Members to GingerSpec

**QA Team Lead: [Oleksandr Tarasyuk](https://github.com/alejandro2003) (@oleksandr.tarasyuk)**

#### Other Members:

|Name     |  Slack Handle   | 
|---------|-----------------|
|[Jose Fernandez Duque](https://github.com/josefd8)| @josefd8        |
