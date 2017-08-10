Feature: Zookeeper steps test

  Scenario: Connection to Zk
    Given I connect to Zookeeper at '${ZOOKEEPER_HOSTS}'

  Scenario: zookeeper node does exist
    Then the zNode '/zookeeper' exists

  Scenario: create zookeeper node and check existance
    Then I create the zNode '/testephemeral' which IS ephemeral
    Then the zNode '/testephemeral' exists
    Then I remove the zNode '/testephemeral'

  Scenario: create zookeeper node and check existance
    Then I create the zNode '/testephemeraldata' with content 'midata' which IS ephemeral
    Then the zNode '/testephemeraldata' exists and contains 'midata'
    Then I remove the zNode '/testephemeraldata'

  Scenario: check no longer exist zookeeper nodes
    Then the zNode '/testephemeral' does not exist
    Then the zNode '/testephemeraldata' does not exist

  Scenario: create zookeeper non ephemeral node and check existance
    Then I create the zNode '/testnonephemeral' which IS NOT ephemeral
    Then the zNode '/testnonephemeral' exists

  Scenario: create zookeeper node and check existance
    Then I create the zNode '/testnonephemeraldata' with content 'midata' which IS ephemeral
    Then the zNode '/testnonephemeraldata' exists and contains 'midata'
    Then I disconnect from Zookeeper
