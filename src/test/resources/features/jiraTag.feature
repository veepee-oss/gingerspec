@ignore
Feature: Jira Integration

  Using the @jira[] you can control the execution of scenarios based on its status
  in Jira as well as update an entity status in Jira based on the result of
  the scenario execution. You can fully configure the behavior of this tag using the
  configuration file located at src/test/resources/jira.properties
  More information here: https://github.com/veepee-oss/gingerspec/wiki/Gherkin-tags#jira-tag

  Rule: Basic usage

    @jira[QMS-990] @rest
    Scenario: Execute scenario based on status of jira ticket
      Given I save 'a' in variable 'x'
      And I save 'b' in variable 'y'
      Then '${x}' is different from '${y}'