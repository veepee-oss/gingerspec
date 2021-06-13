Feature: Testing variable replacements ${VERSION}

  This feature provides an example on how to use variable replacement. You can use variables at any point in your
  feature file: feature title, description, scenario description steps, datatables, docstrings and even comments.

  Scenario: Replacements scenario title (${VERSION})
    #log the value of time.wait is #{wait.time}
    And I wait '#{wait.time}' seconds
    #log the value of time.wait is ${envProperties:wait.time}
    And I wait '${envProperties:wait.time}' seconds

  Scenario: Operations with global variables
    Then '${toUpperCase:${VARNAME}}' matches 'FOO'
    Then '${toLowerCase:${VARNAME}}' matches 'foo'
    Then '${INVALID:-bb}' matches 'bb'

  Scenario: Saving and replacing local variables
    Given I save '2' in variable 'SO_ENV_VAR'
    And I wait '${SO_ENV_VAR}' seconds
    And I wait '${SO_ENV_VAR}' seconds

  Scenario Outline: Replacements in an scenario outline
    Given I save '2' in variable 'SO_ENV_VAR'
    And I wait '${SO_ENV_VAR}' seconds
    And I wait '${SO_ENV_VAR}' seconds
    And I wait '<other>' seconds
    And I wait '#{wait.time}' seconds
    And I wait '${envProperties:wait.time}' seconds

    Examples:
      | other                      |
      | 1                          |
      | ${envProperties:wait.time} |

  Scenario: Reading a file and saving it in a variable
    Given I save '${file:UTF-8:src/test/resources/schemas/empty.json}' in variable 'VAR'
    Then I run '[ "${VAR}" = "{}" ]' locally
    #log The same operation can be done with $variables
    Given I save '${file:UTF-8:src/test/resources/schemas/empty.json}' in variable 'VAR'
    Then I run '[ "${VAR}" = "{}" ]' locally

  Scenario: Simplest read ${file:UTF-8:src/test/resources/schemas/empty.json} on scenario name
    Given I run 'ls' locally

  Scenario Outline: With scenarios outlines
    Given I save '${file:UTF-8:src/test/resources/schemas/simple<id>.json}' in variable 'VAR'
    Then I run '[ "${VAR}" = "<content>" ]' locally

    Examples:
      | id | content     |
      | 0  | {}          |
      | 1  | {"a": true} |

  Scenario Outline: With scenarios outlines in examples table
    Given I save '<file>' in variable 'VAR'
    Then I run '[ "${VAR}" = "<content>" ]' locally

    Examples:
      | content     | file                                                  |
      | {}          | ${file:UTF-8:src/test/resources/schemas/simple0.json} |
      | {"a": true} | ${file:UTF-8:src/test/resources/schemas/simple1.json} |

  Scenario Outline: With scenarios outlines and datatables
    Given I create file 'testSOATtag.json' based on 'schemas/simple1.json' as 'json' with:
      | $.a | REPLACE | <file> | object |
    Given I save '${file:UTF-8:target/test-classes/testSOATtag.json}' in variable 'VAR'
    Then I run '[ "${VAR}" = "<content>" ]' locally

    Examples:
      | content  | file                                                |
      | {"a":{}} | ${file:UTF-8:src/test/resources/schemas/empty.json} |

  Scenario Outline: With scenarios outlines and datatables2
    Given I run 'ls' locally
    Given I create file 'testSOATtag.json' based on 'schemas/simple1.json' as 'json' with:
      | $.a     | REPLACE | ${file:UTF-8:src/test/resources/schemas/<file>.json} | object |
      | b       | ADD     | ${WAIT}                                              | N/A    |
      | ${WAIT} | ADD     | ${file:UTF-8:src/test/resources/schemas/<file>.json} | object |
    Given I save '${file:UTF-8:target/test-classes/testSOATtag.json}' in variable 'VAR'
    Then I run '[ "${VAR}" = "<content>" ]' locally

    Examples:
      | content                 | file    |
      | {"a":{},"1":{},"b":"1"} | empty   |
      | {"a":{},"1":{},"b":"1"} | simple0 |

  Scenario: Create random numbers
    Given I generate a random 'numeric' string of length '20' and save it in the variable 'NUMERIC'
    #log ${NUMERIC}
    Given I generate a random 'alphanumeric' string of length '20' and save it in the variable 'ALPHANUMERIC'
    #log ${ALPHANUMERIC}
    Given I generate a random number between '0' and '10' and save it in the variable 'RANDOM'
    #log ${RANDOM}
    And I wait '1' seconds