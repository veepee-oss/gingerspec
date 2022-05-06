#@ignore
Feature: Testing the Jira Integration
  
  Using the @jira[] you can control the execution of scenarios based on its status
  in Jira as well as update an entity status in Jira based on the result of
  the scenario execution. You can fully configure the behavior of this tag using the
  configuration file located at src/test/resources/jira.properties
  More information here: https://github.com/veepee-oss/gingerspec/wiki/Gherkin-tags#jira-tag

  @jira[QMS-990] @rest
  Scenario: A new element is inserted via a POST call
    Given I send requests to '${REST_SERVER_HOST}:3000'
    When I send a 'POST' request to '/posts' based on 'schemas/mytestdata.json' as 'json'
    Then the service response status must be '201'
    And I save element '$.title' in environment variable 'TITLE'
    Then '${TITLE}' matches 'This is a test'