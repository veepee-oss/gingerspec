Feature: Is it Friday yet?
  Everybody wants to know when it's Friday

  Scenario: Sunday isn't Friday ${variable}
    Given today is Sunday
    When I ask whether it's Friday yet
    And This is a step with variable '${variable}'

  Scenario: Sunday isn't Friday ${variable}
    Given today is Sunday
    When I ask whether it's Friday yet
    And This is a step with variable '${variable}'
