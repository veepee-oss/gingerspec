Feature: Read file and store modified content in environment variable test

  Scenario: Read JSON file
    Given I read file 'schemas/testCreateFile.json' as 'json' and save it in environment variable 'myjson' with:
      | $.key1 | UPDATE | new_value     | n/a   |
      | $.key2 | ADDTO  | ["new_value"] | array |
    When I run 'echo "!{myjson}"' locally
    Then the command output contains '{key1:new_value,key2:[[new_value]],key3:{key3_2:value3_2,key3_1:value3_1}}'

  Scenario: Read string file
    Given I read file 'schemas/krb5.conf' as 'string' and save it in environment variable 'mystring' with:
      | foo | REPLACE |  bar    | n/a   |
    When I run 'echo "!{mystring}"' locally
    Then the command output contains 'bar = bar'