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

Privalia Acceptance Test library. Testing runtime to rule over Privalia's acceptance tests


[![Build Status](https://travis-ci.org/rest-assured/rest-assured.svg)](https://travis-ci.org/rest-assured/rest-assured)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.rest-assured/rest-assured/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.rest-assured/rest-assured)
[![Javadoc](https://javadoc-badge.appspot.com/io.rest-assured/rest-assured.svg)](http://www.javadoc.io/doc/io.rest-assured/rest-assured)

## Project Intro/Objective
The purpose of this project is to provide a generic BDT (behaviour driven testing) library with common BDD steps and extended gherkin language.

GingerSpec provides common functionality that can be reused by different test projects, It encourage code reusability, as we test (in most of the cases) the same, it will help our QA Community to get our objectives much faster. It focus on the reuse of actions (also steps or keytabs) that implement low level functionality and that can be organized to create much more complex features 


### Technologies
* Cucumber for test definition   
* TestNG for execution    
* Selenium     
* AssertJ
* SQL (PostgreSQL & MySQL)
* rest-assured
* SSH support
* Files manipulation
* WebServices
* Kafka & ZooKeeper


## Getting Started

After modifying, to check changes in your local project do:  

`mvn clean install -Dmaven.test.skip=true` (tests skip temporarily)  

If you want to execute all the integration tests in the library
  
#### Execution  
  
Individual tests are supposed to be executed as follows:  
  
` 
mvn verify [-D\<ENV_VAR>=\<VALUE>] [-Dit.test=\<TEST_TO_EXECUTE>|-Dgroups=\<GROUP_TO_EXECUTE>] 
`  
  
Examples:  
  
_**single class execution**_  
  
` mvn verify -DSECS=AGENT_LIST=1,2 -Dit.test=com.privalia.qa.ATests.LoopTagAspectIT `  
  
_**group execution**_  
  
` mvn verify -DSECS=5 -Dgroups=hol `  

_**print log at DEBUG level when running a test**_

` mvn verify -DSECS=AGENT_LIST=1,2 -Dit.test=com.privalia.qa.ATests.LoopTagAspectIT -DlogLevel=DEBUG`

_**-DSELENIUM_GRID and -DFORCE_BROWSER for Selenium features**_

` mvn verify -Dit.test=com.privalia.myproject.mypackage.CucumberSeleniumIT -DSELENIUM_GRID=127.0.0.1:4444`

_**-Dmaven.failsafe.debug to debug with maven and IDE.**_

` mvn verify -DSECS=AGENT_LIST=1,2 -Dit.test=com.privalia.qa.ATests.LoopTagAspectIT -Dmaven.failsafe.debug`

<br>

## Using the library

You must use the following dependency in your testng project to get access to all GingerSpec library functionality

``` 
<dependency>
      <groupId>com.privalia</groupId>
      <artifactId>gingerspec</artifactId>
      <version>1.0.0</version>
</dependency>
``` 

However, we strongly suggest you to make use of the special archetype for GingerSpec based projects: '**automation-archetype**'. Just run the following command in your terminal

``` 
mvn -U archetype:generate -DarchetypeGroupId=com.privalia -DarchetypeArtifactId=automation-archetype
``` 

This will create a ready-to-use project based on a template with best practices and examples that you can modify in the way you see fit for your needs



## Aspects  
  
As part of GingerSpec implementation, there are a couple of AspectJ aspects which may be useful for your scenarios:  
  
- **RunOnTagAspect**:  Allow the conditional execution of scenarios based on a given environment variable
  
- **IgnoreTagAspect**: An AspectJ aspect that allows to skip an scenario or a whole feature. To do so, a tag must be used before the scenario or the feature keyword. Additionally an ignored reason can be set.  
  
- **IncludeTagAspect**: An AspectJ aspect that includes an scenario before the tagged one. It manages parameters as well. Scenario name of the included feature can not contain spaces. Parameters should be wrapped in []  

- **LoopTagAspect**: An AspectJ aspect that allows looping over scenarios. Using this tag before an scenario will convert this scenario into an scenario outline
  
- **BackgroundTagAspect**: An AspectJ aspect included in loopTagAspect that allows conditional backgrounds, or conditional executions of group of steps based in a environmental variable.

- **ReplacementAspect**: Allows the use of variables in the Feature file. Variables are enclosed in #{}, ${}, @{} and !{} symbols and could be global (feature level) or local (scenario level)
  
- **LogTagAspect**: Allows comments in the feature file to be printed in console when tests are executed.
  
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


<br>

## Contact
* You can find a more in depth docs/manuals [here](https://confluence.vptech.eu/pages/viewpage.action?spaceKey=QAP&title=Automation+with+Java+BDD+Framework+Ecosystem).  