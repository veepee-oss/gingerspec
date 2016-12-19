Feature: Create file test

  Scenario: Create JSON file (simple)
    Given I create file 'testCreateFileSimple.json' based on 'schemas/testCreateFile.json' as 'json' with:
      | $.key1 | UPDATE | new_value     | n/a   |
      | $.key2 | ADDTO  | ["new_value"] | array |
    Then I execute command 'cat $(pwd)/src/test/resources/testCreateFileSimple.json' locally

  Scenario: Create JSON file (object)
    Given I create file 'testCreateFileObject.json' based on 'schemas/testCreateFile.json' as 'json' with:
      | $.key3 | REPLACE | @{JSON.schemas/testCreateFileReplaceObject.json} | object |
    Then I execute command 'cat $(pwd)/src/test/resources/testCreateFileObject.json' locally
    And I execute command 'if ! diff $(pwd)/src/test/resources/testCreateFileObject.json $(pwd)/src/test/resources/schemas/testCreateFileResultObject.json -q ; then exit 1; fi' locally
