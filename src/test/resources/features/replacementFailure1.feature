@manual
Feature: Non-existant vars

  Scenario: Correct environment variable replacement
    Given I wait '${WAIT}' seconds

  Scenario Outline: In a step as env var
    Given I wait '<example>' seconds
  Examples:
    | example   |
    | ${DUNNO}  |