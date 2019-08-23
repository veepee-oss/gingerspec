# Changelog

## 2.0.2

* Updated jackson-databind dependency to resolve security warning in github

* Added configuration to automatically publish javadoc on github pages on every release

* Added documentation section in README file to access javadoc and wiki

* Fixed bug when taking a screenshot on selenium tests

* All integration tests are now executed on the travis pipeline

## 2.0.1

* Updated dependencies so now the library can work with jdk > 1.8

* Selenium tests can now be executed in selenium standalone nodes (no grid necessary). Check README for more information

## 2.0.0

* Cucumber java was updated to the latest version

* A lot of functionality was rewritten to work with the new Cucumber APIs

* Console output is now controlled by new formatter plugging, allowing more colors and better looking console messages


## 0.1.0

* Config files for the projects

* Log Aspect to print some logs at INFO level

* New Kafka steps, Kafka with Avro format

* Refactor hardcoded names

* SOAP Web services src/main/java/com/privalia/qa/specs/SoapServiceGSpec.java,
  src/main/java/com/privalia/qa/utils/SoapServiceUtils.java
  
* SQL Hook

* Feature take full-screenshots (for large pages)

* Selenium upgrade to 3.9.1 and minor fixes

* Parse files steps src/main/java/com/privalia/qa/specs/FileParserGSpec.java,
  src/main/java/com/privalia/qa/utils/FileParserUtils.java

* Fix captureEvidence function for Selenium

* Cucumber-java dependency was upgraded (1.1.6 -> 1.1.8)

* New src/main/java/com/privalia/qa/specs/RestSpec.java with RestAssured implementation for 
  Rest API logic, new feature file and changes in CommonG, HookGSpec
  
* New logic for Relation databases: MySQL and Postgres
                        src/main/java/com/privalia/qa/specs/SqlDatabaseGSpec.java
                        src/main/java/com/privalia/qa/utils/SqlUtils.java
                        src/test/java/com/privalia/qa/ATests/SqlDatabaseStepsIT.java
                        src/test/java/com/privalia/qa/utils/SqlUtilsTest.java
                        src/test/resources/features/sqlDatabaseSteps.feature
                        src/main/java/com/privalia/qa/specs/CommonG.java
                        
* New message when assert fails in "the service response status must be" step
* Refactoring and ignoring tests
* Set web port for "we are in page" step
* Fix the regex in "the service response status..." step
                        src/main/java/com/privalia/qa/specs/ThenGSpec.java
                        
* Format the logger
                        src/main/java/com/privalia/qa/utils/CukesGHooks.java
                        
* "modifydata" function migration and split into json|string
                        src/main/java/com/privalia/qa/specs/CommonG.java
                        src/main/java/com/privalia/qa/utils/JsonUtils.java
                        
* Fixed BrowsersDataProviderAspect, fix pointcut. Users can force the browser by using -DFORCE_BROWSER
                        src/main/java/com/privalia/qa/aspects/BrowsersDataProviderAspect.java
                        
* Comparing json against a defined schema
                        src/main/java/com/privalia/qa/specs/ThenGSpec.java
                        
* Mockito dependency deletion, dependencies for json schema. Upgraded jackson-databind to 2.8.10.
* Assert deletion and test change
                        src/test/java/com/privalia/qa/specs/MongoToolsIT.java
                        src/test/java/com/privalia/qa/specs/CassandraToolsIT.java
                        
* New BigData spec and moving methods
                        src/main/java/com/privalia/qa/specs/BigDataGSpec.java
                        src/main/java/com/privalia/qa/specs/GivenGSpec.java
                        src/main/java/com/privalia/qa/specs/ThenGSpec.java
                        src/main/java/com/privalia/qa/specs/WhenGSpec.java

* Selenium steps with poling and alerts
* New Selenium Spec file and tests
                        src/main/java/com/privalia/qa/specs/SeleniumGSpec.java
                        
* Added assertion and exception to "I save element.." step
                        src/main/java/com/privalia/qa/specs/GivenGSpec.java
                        
* Removed unnecesary dependency
                        src/main/java/com/privalia/qa/specs/ThenGSpec.java
                        
* Set and clear headers src/main/java/com/privalia/qa/specs/WhenGSpec.java
                        src/main/java/com/privalia/qa/specs/ThenGSpec.java
                        src/main/java/com/privalia/qa/specs/GivenGSpec.java  
                        
* Deleting not needed files and dcos methods
* Adding patch call to src/main/java/com/privalia/qa/specs/CommonG.java

* Fix "my app is running" step set rest port
* Delete old jenkinsfile
* Updated URL of Jira server
* Changes all references in code to Privalia
* Users can now specify glue files via @CucumberOptions annotation
* Updated references to parent pom
* Migration from Stratio bdt-lib to gitlab
