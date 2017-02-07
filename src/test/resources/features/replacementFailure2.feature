@manual
Feature: Non-existant vars

  Scenario: In a step as local var
    Given I wait '!{DUNNO}' seconds

