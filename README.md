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


### Technologies
* Cucumber for test definition   
* TestNG for execution  
* AsyncHTTP Client  
* Selenium   
* JSONPath  
* AssertJ


## Getting Started

After modifying, to check changes in your local project do:  

`mvn clean install -Dmaven.test.skip=true` (tests skip temporarily)  
  
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
  
``` 
     @runOnEnv(METRIC_HOST)
     @skipOnEnv(METRIC_HOST)
```

<br>
  
- **IgnoreTagAspect**  
  
An AspectJ aspect that allows to skip an scenario or a whole feature. To do so, a tag must be used before the scenario or the feature keyword. Additionally an ignored reason can be set.  
  
  
``` 
    @ignore @manual
    @ignore @unimplemented
    @ignore @toocomplex
    @ignore @tillfixed(DCS-XXX)
``` 

   
  <br>
  
- **IncludeTagAspect**: An AspectJ aspect that includes an scenario before the tagged one. It manages parameters as well. Scenario name of the included feature can not contain spaces. Parameters should be wrapped in []  

  
```
   @include(feature:<feature>,scenario:<scenario>)
   @include(feature:<feature>,scenario:<scenario>,params:<params>) 
```
  
  <br>
  
- **LoopTagAspect**: An AspectJ aspect that allows looping over scenarios. Using this tag before an scenario will convert this scenario into an scenario outline
     
  
```
    @loop(LIST_PARAM,NAME)
```  
  
If LIST_PARAM: `-DLIST_PARAM=elem1,elem2,elem3`, pthe escenario is executed three times, first time with NAME=elem1, second time with NAME=elem2 and third time with NAME=elem3
  
  
  <br>
  
- **Background Tag**: An AspectJ aspect included in loopTagAspect that allows conditional backgrounds, or conditional executions of group of steps based in a environmental variable.
  
  
```  
    @background(VAR)        	// Beginning of conditional block of steps  
     Given X When  Y Then  Z
    @/background            	// End of block  
 ```  
Being VAR: `-DVAR=value`  
  
<br>

## Contributing Members to GingerSpec

**QA Team Lead: [Oleksandr Tarasyuk](https://github.com/alejandro2003) (@oleksandr.tarasyuk)**

#### Other Members:

|Name     |  Slack Handle   | 
|---------|-----------------|
|[Jose Fernandez Duque](https://github.com/josefd8)| @josefd8        |


<br>

## Contact
* You can find a more in depth docs/manuals [here](https://confluence.vptech.eu/pages/viewpage.action?spaceKey=QAP&title=Automation+with+Java+BDD+Framework+Ecosystem).  