Feature: Mongo steps test

  Scenario: Connect to Mongo
    Given I connect to 'Mongo' cluster at '${MONGO_HOST}'

  Scenario: Create database in Mongo
    Given I create a MongoDB dataBase 'test'
    Then a Mongo dataBase 'test' doesnt contains a table 'hola'

  Scenario: Drop database in Mongo
    Given I drop a MongoDB database 'test'

  Scenario: Insert row in Mongo table
    Given I insert into a MongoDB database 'local' and table 'startup_log' this values:
  | _id-Object  | "hola"  |
    And a Mongo dataBase 'local' contains a table 'startup_log' with values:
  | hostname  | mongo  |

  Scenario: Insert row in Mongo table
    And I execute a query 'db.local.find()' of type 'string' in mongo 'local' database using collection 'startup_log' with:
    | hello |
  Scenario: Drop document in Mongo
    Given I drop every document at a MongoDB database 'local' and table 'System'