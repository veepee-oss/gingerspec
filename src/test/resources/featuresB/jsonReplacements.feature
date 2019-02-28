Feature: JSON replacements

  Scenario: Simplest read
    #log prueba @{JSON.schemas/empty.json}
    Given I save '@{JSON.schemas/empty.json}' in variable 'VAR'
    #log prueba 2 @{JSON.schemas/empty.json}
    Then I run '[ "!{VARASCD}" = "{}" ]' locally

  Scenario: Simplest read with failure message
    Given I save '@{JSON.schemas/otherempty.json}' in variable 'VAR'
    Then I run '[ "!{VAR}" = "ERR! File not found: schemas/otherempty.json" ]' locally

  Scenario: Simplest read @{JSON.schemas/empty.json} on scenario name
    Given I run 'ls' locally

  Scenario: Simplest read ${WAIT} on scenario name 2
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