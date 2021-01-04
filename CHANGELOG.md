# Changelog

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
