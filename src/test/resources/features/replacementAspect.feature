Feature: Testing variable replacements ${VERSION}

  This features provides examples on how to use variable replacement. You can use variables at any point in your
  feature file: feature title, description, scenario description steps, datatables, docstrings and even comments.

  Scenario: Replacements in comments
    #log the value of time.wait is #{wait.time}
    And I wait '#{wait.time}' seconds

  Scenario: Replacements scenario title (${VERSION})
    #log the value of time.wait is #{wait.time}
    And I wait '#{wait.time}' seconds

  Scenario: Operations with global variables
    Then '${VARNAME.toUpper}' matches 'FOO'
    Then '${VARNAME.toLower}' matches 'foo'
    Then '${INVALID:-bb}' matches 'bb'

  Scenario: Saving and replacing local variables
    Given I save '2' in variable 'SO_ENV_VAR'
    And I wait '!{SO_ENV_VAR}' seconds

  Scenario Outline: Replacements in an scenario outline
    Given I save '2' in variable 'SO_ENV_VAR'
    And I wait '!{SO_ENV_VAR}' seconds
    And I wait '<other>' seconds
    And I wait '#{wait.time}' seconds

    Examples:
      | other        |
      | 1            |
      | #{wait.time} |

  Scenario: Reading a file and saving it in a variable
    Given I save '@{JSON.schemas/empty.json}' in variable 'VAR'
    Then I run '[ "!{VAR}" = "{}" ]' locally

  Scenario: Reading a file with failure message
    Given I save '@{JSON.schemas/otherempty.json}' in variable 'VAR'
    Then I run '[ "!{VAR}" = "ERR! File not found: schemas/otherempty.json" ]' locally

  Scenario: Simplest read @{JSON.schemas/empty.json} on scenario name
    Given I run 'ls' locally

  Scenario Outline: With scenarios outlines
    Given I save '@{JSON.schemas/simple<id>.json}' in variable 'VAR'
    Then I run '[ "!{VAR}" = "<content>" ]' locally

    Examples:
      | id  | content    |
      | 0   | {}         |
      | 1   | {"a":true} |

  Scenario Outline: With scenarios outlines in examples table
    Given I save '<file>' in variable 'VAR'
    Then I run '[ "!{VAR}" = "<content>" ]' locally

    Examples:
      | content    | file                         |
      | {}         | @{JSON.schemas/simple0.json} |
      | {"a":true} | @{JSON.schemas/simple1.json} |

  Scenario Outline: With scenarios outlines and datatables
    Given I create file 'testSOATtag.json' based on 'schemas/simple1.json' as 'json' with:
      | $.a | REPLACE | <file>     | object   |
    Given I save '@{JSON.testSOATtag.json}' in variable 'VAR'
    Then I run '[ "!{VAR}" = "<content>" ]' locally

    Examples:
      | content  | file                       |
      | {"a":{}} | @{JSON.schemas/empty.json} |

  Scenario Outline: With scenarios outlines and datatables2
    Given I run 'ls' locally
    Given I create file 'testSOATtag.json' based on 'schemas/simple1.json' as 'json' with:
      | $.a         | REPLACE | @{JSON.schemas/<file>.json}     | object   |
      | b           | ADD     | ${WAIT}                         | N/A      |
      | ${WAIT}     | ADD     | @{JSON.schemas/<file>.json}     | object   |
    Given I save '@{JSON.testSOATtag.json}' in variable 'VAR'
    Then I run '[ "!{VAR}" = "<content>" ]' locally

    Examples:
      | content                 | file    |
      | {"a":{},"1":{},"b":"1"} | empty   |
      | {"a":{},"1":{},"b":"1"} | simple0 |