Feature: Create file test

  Scenario: Create JSON file (simple)
    Given I create file 'testCreateFileSimple.json' based on 'schemas/testCreateFile.json' as 'json' with:
      | $.key1 | UPDATE | new_value     | n/a   |
      | $.key2 | ADDTO  | ["new_value"] | array |
    Then I run 'cat $(pwd)/target/test-classes/testCreateFileSimple.json' locally

  Scenario: Create JSON file (object)
    Given I create file 'testCreateFileObject.json' based on 'schemas/testCreateFile.json' as 'json' with:
      | $.key3 | REPLACE | @{JSON.schemas/testCreateFileReplaceObject.json} | object |
    Then I run 'cat $(pwd)/target/test-classes/testCreateFileObject.json' locally
    And I run 'if ! diff $(pwd)/target/test-classes/testCreateFileObject.json $(pwd)/target/test-classes/schemas/testCreateFileObjectResult.json -q ; then exit 1; fi' locally

  Scenario: Create JSON file (plain text file)
    Given I create file 'testCreateFilePlain.json' based on 'schemas/testCreateFile.json' as 'json' with:
      | $.key2 | ADDTO | @{FILE.schemas/testCreateFileReplacePlainText.json} | string |
    Then I run 'cat $(pwd)/target/test-classes/testCreateFilePlain.json' locally
    And I run 'if ! diff $(pwd)/target/test-classes/testCreateFilePlain.json $(pwd)/target/test-classes/schemas/testCreateFileReplacePlainTextResult.json -q ; then exit 1; fi' locally

  Scenario: Create JSON file replacing with previous file
    Given I create file 'testCreatePrevious.json' based on 'schemas/testCreatePreviousFile.json' as 'json' with:
      | $.key_previous_1 | UPDATE | value_previous_2 | string |
    Then I create file 'testCreatePreviousFinal.json' based on 'schemas/testCreateFile.json' as 'json' with:
      | $.key2 | ADDTO | @{JSON.testCreatePrevious.json} | object |
    Then I run 'cat $(pwd)/target/test-classes/testCreatePreviousFinal.json' locally
    And I run 'if ! diff $(pwd)/target/test-classes/testCreatePreviousFinal.json $(pwd)/target/test-classes/schemas/testCreatePreviousFileResult.json -q ; then exit 1; fi' locally
