Feature: Testing the Jira Integration
  
  Using the @jira() you can control the execution of scenarios based on its status 
  in Jira as well as update an entity status in Jira based on the result of
  the scenario execution. You can fully configure the behavior of this tag using the
  configuration file located at src/test/resources/jira.properties

  @jira(QMS-990)
  Scenario: This scenario should run only if ticket QMS-990 is Done or Deployed status
    Given I wait '1' seconds