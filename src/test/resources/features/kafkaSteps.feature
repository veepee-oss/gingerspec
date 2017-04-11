@rest
Feature: Cassandra steps test

  Scenario: Trying connection to Zk
    Given I connect to Zookeeper at '${ZOOKEEPER_HOSTS}'
    Then I disconnect from Zookeeper

  Scenario: Connect to kafka
    Given I connect to kafka at '${ZOOKEEPER_HOSTS}' using path 'brokers/topics'

  Scenario: A kafka topic does exist
    Then A kafka topic named 'testqa' exists

  Scenario: Send message to kafka topic
    Given I send a message 'hello' to the kafka topic named 'testqa'

  Scenario: Increase partitions in kafka topic
    Given I increase '1' partitions in a Kafka topic named 'testqa'

  Scenario: A kafka topic does not exist
    Given A kafka topic named 'testWrong' not exists
