  Feature: Non-existant env var

    Scenario: In a step
      Given I wait '!{DUNNO}' seconds
