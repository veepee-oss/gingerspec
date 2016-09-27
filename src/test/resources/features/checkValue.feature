Feature: Simple check values test

  Background: Connect to bootstrap machine
    Given I open remote ssh connection to host '${SSH}' with user 'root' and password 'stratio'

  Scenario: is
    When I execute command 'echo "EqualTo"' in remote ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' is 'EqualTo'
    And I execute command 'echo "Equal To"' in remote ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' is 'Equal To'
    And I execute command 'echo "15"' in remote ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' is '15'

  Scenario: matches
    When I execute command 'echo "MatcheS"' in remote ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' matches '^[A-Za-z]+'
    And I execute command 'echo "123"' in remote ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' matches '^\d+'
    And I execute command 'echo "-123"' in remote ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' matches '^-?\d+'
    And I execute command 'echo "Mat1ch2eS3"' in remote ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' matches '^[A-Za-z\d]+'
    And I execute command 'echo "Mat 1ch2 eS3"' in remote ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' matches '^[A-Za-z\d\s]+'

  Scenario: is higher than
    When I execute command 'echo "25"' in remote ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' is higher than '10'

  Scenario: is lower than
    When I execute command 'echo "10"' in remote ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' is lower than '25'

  Scenario: contains
    When I execute command 'echo "Contains"' in remote ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' contains 'Con'
    And '!{EVAR}' contains 'nta'
    And '!{EVAR}' contains 'ains'
    Then I execute command 'echo "Con ta ins"' in remote ssh connection and save the value in environment variable 'EVAR'
    And '!{EVAR}' contains 'a in'

  Scenario: is different from
    When I execute command 'echo "Different"' in remote ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' is different from 'Not Different'
    And I execute command 'echo "15"' in remote ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' is different from '25'
