Feature: JSON replacements

  Scenario: Simplest read
    Given I save '@{JSON.schemas/empty.json}' in variable 'VAR'
    Then I run '[ "!{VAR}" = "{}" ]' locally

  Scenario: Simplest read with failure message
    Given I save '@{JSON.schemas/otherempty.json}' in variable 'VAR'
    Then I run '[ "!{VAR}" = "ERROR: File does not exist: schemas/otherempty.json" ]' locally

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