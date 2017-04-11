Feature: Simple run test

  Background: Connect to bootstrap machine
    Given I open a ssh connection to '${SSH}' with user 'root' and password 'stratio'

  Scenario: Default exit status - Successful (0)
    Then I run 'ls /tmp' in the ssh connection

  Scenario: Custom exit status - Wrong (not 0)
    Then I run 'lss /tmp' in the ssh connection with exit status '127'

  Scenario: Default exit status - Store/retrieve in/from environment variable
    When I run 'ls -la /tmp' in the ssh connection and save the value in environment variable 'DEFEXSTAT'
    Then '!{DEFEXSTAT}' contains 'total'

  Scenario: Run command locally
    Given I run 'ls /tmp | wc -l' locally with exit status '0' and save the value in environment variable 'wordcount'

  Scenario: Custom exit status - Store/retrieve in/from environment variable
    When I run 'lss /tmp' in the ssh connection with exit status '127' and save the value in environment variable 'CUSEXSTAT'
    Then '!{CUSEXSTAT}' contains 'lss'

  Scenario: Outbound Inbound file
    When I outbound copy 'exampleJSON.conf' through a ssh connection to '/tmp/exampleJSON.conf'
    Then I inbound copy '/tmp/exampleJSON.conf' through a ssh connection to 'fileFromSsh.conf'

  Scenario: Default exit status - Check output
    When I run 'ls -la /tmp' in the ssh connection
    Then the command output contains 'total'
