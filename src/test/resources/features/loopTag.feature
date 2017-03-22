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
#    Given I run 'echo <VAR_NAME> >> ignore.txt' locally
#    When I run 'wc -l ignore.txt' locally
#    Then the command output contains '<VAR_NAME.id>'
