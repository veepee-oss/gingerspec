  Feature: Further replacements

    Scenario Outline: With scenarios outlines #fails
      Given I wait '<ExSLeep>' seconds

      Examples:
      | ExSLeep      |
      | 3            |
      | ${SLEEPTEST} |
      | 3            |
