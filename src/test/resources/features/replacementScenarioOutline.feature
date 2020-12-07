Feature: Scenario outline replacements

  this is a comment

  Scenario: inner scenario outline replacements ${VAR}
    Given I save '2' in variable 'SO_ENV_VAR'
    And I wait '!{SO_ENV_VAR}' seconds

  Scenario Outline: inner scenario outline replacements ${VAR}
    Given I save '2' in variable 'SO_ENV_VAR'
    And I wait '!{SO_ENV_VAR}' seconds
    And I wait '<other>' seconds
    And I wait '#{wait.time}' seconds

    Examples:
      | other        |
      | 1            |
      | #{wait.time} |