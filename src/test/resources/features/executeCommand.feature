@ignore @toocomplex
Feature: Example on how to execute commands in the system shell

  This feature provides examples for running commands in the system shell. Commands can be run locally (in the host system)
  or you can even execute shell commands in a remote server via a SSH connection.

  Background: Connect to a remote SSH server
    Given I open a ssh connection to '${SSH}' with user 'root' and password 'temporal'

  Scenario: Default exit status - Successful (0)
    Then I run 'ls /tmp' in the ssh connection

  Scenario: Custom exit status - Wrong (not 0)
    Then I run 'lss /tmp' in the ssh connection with exit status '127'

  Scenario: Default exit status - Check output
    When I run 'ls -la /tmp' in the ssh connection
    Then the command output contains 'total'

  Scenario: Default exit status - Store/retrieve in/from environment variable
    When I run 'ls -la /tmp' in the ssh connection and save the value in environment variable 'DEFEXSTAT'
    Then '${DEFEXSTAT}' contains 'total'

  Scenario: Run command locally
    Given I run 'ls /tmp | wc -l' locally with exit status '0' and save the value in environment variable 'wordcount'

  Scenario: Custom exit status - Store/retrieve in/from environment variable
    When I run 'lss /tmp' in the ssh connection with exit status '127' and save the value in environment variable 'CUSEXSTAT'
    Then '${CUSEXSTAT}' contains 'lss'

  Scenario: Upload/Download files to/from the remote server through the SSH connection
    When I outbound copy 'exampleJSON.conf' through a ssh connection to '/tmp/exampleJSON.conf'
    Then I inbound copy '/tmp/exampleJSON.conf' through a ssh connection to 'fileFromSsh.conf'
