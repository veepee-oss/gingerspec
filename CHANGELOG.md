# Changelog

## 2.2.15

* Added small change to add some style to html documentation

* Edited Java SDKs for pipelines testing and execution

* Added Support for graphql

* Minor improve for some selenium steps: Added posibility of ignoring case when checking text of page source and element string contains

## 2.2.14

* Added support for clickhouse DB

* Bump commons-text from 1.9 to 1.10.0

* Bump postgresql from 42.3.3 to 42.3.8

* Bump commons-configuration2 from 2.7 to 2.8.0

* Bump jsch from 0.1.53 to 0.1.54

* Bump soa-model-core from 1.6.0 to 1.6.4

* Bump log4j-api from 2.16.0 to 2.17.1

* Bump log4j-core from 2.14.1 to 2.17.1

* Bump commons-text from 1.9 to 1.10.0

## 2.2.13

* Created a new formatter capable of transforming the feature files into a html document. This is for auto-generating the documentation that will be published in GitHub pages (https://veepee-oss.github.io/gingerspec/index.html)

* Some unnecessary features and runner classes were removed, general code-cleanup

* Completely rewrite the Selenium, Rest and SQL features, so they can be displayed with nice sections in the final html page. The Selenium feature was rewritten using examples from https://testpages.herokuapp.com

* Many Selenium steps were rewritten to avoid using a previous step for storing webelements

* Test page used during the pipeline execution for testing the selenium steps was removed and is no longer using demoqa.com, but a static page from https://testpages.herokuapp.com

* Improved debug messages for selenium steps

* @rest annotation was removed. Rest steps will now automatically bootstrap a new instance of rest-assured client if it doesn't exists

* @sql annotation was removed. Now GingerSpec will automatically close any open database connection at the end of the scenario

## 2.2.12

* Updated step so Rest client from rest assured is not initialized using a port if the user does not specify one

* Removed unnecessary reference to RestGSpec from BigDataGSpec

* Completely removed async-http-client to make Rest requests. Now GingerSpec only uses rest-assured internally

* Created a new formatted to automatically generate features documentation (WIP)

* Added a step for saving the amount of rows returned in the last SQL query in a variable for future use in the scenario

* Added new step to verify the amount of rows returned by an SQL query

* ReplacementAspect feature was reorganized and more examples were added

* EnvPropertyLookup can now also read the env variable from a previously saved variable in the same scenario, not only from CLI variables

* GingerSpec now performs variable replacement in the SQL files before executing the query in the database

* Multiples improvements in the rest steps. Method for getting the value of a json element given the jsonpath was modified to use the one that comes pre-bumdled in rest-assured. Several bug fixes and improvements in the log messages when theres an assertion error. Feature for rest steps was revamped with more examples and use cases

## 2.2.11

* mysql and postgre dependencies were updated and connection string used to bootstrap the connection no longer uses UTC timezone

* Added new lookup to StringSubstitutor in replacement aspect to evaluate expressions from the Java Faker library

* Fixed and error in which the formatter was unnecessarily calling the replacement aspect. Added a couple of steps for testing how the formatter prints docstring and datatables

* Added a new lookup to StringSubstitutor in replacementAspect to evaluate mathematical expressions using the exp4j library

* Bump log4j-api from 2.14.1 to 2.16.0

* Removed deprecated code from ReplacementAspect, changed its public API method to make it more coherent. Support for !{} #{} and @{} variable placeholders was completely removed from GingerSpec

* Added support make variable replacements in local files. Now, for example, you can use variables directly in the json body of a request that is stored in a local file. GingerSpec will try to perform variable replacement in the text before adding the body to the request. This eliminates the need to use a datatable for altering the body of the request

## 2.2.10

* Restored JaCoCoClient.java class to allow connections to a remote JaCoCo server

* Bump commons-beanutils from 1.9.3 to 1.9.4
  
* Upgrade com.google.guava:guava to version 30.0-jre
  
* Bump commons-configuration2 from 2.3 to 2.7

## 2.2.9

* Added debug messages for SQL steps

* Bump jackson-databind from 2.10.3 to 2.10.5.1 

* Bump jsoup from 1.7.3 to 1.14.2

* Bump commons-io from 2.6 to 2.7

* Bump httpclient from 4.5.1 to 4.5.13

* Optimized imports

* Added step to send messages to a slack channel

## 2.2.8

* Removed some unnecessary classes and methods

* Added new tag @slack that allows sending an slack notification when a scenario fails

* Added ALL and OFF as possible debug level when using tags

* Fixed a bug when creating folder structure when taking an screenshot during selenium tests

## 2.2.7

* Fixed a small bug with ifStatementAspect

## 2.2.6

* License changed from Apache 2 to ISC and header information updated in all relevant files

* @ignore tag functionality is now executed in a hook and not an aspect since this was causing the aspectj pointcut not to be triggered when running the test in Intellij IDE as a cucumber java test

* @runOnEnv and @skipOnEnv tag are now executed on a hook and not via aspects since aspectj pointcuts were not triggered when running the tests in Intellij IDEA as cucumber java tests

* Added a new aspect (ifStatementAspect) that allows the conditional execution of steps during runtime

* Optimized imports in all classes in the project

* Fixed a bug in which comments were not correctly processed by the TestNGPrettyFormatter

* Set a variable in a before hook to remove the warning coming from nashborn engine

* Removed unnecessary docs folder from root

* Small fix in logger name for CommonG class that was causing the @debug tag not to work correctly in all cases

## 2.2.5

* Small update in JiraConnector. Search of the correct transition id by name is now case insensitive.

* Comment posted to jira now only contains path from content root and not full path.

* Added a hook to change logging level at runtime (@debug, @trace, @info, etc). This is for debugging within Intellij IDEA.

* Added information in README file about the Jira tag hook.

## 2.2.4

* Jira tags can now use brackets instead of parenthesis (i.e @jira[QMS-123]). This is because it can be problematic to run scenarios based on tags if the tags contains parenthesis (it is a reserved character for cucumber tags)

* For Jira tags, the scenarios are now skipped at hook level using SkipException from TestNG. This is to improve the integration of GingerSpec with intellij IDEA (aspects are not picked up when running tests as cucumber java in Intellij IDEA)

## 2.2.3

* Added a new tag, @jira(QMS-123), that will allow users to skip the execution of scenarios based on the status of the referenced entities in Jira. this tag could also change the status of the entity in Jira based on the result of the scenario execution

* Minor fixes in README file

* Minor fix in footer message shown during tests executions

* Removed references to old variables placeholders (!{}, @{} and #{})

* Updated javadoc documentation for some rest steps

## 2.2.2

* Fixed problem that was causing the wrong body to be sent when using DocStrings in REST steps

* Fixed a small problem that was causing DocStrings to be incorrectly printed in the formatter

* Fixed logging messages in some classes

* Confluence repository in POM now uses HTTPS to avoid maven from blocking when resolving the dependency for avro serializer

## 2.2.1

* Kafka tests were removed from the pipeline. They were failing intermittently without a clear reason (most likely, lack of memory int he github action runner nodes)

* Downgraded to a more stable version of selenium grid and chrome and removed image for firefox since it was not really being used in the pipeline

## 2.2.1-RC2

* The functionality for executing the same selenium feature in all connected nodes was removed. It will no longer be maintained. This because it breaks with the newer versions of Selenium grid
and because it forces the user to create separate runner classes for Selenium tests. This provides a cleaner implementation that is much easier to test and debug.

* When using @web and a selenium grid, now the the full URL for the selenium grid will have to be provided (i.e -DSELENIUM_GRID=http://localhost:4444/wd/hub)

* Updated the Selenium grid and node images that are used for running the integration tests in the pipeline

* @mobile annotation will use http://127.0.0.1:4723 as default appium server URL if -DSELENIUM_GRID argument not found

* The functionality for creating the driver using @web and @mobile tags was completely rewritten. For @web the user can now use at least 3 VM variables for browser selection:
browserName, platform and version (that matches the manes of real capabilities). When executing the test against a Selenium Grid or when using Appium, the user can also provide
a json file with capabilities to be used (using -DCAPABILITIES=/path/to/capabilities.json)

* The steps to clear previous headers/cookies and url parameters were removed. Now, a new rest client is initialized as soon as a request completes its execution.

* Fixed a long standing issue with logging. Now, using -DlogLevel=DEBUG will print all debug messages from the steps, and, in the case of rest steps, all request/response information
can be printed for easier debugging

* Variables -DSELENIUM_NODE, -DSELENIUM_NODE_TYPE and -Dbrowser were removed

* Minor fixes in the Github actions workflows, removed unnecessary files related to TravisCI

## 2.2.1-RC1

* The behavior of the @web and @mobile tags was redesigned. There is no longer a need to use a special constructor in the runner class
and scenarios can be executed directly as cucumber scenarios. Variables -DSELENIUM_NODE, -DSELENIUM_NODE_TYPE, -Dbrowser were removed, and now just using -DSELENIUM_GRID is enough

* Capabilities for browser selection are now passed via CLI (-DbrowserName, -Dplatform, -Dversion)

* Opera, Internet explorer, Edge and Safari 

* Improvements in javadoc for sql, rest and selenium steps

* Added two new steps to wait for elements

* Added a new step to pass the body of the request as a DocString.

* Added new functionality to specify a proxy for rest requests

* Added step to type large pieces of text into a web element

* Function to calculate connected nodes to a grid was rewritten and is now more decoupled and unit tested (the functionality
for running the same selenium test in all connected nodes could be removed in future versions)

* When using a selenium grid, if capabilities are passed as VM arguments, these will be used as filters.

## 2.2.0

* Fixed small bug that was preventing the screen capture to be taken when running the tests via Intellij IDEA.

* Fixed small bug that was preventing to correctly replace a variable when the value was null.

* Minor improvements in some debug messages.

## 2.2.0-RC4

* Fixed Selenium step for getting value of given attribute. It was being restricted to only use selenium locators attributes.

* Fixed problem when warning for old variable placeholders was not being printed correctly

## 2.2.0-RC3

* The functionality for using variables in the gherkin file has been rewritten and now it uses StringSubstitutor (from Apache Commons) to do the variable replacement. This library is much more potent with a lot more functionality. From now own, the only valid variable placeholder will be ${}. Placeholders such as !{}, #{} and @{} are deprecated and will be removed in future releases. Check the wiki about "Gherkin variables" for more information about this. 

* (Partially) addressed the problem of variable replacement on final reports. The system now will try to perform as many variable replacements as possible right when the feature file is read: Feature title, Feature description, Rule, Scenario title, Scenario outline title, or Background title. Variables in regular steps are not replaced at this point since steps may contain variables that do not yet exis

* Added more links to helpMessage footer

* Added 2 new steps: one for creating random int within range an another one to create random strings (numeric or alphanumeric) of a given length

## 2.2.0-RC2

* Uncommented method to perform variables replacement in the commonG class.

* Fixed after web hook for adding images to failing steps in reports.

* Fixed minor javadoc error and simplified documentation for background tag.

* Improvements in the documentation of some features. Code cleanup.

* Minor fix in banner.

## 2.2.0-RC1

* Cucumber and TestNG dependencies updated to the latest version.

* IgnoreTagAspect, RunOnEvnAspect, LoopTagAspect, BackgroundAspect, and ReplacementAspect were rewritten to comply with the API changes.

* LogTagAspect was removed (functionality is now only in the formatter). Its reference was also removed from aop.xml file.

* TestNGPrettyFormatter was completely rewritten. Steps are now printed after the execution and the correct color is now applied, along with a new banner and information message at the beginning of a test suite.

* Some unnecessary feature files were removed or merged. Code cleanup.

* Minor fixes links in README file.

* Fixed problem with repo name in travis.yml file.

* Some steps in Rest were modified since the regex was complicated to maintain.

## 2.1.5

* Minor fix in SqlUtils class. Database cells with null values were causing an exception when transforming the resulset to List

* Improved javadoc for SQL database steps

## 2.1.4

* Overall improvements in the Javadocs for the Selenium and Rest steps. Improved documentation.

* Updated jweaver dependency. Client projects must update this in their POMS to correctly inject the javaagent ar runtime.

* Added tagName, LinkText y partialLinkText to the list of supported locator for selenium. Added 3 more steps for selenium.

* Added changed in logger to allow the execution of feature files directly from intellij IDEA.

* Selenium features can now be executed via runners without contructor, and also through intellij IDEA directly.

* Improvements in the regex of some steps. Added selenium steps for right click, double click, scroll until element visible and type directly by locator.

* ReplacementAspect was changed to final so its functions can be used directly from te class CommonG to get variables at runtime.

* Improvements in the regex for many steps to get better suggestions when using Intellij

* Added function to perform hovering on a web element.

* Added step to temporally stops the execution of a feature.

## 2.1.3

* Junit dependency was removed and now assertj is the preferred assertion library

* Small version bump in other dependencies

* Added 2 new steps in SeleniumGSpec

* Fixed error when printing the assertion error of a step

* Improvements in javadoc for SleniumGSpec and RestSpec

## 2.1.2

* Fix a minor problem when executing several cucumber runners with selenium features using cucumber tags. Now, they can be executed with 'mvn verify' (to execute everything) or for example 'mvn verify -Dcucumber.options="--tags @web"'

## 2.1.2-RC3

* Minor improvements on assertion messages.  Improved some assertion functions for better readability when executing the tests.
 
* The screenshot functionality is now performed by the standard function of selenium, this produces a capture of only the current view instead of the whole page. Ashot was causing a weird-looking captures on pages with infinite scrolling.

## 2.1.2-RC2

* Capturing a screenshot after a selenium scenario fails is now performed in a cucumber hook. So, now there's no need for the SeleniumAspect or for the SeleniumAssert. The snapshot is now also embedded in the selenium step, so it can be directly shown in the HTML report :)

* Modified formater to print the cause of the error in red color after each step. This removes the need for the AssertJAspect and makes errors much easier to spot

* Upgraded Ashot dependency to get rid of warning messages during runtime

* Miscellaneous optimizations and improvements in some assertion messages

## 2.1.2-RC1

* Changed the way Gingerspec modifies the current cucumber options of a runner class. This change is located in BaseGTest class and it modifies the CucumberOptions annotation of the class before the TestNGCucumberRunner object is created. This new implementation is much cleaner and more decoupled from cucumber.

* Removed useless BaseTest class

* Removed CLI command in the POM that allocated more RAM memory on runtime

* Added a step for better testing @ignore, @skip and @background annotations. The step must fail if executed, making easy to spot a problem in the annotations

* Small bump in cucumber version

## 2.1.1

* Updated dependencies for rest-assured and jacksonxml. This fixed an error when when doing schema validation for json responses

* Updated dependency for webdrivermanager and fixed a small bug when passing arguments to selenium driver in local mode for firefox

* Added the possibility of passing arguments directly to ChromeOptions/FirefoxOptions using the maven variable -DSELENIUM_ARGUMENTS=argument1;argument2;.....;argumentn (i.e -DSELENIUM_ARGUMENTS=--headless)

## 2.1.0

* Improved assertion error messages for selenium features. Messages are now more descriptive and the errors easier to spot.

* Improved javadoc for SeleniumGSpec class.

* Cleanup of README file for better readability. Removed information that is now located in the project wiki.

## 2.1.0-RC3

*  Fixed bug that was causing that during assertion errors, no screen capture was taken when driver was instance of mobiledriver (appium)

* Added a more descriptive error message to evaluateJSONElementOperation method

## 2.1.0-RC2

* Changed the hasAtLeast method from assertGreaterThan to assertGreaterThanOrEqual. Under certain conditions, no screen capture was taken when an exception occurred 

## 2.1.0-RC1

* A new step for executing Javascript functions (using JavaScriptExecutor) was included in the Selenium step definitions

* Bump jackson-databind from 2.9.9.2 to 2.9.10.1

* Gingerspec now supports Appium for testing mobile web/native apps. Many sections needed to be rewritten for this functionality.

## 2.0.4

* Improved documentation on some of the feature files

* Added new functionality to some of the selenium steps

* Added support for using the local browser when running selenium tests. If neither SELENIUM_GRID nor SELENIUM_NODE variables are found, gingerspec will try to download the appropiate driver for the selected browser and operating system (chrome is selected by default if no -Dbrowser variable found)

## 2.0.3

* GivenGSpec, WhenGSpec, ThenGSpec classes were removed and steps definitions were organized in a more logical way (This change may have some breaking changes to clients that still rely on the aforementioned classes)

* Step definition location is now hidden by default when running tests. The variable -DSHOW_STACK_INFO can be used to reveal it, together with information about the value of each parameter passed to the underlying test definition function

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
