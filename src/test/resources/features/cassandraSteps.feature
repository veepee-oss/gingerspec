Feature: Cassandra steps test

  Scenario: Connect to Cassandra
    Given I connect to 'Cassandra' cluster at '${CASSANDRA_HOST}'

  Scenario: Create a keyspace in Cassandra
    Given I create a Cassandra keyspace named 'opera'
    Then a Cassandra keyspace 'opera' exists

  Scenario: Create a table in Cassandra
    And I create a Cassandra table named 'analyzertable' using keyspace 'opera' with:
      |name  | comment |lucene |
      | TEXT |TEXT     |TEXT   |
      |  PK  |         |       |

    And I insert in keyspace 'opera' and table 'analyzertable' with:
      |name 	    |comment				        |
      |'Kurt'      	|'Hello to a man'   			|
      |'Michael'    |'Hello to a woman'			    |
      |'Louis'     	|'Bye to a man' 				|
      |'John'     	|'Bye to a woman'  			    |
      |'James'     	|'Hello to a man and a woman'  	|

    Then a Cassandra keyspace 'opera' contains a table 'analyzertable'
    And a Cassandra keyspace 'opera' contains a table 'analyzertable' with '5' rows

  Scenario: Querying table in Cassandra
    When a Cassandra keyspace 'opera' contains a table 'analyzertable' with values:
      |  comment-varchar |

  Scenario: I remove all data
    Given I drop a Cassandra keyspace 'opera'

  Scenario: Exception in query
    When I create a Cassandra keyspace named 'opera'
    And I create a Cassandra table named 'location' using keyspace 'opera' with:
      | place  | latitude | longitude |lucene |
      | TEXT   | DECIMAL  |  DECIMAL  |TEXT   |
      |  PK    | PK       |           |       |
    And I insert in keyspace 'opera' and table 'location' with:
      |latitude|longitude|place       |
      |2.5     |2.6      |'Madrid'    |
      |12.5    |12.6     |'Barcelona' |

    Given I execute a query over fields '*' with schema 'schemas/geoDistance.conf' of type 'string' with magic_column 'lucene' from table: 'location' using keyspace: 'opera' with:
      | col          | UPDATE  | geo_point  |
      | __lat        | UPDATE  | 0          |
      | __lon        | UPDATE  | 0          |
      | __maxDist    | UPDATE  | 720km      |
      | __minDist    | UPDATE  | -100km     |
    Then an exception 'IS' thrown with class 'Exception' and message like 'InvalidQueryException'

  Scenario: Truncate table in Cassandra
    Given I truncate a Cassandra table named 'analyzertable' using keyspace 'opera'

  Scenario: Drop table in Cassandra
    Given I drop a Cassandra table named 'analyzertable' using keyspace 'opera'

  Scenario: Drop keyspace in Cassandra
    Given I drop a Cassandra keyspace 'opera'