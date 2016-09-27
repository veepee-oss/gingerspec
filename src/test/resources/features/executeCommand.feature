Feature: Simple execute command test

  Background: Connect to bootstrap machine
    Given I open remote ssh connection to host '${SSH}' with user 'root' and password 'stratio'

  Scenario: Default exit status - Successful (0)
    Then I execute command 'ls /tmp' in remote ssh connection

  Scenario: Custom exit status - Wrong (not 0)
    Then I execute command 'lss /tmp' in remote ssh connection with exit status '127'

  Scenario: Default exit status - Store/retrieve in/from environment variable
    When I execute command 'ls -la /tmp' in remote ssh connection and save the value in environment variable 'DEFEXSTAT'
    Then '!{DEFEXSTAT}' contains 'total'

  Scenario: Custom exit status - Store/retrieve in/from environment variable
    When I execute command 'lss /tmp' in remote ssh connection with exit status '127' and save the value in environment variable 'CUSEXSTAT'
    Then '!{CUSEXSTAT}' contains 'lss'

  Scenario: Default exit status - Check output
    When I execute command 'ls -la /tmp' in remote ssh connection
    Then the command output contains 'total'
