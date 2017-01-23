@rest
Feature: Datatable replacements

  Scenario: Data table replacement enviroment(passed in variable)
    Given I open remote ssh connection to host '${SSH}' with user 'root' and password 'stratio'
    And My app is running in 'jenkins.stratio.com:80'
    And I send a 'POST' request to '/FNF' based on 'schemas/rest.json' as 'json' with:
      | $.type  | UPDATE  | ${SLEEPTEST}  |
    Then the service response status must be '404'.

  Scenario: Data table replacement enviroment(save in scenario)
    Given I open remote ssh connection to host '${SSH}' with user 'root' and password 'stratio'
    And My app is running in 'jenkins.stratio.com:80'
    When I execute command 'echo datatable' in remote ssh connection and save the value in environment variable 'ELEM'
    And I send a 'POST' request to '/FNF' based on 'schemas/rest.json' as 'json' with:
      | $.type  | UPDATE  | !{ELEM}  |
    Then the service response status must be '404'.
