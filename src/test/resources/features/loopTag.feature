Feature: Feature used in testing loop tag aspect

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

#  @loop(AGENT_LIST,VAR_NAME)
#  Scenario: This is an omitted scenario so it contains a failing assert
#    Given I run '[ "SHOULDNT_RUN" = "FAIL OTHERWISE" ]' locally

  @skipOnEnv(AGENT_LIST)
  Scenario: This scenario should be omitted.
    Given I run '[ "SHOULDNT_RUN" = "FAIL OTHERWISE" ]' locally

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