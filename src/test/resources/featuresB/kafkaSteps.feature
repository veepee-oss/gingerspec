Feature: Kafka steps test.


  Scenario: Connect to kafka
    Given I connect to kafka at '${ZOOKEEPER_HOST}:2181'
    Then I close the connection to kafka


  Scenario: Send message to kafka topic
    Given I connect to kafka at '${ZOOKEEPER_HOST}:2181'
    Given I create a Kafka topic named 'testqa' if it doesn't exists
    Then A kafka topic named 'testqa' exists
    Given I send a message 'hello' to the kafka topic named 'testqa'
    Then The kafka topic 'testqa' has a message containing 'hello'
    Then I close the connection to kafka


  Scenario: Increase partitions in kafka topic
    Given I connect to kafka at '${ZOOKEEPER_HOST}:2181'
    Given I create a Kafka topic named 'testqa' if it doesn't exists
    Then A kafka topic named 'testqa' exists
    Given I increase '1' partitions in a Kafka topic named 'testqa'
    Then I close the connection to kafka


  Scenario: A kafka topic deletion
    Given I connect to kafka at '${ZOOKEEPER_HOST}:2181'
    Given I create a Kafka topic named 'testqa' if it doesn't exists
    Then A kafka topic named 'testqa' exists
    When I delete a Kafka topic named 'testqa'
    Then A kafka topic named 'testqa' does not exist
    Then I close the connection to kafka


  Scenario: Managing schemas in the schema registry
    Given My schema registry is running at '${SCHEMA_REGISTRY_HOST}:8081'
    Then I register a new version of a schema under the subject 'record' with 'schemas/recordSchema.avsc'


  Scenario: Using String, Long serializers/deserializers
    Given I connect to kafka at '${ZOOKEEPER_HOST}:2181'
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
    Then I close the connection to kafka


  Scenario: Using AVRO serializers/deserializers
    Given I connect to kafka at '${ZOOKEEPER_HOST}:2181'
    Given My schema registry is running at '${SCHEMA_REGISTRY_HOST}:8081'
    Then I register a new version of a schema under the subject 'record' with 'schemas/recordSchema.avsc'
    And I create a Kafka topic named 'avroTopic' if it doesn't exists
    #log if no seed file present, the datatable represents the values for every key in the schema
    Then I create the avro record 'record' from the schema in 'schemas/recordSchema.avsc' with:
      | str1    | str1 |
      | str2    | str2 |
      | int1    |   1  |
    #log if a seed file is present, the datatable represents a set of modifications to apply to the seed file
    Then I create the avro record 'record2' from the schema in 'schemas/recordSchema.avsc' based on 'schemas/recordSeed.json' with:
      | $.str1  | UPDATE | new_str1 |
    #log if no seed file present, the datatable represents the values for every key in the schema
    Then I create the avro record 'record3' using version '1' of subject 'record' from registry with:
      | str1    | str1 |
      | str2    | str2 |
      | int1    |   1  |
    #log if a seed file is present, the datatable represents a set of modifications to apply to the seed file
    Then I create the avro record 'record4' using version '1' of subject 'record' from registry based on 'schemas/recordSeed.json' with:
      | $.str2  | UPDATE | new_str2 |
    When I send the avro record 'record' to the kafka topic 'avroTopic' with:
      | key.serializer    | org.apache.kafka.common.serialization.StringSerializer |
    Then The kafka topic 'avroTopic' has an avro message 'record' with:
      | key.deserializer    | org.apache.kafka.common.serialization.StringDeserializer |
    When I send the avro record 'record2' to the kafka topic 'avroTopic' with:
      | key.serializer    | org.apache.kafka.common.serialization.StringSerializer |
    Then The kafka topic 'avroTopic' has an avro message 'record2' with:
      | key.deserializer    | org.apache.kafka.common.serialization.StringDeserializer |
    Then I close the connection to kafka
