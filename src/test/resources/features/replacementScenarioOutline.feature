Feature: Scenario outline replacements

  Scenario Outline: inner scenario outline replacements
    Given I save '2' in variable 'SO_ENV_VAR'
    And I wait '!{SO_ENV_VAR}' seconds
    And I wait '<other>' seconds
    And I wait '#{wait.time}' seconds

    Examples:
      | other |
      | 1     |
      | 2     |