Feature: Simple check values test

  Background: Connect to bootstrap machine
    Given I open a ssh connection to '${SSH}' with user 'root' and password 'stratio'

  Scenario: is
    When I run 'echo "EqualTo"' in the ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' is 'EqualTo'
    And I run 'echo "Equal To"' in the ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' is 'Equal To'
    And I run 'echo "15"' in the ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' is '15'

  Scenario: matches
    When I run 'echo "MatcheS"' in the ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' matches '^[A-Za-z]+'
    And I run 'echo "123"' in the ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' matches '^\d+'
    And I run 'echo "-123"' in the ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' matches '^-?\d+'
    And I run 'echo "Mat1ch2eS3"' in the ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' matches '^[A-Za-z\d]+'
    And I run 'echo "Mat 1ch2 eS3"' in the ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' matches '^[A-Za-z\d\s]+'

  Scenario: is higher than
    When I run 'echo "25"' in the ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' is higher than '10'

  Scenario: is lower than
    When I run 'echo "10"' in the ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' is lower than '25'

  Scenario: contains
    When I run 'echo "Contains"' in the ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' contains 'Con'
    And '!{EVAR}' contains 'nta'
    And '!{EVAR}' contains 'ains'
    Then I run 'echo "Con ta ins"' in the ssh connection and save the value in environment variable 'EVAR'
    And '!{EVAR}' contains 'a in'

  Scenario: is different from
    When I run 'echo "Different"' in the ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' is different from 'Not Different'
    And I run 'echo "15"' in the ssh connection and save the value in environment variable 'EVAR'
    Then '!{EVAR}' is different from '25'
