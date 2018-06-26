Feature: Kafka steps test

  Scenario: Connect to kafka
    Given I connect to kafka at '${ZOOKEEPER_HOST}'

  Scenario: Send message to kafka topic
    Given I connect to kafka at '${ZOOKEEPER_HOST}'
    Given I create a Kafka topic named 'testqa' if it doesn't exists
    Then A kafka topic named 'testqa' exists
    Given I send a message 'hello' to the kafka topic named 'testqa'
    Then The kafka topic 'testqa' has a message containing 'hello'

  Scenario: Increase partitions in kafka topic
    Given I increase '1' partitions in a Kafka topic named 'testqa'

  Scenario: A kafka topic deletion
    Given I create a Kafka topic named 'testqa' if it doesn't exists
    Then A kafka topic named 'testqa' exists
    When I delete a Kafka topic named 'testqa'
    Then A kafka topic named 'testqa' does not exist

  Scenario: Managing schemas in the schema registry
    Given My schema registry is running at '${SCHEMA_REGISTRY_HOST}'
    Then I register a new version of a schema under the subject 'record' with 'schemas/recordSchema.avsc'

  Scenario: Using String, Long, and AVRO serializers/deserializers
     Given I connect to kafka at '${ZOOKEEPER_HOST}'

    Given I create a Kafka topic named 'stringTopic' if it doesn't exists
    When I send a message 'hello' to the kafka topic named 'stringTopic'
    Then The kafka topic 'stringTopic' has a message containing 'hello'

    Given I create a Kafka topic named 'longTopic' if it doesn't exists
    When I send a message '1234567890' to the kafka topic named 'longTopic' with:
      | key.serializer    | org.apache.kafka.common.serialization.StringSerializer |
      | value.serializer  | org.apache.kafka.common.serialization.LongSerializer   |

    Then The kafka topic 'longTopic' has a message containing '1234567890' with:
      | key.deserializer    | org.apache.kafka.common.serialization.StringDeserializer |
      | value.deserializer  | org.apache.kafka.common.serialization.LongDeserializer   |

    Given My schema registry is running at '${SCHEMA_REGISTRY_HOST}'
    Then I register a new version of a schema under the subject 'record' with 'schemas/recordSchema.avsc'
    And I create a Kafka topic named 'avroTopic' if it doesn't exists
    Then I create the avro record 'record' from the schema in 'schemas/recordSchema.avsc' with:
      | str1    | str1 |
      | str2    | str2 |
      | int1    |   1  |
    When I send the avro record 'record' to the kafka topic 'avroTopic' with:
      | key.serializer    | org.apache.kafka.common.serialization.StringSerializer |
    Then The kafka topic 'avroTopic' has an avro message 'record' with:
      | key.deserializer    | org.apache.kafka.common.serialization.StringDeserializer |