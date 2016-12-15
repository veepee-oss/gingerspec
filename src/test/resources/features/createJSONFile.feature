Feature: Create file test

  Scenario: Create JSON file (simple)
    Given I create file 'testCreateFileSimple.json' based on 'schemas/testCreateFile.json' as 'json' with:
      | $.key1 | UPDATE | new_value     | n/a   |
      | $.key2 | ADDTO  | ["new_value"] | array |
    Then I execute command 'cat $(pwd)/testCreateFileSimple.json' locally

  Scenario: Create JSON file (object)
    Given I create file 'testCreateFileReplaceObject.json' based on 'schemas/testCreateFile.json' as 'json' with:
      | $.key3 | REPLACE | @{JSON.schemas/testCreateFileReplaceObject.json} | object |
    Then I execute command 'cat $(pwd)/testCreateFileReplaceObject.json' locally
    And I execute command 'if ! diff $(pwd)/testCreateFileReplaceObject.json $(pwd)/src/test/resources/schemas/testCreateFileResult.json -q ; then exit 1; fi' locally
