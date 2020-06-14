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

[![Build Status](https://travis-ci.com/PrivaliaTech/gingerspec.svg)](https://travis-ci.com/PrivaliaTech/gingerspec)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.privaliatech/gingerspec/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.privaliatech/gingerspec)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![GitHub Release Date](https://img.shields.io/github/release-date/PrivaliaTech/gingerspec)

Privalia Acceptance Test library. Testing runtime to rule over Privalia's acceptance tests

## Project Intro/Objective
The purpose of this project is to provide a generic BDT (behaviour driven testing) library with common BDD steps and extended gherkin language.

GingerSpec provides common functionality that can be reused by different test projects, It encourage code reusability, as we test (in most of the cases) the same, it will help our QA Community to get our objectives much faster. It focus on the reuse of actions (also steps or keytabs) that implement low level functionality and that can be organized to create much more complex features

## Documentation
* [Project wiki](https://github.com/PrivaliaTech/gingerspec/wiki)
* [Javadoc](https://privaliatech.github.io/gingerspec/)  

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

* [Compiling the library](https://github.com/PrivaliaTech/gingerspec/wiki/Getting-started#compiling-the-library)
* [Testing with alternative SDK](https://github.com/PrivaliaTech/gingerspec/wiki/Getting-started#testing-with-alternative-sdk)  
* [Tests execution](https://github.com/PrivaliaTech/gingerspec/wiki/Getting-started#tests-execution)

<br>

## Using the library

You must use the following dependency in your testng project to get access to all GingerSpec library functionality

``` 
<dependency>
    <groupId>com.github.privaliatech</groupId>
    <artifactId>gingerspec</artifactId>
    <version>2.1.0-RC3</version>
</dependency>
``` 

However, we **strongly** suggest to make use of the special archetype for GingerSpec based projects: [gingerspec-starter](https://github.com/PrivaliaTech/gingerspec-starter). Just run the following command in your terminal

``` 
mvn -U archetype:generate -DarchetypeGroupId=com.github.privaliatech -DarchetypeArtifactId=gingerspec-starter
``` 

This will create a ready-to-use project based on a template with best practices and examples that you can modify in the way you see fit for your needs



## Aspects  
  
As part of GingerSpec implementation, there are a couple of AspectJ aspects which may be useful for your scenarios:  
  
- [RunOnTagAspect](https://github.com/PrivaliaTech/gingerspec/wiki/Gherkin-extended-language#runonenv-tag):  Allow the conditional execution of scenarios based on a given environment variable
  
- [IgnoreTagAspect](https://github.com/PrivaliaTech/gingerspec/wiki/Gherkin-extended-language#ignore-tags): An AspectJ aspect that allows to skip an scenario or a whole feature. To do so, a tag must be used before the scenario or the feature keyword. Additionally an ignored reason can be set.  
  
- [IncludeTagAspect](https://github.com/PrivaliaTech/gingerspec/wiki/Gherkin-extended-language#include-tag): An AspectJ aspect that includes an scenario before the tagged one. It manages parameters as well. Scenario name of the included feature can not contain spaces. Parameters should be wrapped in []  

- [LoopTagAspect](https://github.com/PrivaliaTech/gingerspec/wiki/Gherkin-extended-language#loop-tag): An AspectJ aspect that allows looping over scenarios. Using this tag before an scenario will convert this scenario into an scenario outline
  
- [BackgroundTagAspect](https://github.com/PrivaliaTech/gingerspec/wiki/Gherkin-extended-language#background-extension): An AspectJ aspect included in loopTagAspect that allows conditional backgrounds, or conditional executions of group of steps based in a environmental variable.

- [ReplacementAspect](https://github.com/PrivaliaTech/gingerspec/wiki/Gherkin-extended-language#environment-variables): Allows the use of variables in the Feature file. Variables are enclosed in #{}, ${}, @{} and !{} symbols and could be global (feature level) or local (scenario level)
  
- [LogTagAspect](https://github.com/PrivaliaTech/gingerspec/wiki/Gherkin-extended-language#log-aspect): Allows comments in the feature file to be printed in console when tests are executed.
  
  <br>
  
  
## Steps

GingerSpec contains tons of predefined Gherkin steps ready to use in your own features. The steps cover a wide range of functionality, from steps for testing Rest endpoints, perform front-end etsting using selenium, and even for testing kafka services!


_Testing Rest services_
```
  Scenario: A successful response with a valid body is returned
    Given I securely send requests to 'jsonplaceholder.typicode.com:443'
    When I send a 'GET' request to '/posts'
    Then the service response status must be '200'
    And I save element '$.[0].userId' in environment variable 'USER_ID'
    Then '!{USER_ID}' matches '1'
```


_Testing a web page_
```
  Scenario: Test send keys function
    Given My app is running in 'mytestpage.com'
    When I browse to '/registration'
    When '1' elements exists with 'id:name_3_firstname'
    Then I type 'testUser' on the element on index '0'
    Then I send 'ENTER' on the element on index '0'
```

_Testing a kafka service_
```
Scenario: Send message to kafka topic
    Given I connect to kafka at 'myzookeeperaddress:2181'
    Given I create a Kafka topic named 'testqa' if it doesn't exists
    Then A kafka topic named 'testqa' exists
    Given I send a message 'hello' to the kafka topic named 'testqa'
    Then The kafka topic 'testqa' has a message containing 'hello'
    Then I close the connection to kafka
```
  

And many many more!  

  
## Contributing Members to GingerSpec

**QA Team Lead: [Oleksandr Tarasyuk](https://github.com/alejandro2003) (@oleksandr.tarasyuk)**

#### Other Members:

|Name     |  Slack Handle   | 
|---------|-----------------|
|[Jose Fernandez Duque](https://github.com/josefd8)| @josefd8        |
