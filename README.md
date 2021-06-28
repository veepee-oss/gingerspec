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
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![GitHub Release Date](https://img.shields.io/github/release-date/veepee-oss/gingerspec)
[![javadoc](https://javadoc.io/badge2/com.github.privaliatech/gingerspec/javadoc.svg)](https://javadoc.io/doc/com.github.privaliatech/gingerspec)

Acceptance Test library. General purpose automation framework.

## Project Intro/Objective
The purpose of this project is to provide a generic BDT (behaviour driven testing) library with common BDD steps and extended gherkin language.

GingerSpec provides common functionality that can be reused by different test projects, It encourages code reusability, as we test (in most of the cases) the same, it will help our QA Community to get our objectives much faster. It focuses on the reuse of actions (also steps or key tabs) that implement low level functionality and that can be organized to create much more complex features.

## Documentation
* [Project wiki](https://github.com/veepee-oss/gingerspec/wiki)
* [Javadoc](https://javadoc.io/doc/com.github.privaliatech/gingerspec/latest/index.html)  

### Requirements
* Java 8+
* Maven 3+
* Docker/docker-compose (for testing)


### Technologies
* Cucumber for test definition   
* TestNG for execution    
* Selenium
* Appium     
* AssertJ
* SQL (PostgreSQL & MySQL)
* rest-assured (Rest API testing)
* SSH support
* Files manipulation
* WebServices (SOAP)
* Kafka & ZooKeeper


## Getting Started

Check the following articles to get more information about how to use GingerSpec:

* [Technical documentation](https://github.com/veepee-oss/gingerspec/wiki/Technical-documentation)
* [Getting started](https://github.com/veepee-oss/gingerspec/wiki/Getting-started)  
* [Running your tests](https://github.com/veepee-oss/gingerspec/wiki/Running-your-tests)


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
  
As part of GingerSpec implementation, there are a couple of AspectJ aspects which may be useful for your scenarios:  
  
- [RunOnTagAspect](https://github.com/veepee-oss/gingerspec/wiki/Gherkin-tags#runonenv-tag):  Allow the conditional execution of scenarios based on a given environment variable
  
- [IgnoreTagAspect](https://github.com/veepee-oss/gingerspec/wiki/Gherkin-tags#ignore-tag): An AspectJ aspect that allows to skip an scenario or a whole feature. To do so, a tag must be used before the scenario or the feature keyword. Additionally, an ignored reason can be set.  
  
- [IncludeTagAspect](https://github.com/veepee-oss/gingerspec/wiki/Gherkin-tags#include-tag): An AspectJ aspect that includes an scenario before the tagged one. It manages parameters as well. Scenario name of the included feature cannot contain spaces. Parameters should be wrapped in []  

- [LoopTagAspect](https://github.com/veepee-oss/gingerspec/wiki/Gherkin-tags#loop-tag): An AspectJ aspect that allows looping over scenarios. Using this tag before an scenario will convert this scenario into a scenario outline.
  
- [BackgroundTagAspect](https://github.com/veepee-oss/gingerspec/wiki/Gherkin-tags#background-tag): An AspectJ aspect included in loopTagAspect that allows conditional backgrounds, or conditional executions of group of steps based in a environmental variable.

- [ReplacementAspect](https://github.com/veepee-oss/gingerspec/wiki/Gherkin-variables): Allows the use of variables in the Feature file. Variables are enclosed in ${}.
  
- [LogTagAspect](https://github.com/veepee-oss/gingerspec/wiki/Gherkin-tags#log-tag): Allows comments in the feature file to be printed in console when tests are executed.
  
  
## Steps

GingerSpec contains tons of predefined Gherkin steps ready to use in your own features. The steps cover a wide range of functionality, from steps for testing Rest endpoints, perform front-end etsting using selenium, and even for testing kafka services!


_Testing Rest services_
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


_Testing a web page_
```
  Scenario: Fill the form and click the submit button
    Given I go to 'http://demoqa.com/text-box'
    And I type 'John' on the element with 'id:userName'
    And I type 'john.smith@email.com' on the element with 'id:userEmail'
    And I type '123 fake address' on the element with 'id:currentAddress'
    When I scroll down until the element with 'id:submit' is visible
    And I click on the element with 'id:submit'
    Then at least '1' elements exists with 'id:output'
```

_Testing database_
```
  @sql
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
  

And many many more!  

  
## Contributing Members to GingerSpec

**QA Team Lead: [Oleksandr Tarasyuk](https://github.com/alejandro2003) (@oleksandr.tarasyuk)**

#### Other Members:

|Name     |  Slack Handle   | 
|---------|-----------------|
|[Jose Fernandez Duque](https://github.com/josefd8)| @josefd8        |
