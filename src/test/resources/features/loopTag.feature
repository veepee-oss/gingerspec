Feature: Feature used in testing loop tag aspect

  This feature provides examples on how to use the @loop tag. Using this tag before a scenario will
  convert this scenario into a scenario outline, changing parameter defined "NAME" for every element
  in the environment variable list received.

  Scenario: wipe test file.
    Given I run 'rm -f testOutput.txt' locally

  @loop(AGENT_LIST,VAR_NAME)
  Scenario: write <VAR_NAME> a file the final result of the scenario.
    Given I run 'echo <VAR_NAME> >> testOutput.txt' locally

  Scenario: verify file content.
    Given I run 'wc -l testOutput.txt' locally
    Then the command output contains '2'

  @loop(AGENT_LIST,VAR_NAME)
  Scenario: With scenarios outlines and datatables
    Given I create file 'testSOATtag<VAR_NAME.id>.json' based on 'schemas/simple<VAR_NAME>.json' as 'json' with:
      | $.a | REPLACE | @{JSON.schemas/empty.json}     | object   |
    Given I save '@{JSON.testSOATtag<VAR_NAME.id>.json}' in variable 'VAR'
    Then I run '[ "!{VAR}" = "{"a":{}}" ]' locally

  @skipOnEnv(AGENT_LIST)
  Scenario: This scenario should be omitted.
    Given I run '[ "!{VAR_NOT_DEFINED}" = "{"a":{}}" ]' locally
    Given I run 'exit 1' locally

  @runOnEnv(AGENT_LIST)
  Scenario: This scenario should be executed.
    Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally

  @runOnEnv(AGENT_LIST)
  @loop(AGENT_LIST,VAR_NAME)
  Scenario: With scenarios outlines and datatables
    Given I create file 'testSOATtag<VAR_NAME.id>B.json' based on 'schemas/simple<VAR_NAME>.json' as 'json' with:
      | $.a | REPLACE | @{JSON.schemas/empty.json}     | object   |
    Given I save '@{JSON.testSOATtag<VAR_NAME.id>B.json}' in variable 'VAR'
    Then I run '[ "!{VAR}" = "{"a":{}}" ]' locally

  @runOnEnv(NO_VAR)
  @loop(NO_VAR,VAR_NAME)
  Scenario: With scenarios outlines and datatables
    Given I create file 'testSOATtag<VAR_NAME.id>B.json' based on 'schemas/simple<VAR_NAME>.json' as 'json' with:
      | $.a | REPLACE | @{JSON.schemas/empty.json}     | object   |
    Given I save '@{JSON.testSOATtag<VAR_NAME.id>B.json}' in variable 'VAR'
    Then I run '[ "!{VAR}" = "{"a":{}}" ]' locally

