Feature: Kafka steps test

#  Scenario: Connect to kafka
#    Given I connect to kafka at 'localhost:2181'
#
#  Scenario: Send message to kafka topic
#    Given I connect to kafka at 'localhost:2181'
#    Given I create a Kafka topic named 'testqa' if it doesn't exists
#    Then A kafka topic named 'testqa' exists
#    Given I send a message 'hello' to the kafka topic named 'testqa'
#    Then The kafka topic 'testqa' has a message containing 'hello'
#
#  Scenario: Increase partitions in kafka topic
#    Given I increase '1' partitions in a Kafka topic named 'testqa'
#
#  Scenario: A kafka topic deletion
#    Given I create a Kafka topic named 'testqa' if it doesn't exists
#    Then A kafka topic named 'testqa' exists
#    When I delete a Kafka topic named 'testqa'
#    Then A kafka topic named 'testqa' does not exist

  Scenario: Managing schemas in the schema registry
      Given My schema registry is running at 'http://localhost:8081'
      Then I register a new version of a schema under the subject 'record' with 'schemas/recordSchema.avsc'