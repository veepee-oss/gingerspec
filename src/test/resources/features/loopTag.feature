Feature: Feature used in testing loop tag aspect

  Scenario: wipe test file.
    Given I run 'rm -f testOutput.txt' locally

  @loop(AGENT_LIST,VAR_NAME)
  Scenario: write <VAR_NAME> a file the final result of the scenario.
    Given I run 'echo <VAR_NAME> >> testOutput.txt' locally

  Scenario: verify file content.
    Given I run 'wc -l testOutput.txt' locally
    Then the command output contains '3'