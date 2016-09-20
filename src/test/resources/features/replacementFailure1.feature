  Feature: Non-existant vars

    Scenario: In a step as env var
      Given I wait '${DUNNO}' seconds

