package com.privalia.qa.specs;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.datatable.DataTable;
import okhttp3.Response;
import org.apache.avro.generic.GenericRecord;
import org.apache.zookeeper.KeeperException;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps definitions for working with Apache Kafka
 *
 * @author José Fernández
 */
public class KafkaGSpec extends BaseGSpec {

    /**
     * Instantiates a new Kafka g spec.
     *
     * @param spec the spec
     */
    public KafkaGSpec(CommonG spec) {
        this.commonspec = spec;
    }


    /**
     * Connect to Kafka.
     * <p>
     * Establish the connection to the Kafka cluster via the IP of the Zookeeper service. This is an initialization step necessary for all future steps
     *
     * <pre>
     * Example: Assuming zookeeper service is running at localhost:2181
     * {@code
     *      Given I connect to kafka at 'localhost:2181'
     * }
     * </pre>
     * @see #disconnectFromKafka()
     * @param zkHost ZK host
     * @param zkPath ZK port
     * @throws UnknownHostException exception
     */
    @Given("^I connect to kafka at '(.+?)'( using path '(.+?)')?$")
    public void connectToKafka(String zkHost, String zkPath) throws UnknownHostException {
        String zkPort = zkHost.split(":")[1];
        zkHost = zkHost.split(":")[0];
        commonspec.getKafkaUtils().setZkHost(zkHost, zkPort, zkPath);
        commonspec.getKafkaUtils().connect();
    }

    /**
     * Create a Kafka topic.
     * <p>
     * Creates a kafka topic with the given name. It can also create the topic only if it doesn't exists. All topics are created by default
     * with 1 partition and a replication factor of 1
     *
     * <pre>
     * Example: Create the topic 'testqa'
     * {@code
     *      Given I create a Kafka topic named 'testqa'
     * }
     * Example: Create the topic 'testqa' if it doesn't exists
     * {@code
     *      Given I create a Kafka topic named 'testqa' if it doesn't exists
     * }
     * </pre>
     *
     * @see #deleteTopic(String)
     * @see #assertTopicExists(String)
     * @see #modifyTopicPartitions(int, String)
     * @param topic_name topic name
     * @param ifExists   String fro matching optional text in Gherkin
     * @throws Exception Exception
     */
    @When("^I create a Kafka topic named '(.+?)'( if it doesn't exists)?")
    public void createTopic(String topic_name, String ifExists) throws Exception {
        if (ifExists != null) {
            commonspec.getLogger().debug("Checking if topic " + topic_name + " exists before creation");
            List<String> topics = this.commonspec.getKafkaUtils().listTopics();
            if (topics.contains(topic_name)) {
                return;
            }
        }

        commonspec.getLogger().debug("Creating topic " + topic_name);
        commonspec.getKafkaUtils().createTopic(topic_name);
    }

    /**
     * Delete a Kafka topic.
     * <pre>
     * Example: Delete the topic 'testqa'
     * {@code
     *      When I delete a Kafka topic named 'testqa'
     * }
     * </pre>
     *
     * @see #createTopic(String, String)
     * @see #assertTopicExists(String)
     * @see #modifyTopicPartitions(int, String)
     * @param topic_name topic name
     * @throws Exception Exception
     */
    @When("^I delete a Kafka topic named '(.+?)'")
    public void deleteTopic(String topic_name) throws Exception {
        commonspec.getKafkaUtils().deleteTopic(topic_name);
    }

    /**
     * Increase partitions in kafka topic
     * <p>
     * Modify partitions in a Kafka topic by increasing the current number of partitions in the topic by the specified
     * number. Mind that the number of partitions for a topic can only be increased once its created
     *
     * <pre>
     * Example: Increase partitions topic 'testqa'
     * {@code
     *      Given I increase '1' partitions in a Kafka topic named 'testqa'
     * }
     * </pre>
     *
     * @see #deleteTopic(String)
     * @see #createTopic(String, String)
     * @see #modifyTopicPartitions(int, String)
     * @param numPartitions number of partitions to add to the current amount of partitions for the topic
     * @param topic_name    topic name
     * @throws Exception Exception
     */
    @When("^I increase '(.+?)' partitions in a Kafka topic named '(.+?)'")
    public void modifyTopicPartitions(int numPartitions, String topic_name) throws Exception {
        int currentPartitions = commonspec.getKafkaUtils().getPartitions(topic_name);
        commonspec.getKafkaUtils().modifyTopicPartitioning(topic_name, currentPartitions + numPartitions);
        assertThat(commonspec.getKafkaUtils().getPartitions(topic_name)).as("Number of partitions is not the expected after operation").isEqualTo(currentPartitions + numPartitions);
    }

    /**
     * Sends a message to a Kafka topic.
     * <p>
     * By default, this steps uses StringSerializer and StringDeserializer for
     * the key/value of the message, and default properties for the producer. This steps can also verify if a message
     * with the corresponding key and value already exists in the topic before inserting. Remember that for a consumer to
     * be able to read all the messages from a topic, it must use a new group.id (not used before in that topic). When reading
     * from a kafka topic with a consumer, kafka will return the next message by offset not read for that group.
     *
     * <pre>
     * Example: For sending a simple message (only specifying value)
     * {@code
     *      Given I send a message 'hello' to the kafka topic named 'testqa'
     * }
     * Example: For sending a message with key and value (specifying kay and value)
     * {@code
     *      Given I send a message 'hello' to the kafka topic named 'testqa' with key 'keyvalue'
     * }
     * Example: To insert a message only if the exact key-value combination does not already exists in the topic
     * {@code
     *      Given I send a message 'hello' to the kafka topic named 'testqa' with key 'keyvalue' if not exists
     * }
     * </pre>
     *
     * @see #sendMessageToTopicWithProperties(String, String, String, DataTable)
     * @see #sendAvroMessageToTopicWithProperties(String, String, String, DataTable)
     * @param message    string that you send to topic
     * @param topic_name topic name
     * @param recordKey  the record key
     * @param ifExists   for handling optional text in Gherkin
     * @throws Exception Exception
     */
    @When("^I send a message '(.+?)' to the kafka topic named '(.+?)'( with key '(.+?)')?( if not exists)?$")
    public void sendMessageToTopic(String message, String topic_name, String recordKey, String ifExists) throws Exception {
        if (ifExists != null) {
            Map<Object, Object> result = commonspec.getKafkaUtils().readTopicFromBeginning(topic_name);
            if (result.containsKey(recordKey)) {
                if (!result.get(recordKey).toString().matches(message)) {
                    commonspec.getKafkaUtils().sendAndConfirmMessage(message, recordKey, topic_name, 1);
                }
            } else {
                commonspec.getKafkaUtils().sendAndConfirmMessage(message, recordKey, topic_name, 1);
            }
        } else {
            commonspec.getKafkaUtils().sendAndConfirmMessage(message, recordKey, topic_name, 1);
        }
    }

    /**
     * Sends a message to a Kafka topic with properties.
     * <p>
     * Similar to the {@link #sendMessageToTopic(String, String, String, String)} step, but gives the possibility to override
     * the properties of the producer before sending a message. For example, the properties of the producer can be altered
     * to change the default serializer for key/value before sending. In this case, for the key.serializer property the
     * value should be "org.apache.kafka.common.serialization.StringSerializer" and for the value.serializer the value
     * should be "org.apache.kafka.common.serialization.LongSerializer".
     * <p>
     * The library will try to automatically cast the message to the type of the specified value.serializer property. So,
     * for example, trying to send the message "hello" using LongSerializer for value.serializer will produce an error.
     *
     * <pre>
     * Example: For sending a message to topic 'longTopic' with key as String and value as Long
     * {@code
     *      When I send a message '1234567890' to the kafka topic named 'longTopic' with:
     *          | key.serializer    | org.apache.kafka.common.serialization.StringSerializer |
     *          | value.serializer  | org.apache.kafka.common.serialization.LongSerializer   |
     * }
     *
     * Other common properties for a Kafka consumer:
     * - bootstrap.servers (defaults to 0.0.0.0:9092)
     * - acks (defaults to "all")
     * - retries (defaults to 0)
     * - batch.size (defaults to 16384)
     * - linger.ms (defaults to 1)
     * - buffer.memory (defaults to 33554432)
     * - client.id (defaults to KafkaQAProducer)
     * </pre>
     *
     * @see #sendMessageToTopic(String, String, String, String)
     * @see #sendAvroMessageToTopicWithProperties(String, String, String, DataTable)
     * @param message    Message to send (will be converted to the proper type specified by the value.serializer prop). String is default
     * @param topic_name Name of the topic where to send the message
     * @param recordKey  Key of the kafka record
     * @param table      Table containing alternative properties for the producer
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException   ExecutionException
     * @throws TimeoutException     TimeoutException
     */
    @Given("I send a message '(.+?)' to the kafka topic named '(.+?)'( with key '(.+?)')? with:$")
    public void sendMessageToTopicWithProperties(String message, String topic_name, String recordKey, DataTable table) throws InterruptedException, ExecutionException, TimeoutException {

        for (List<String> row : table.asLists()) {
            String key = row.get(0);
            String value = row.get(1);
            commonspec.getKafkaUtils().modifyProducerProperties(key, value);
        }

        commonspec.getKafkaUtils().sendAndConfirmMessage(message, recordKey, topic_name, 1);

    }

    /**
     * Check that a kafka topic does not exist
     * <pre>
     * Example:
     * {@code
     *      Then A kafka topic named 'testqa' does not exist
     * }
     * </pre>
     *
     * @see #createTopic(String, String)
     * @see #deleteTopic(String)
     * @see #modifyTopicPartitions(int, String)
     * @see #assertTopicExists(String)
     * @param topic_name name of topic
     * @throws KeeperException      KeeperException
     * @throws InterruptedException InterruptedException
     */
    @Then("^A kafka topic named '(.+?)' does not exist")
    public void assertTopicDoesntExist(String topic_name) throws KeeperException, InterruptedException {
        assert !commonspec.getKafkaUtils().getZkUtils().pathExists("/" + topic_name) : "There is a topic with that name";
    }

    /**
     * Check that the number of partitions is the expected for the given topic.
     *
     * @see #modifyTopicPartitions(int, String)
     * @param topic_name      Name of kafka topic
     * @param numOfPartitions Number of partitions
     * @throws Exception Exception
     */
    @Then("^The number of partitions in topic '(.+?)' should be '(.+?)''?$")
    public void checkNumberOfPartitions(String topic_name, int numOfPartitions) throws Exception {
        assertThat(commonspec.getKafkaUtils().getPartitions(topic_name)).isEqualTo(numOfPartitions);

    }

    /**
     * Check if message exists
     * <p>
     * Pools the given topic for messages and checks if any have the given value. By default, this method
     * uses String Serializer/Deserializer to read the messages from the topic (as well as all the default properties for
     * the consumer).
     * <p>
     * Unless specified, the method will only look for records that contain the specific message in the value of the kafka
     * record but it can also be used.
     *
     * <pre>
     * Example: Check if the topic contains the message with the given value
     * {@code
     *      Then The kafka topic 'testqa' has a message containing 'hello'
     * }
     * Example: Check if the topic contains the message with the given key
     * {@code
     *      Then The kafka topic 'testqa' has a message containing 'hello' as key
     * }
     * </pre>
     *
     * @see #assertTopicContainsMessageWithProperties(String, String, String, DataTable)
     * @see #assertTopicContainsAvroMessageWithProperties(String, String, DataTable)
     * @see #assertTopicContainsPartialAvroMessageWithProperties(String, String, int, DataTable)
     * @param topic   Topic to poll
     * @param content Value to look for (as String)
     * @param key     key of the record
     * @throws InterruptedException InterruptedException
     */
    @Then("^The kafka topic '(.*?)' has a message containing '(.*?)'( as key)?$")
    public void assertTopicContainsMessage(String topic, String content, String key) throws InterruptedException {
        if (key != null) {
            assertThat(commonspec.getKafkaUtils().readTopicFromBeginning(topic).containsKey(content)).as("Topic does not exist or the content does not match").isTrue();
        } else {
            assertThat(commonspec.getKafkaUtils().readTopicFromBeginning(topic).containsValue(content)).as("Topic does not exist or the content does not match").isTrue();
        }
    }

    /**
     * Check that a kafka topic exist
     *
     * <pre>
     * Example: Verify the topic 'testqa' exists
     * {@code
     *      Then A kafka topic named 'testqa' exists
     * }
     * </pre>
     *
     * @see #createTopic(String, String)
     * @see #deleteTopic(String)
     * @see #assertTopicDoesntExist(String)
     * @see #modifyTopicPartitions(int, String)
     * @param topic_name name of topic
     * @throws KeeperException      KeeperException
     * @throws InterruptedException InterruptedException
     */
    @Then("^A kafka topic named '(.+?)' exists")
    public void assertTopicExists(String topic_name) throws KeeperException, InterruptedException {
        List<String> topics = this.commonspec.getKafkaUtils().listTopics();
        assertThat(topics.contains(topic_name)).as("There is no topic with that name").isTrue();
    }


    /**
     * Sets URL of schema registry.
     * <p>
     * Initializes the remote URL of the schema registry service for all future requests. Also sets the property
     * schema.registry.url in the consumer and producer properties
     *
     * <pre>
     * Example: To set the schema registry at localhost:8081:
     * {@code
     *      Given My schema registry is running at 'localhost:8081'
     * }
     * </pre>
     *
     * @see #registerNewSchema(String, String)
     * @see #createNewAvroMessageFromRegistry(String, String, String, String, DataTable)
     * @param host Remote host and port (defaults to http://0.0.0.0:8081)
     * @throws Throwable Throwable
     */
    @Given("^My schema registry is running at '(.+)'$")
    public void setSchemaRegistryURL(String host) throws Throwable {
        commonspec.getKafkaUtils().setSchemaRegistryUrl("http://" + host);
        commonspec.getKafkaUtils().modifyProducerProperties("schema.registry.url", "http://" + host);
    }

    /**
     * Adds a new schema to the schema register.
     * <p>
     * Generates a POST to the schema register to add a new schema for the given subject
     * <pre>
     * Example: Assuming the file located under schemas/recordSchema.avsc contains the following valid schema
     *
     * {
     *     "namespace": "com.mynamespace",
     *     "type": "record",
     *     "name": "Record",
     *     "fields": [
     *         { "name": "str1", "type": "string" },
     *         { "name": "str2", "type": "string" },
     *         { "name": "int1", "type": "int" }
     *      ]
     * }
     *
     * Then, to set it at the schema registry at localhost:8081:
     * {@code
     *      Given My schema registry is running at 'localhost:8081'
     *      Then I register a new version of a schema under the subject 'record' with 'schemas/recordSchema.avsc'
     * }
     * </pre>
     *
     * @see #setSchemaRegistryURL(String)
     * @see #createNewAvroMessageFromRegistry(String, String, String, String, DataTable)
     * @param subjectName Name of the subject where register the new schema
     * @param filepath    Path of the file containing the schema
     * @throws Throwable Throwable
     */
    @Then("^I register a new version of a schema under the subject '(.+)' with '(.+)'$")
    public void registerNewSchema(String subjectName, String filepath) throws Throwable {

        String retrievedData = commonspec.retrieveData(filepath, "json");
        Response response = commonspec.getKafkaUtils().registerNewSchema(subjectName, retrievedData);
        assertThat(response.code()).as("Schema registry returned " + response.code() + " response, body: " + response.body().string()).isEqualTo(200);

    }

    /**
     * Reads message from topic with properties
     * <p>
     * Reads messages from the beginning of the topic with the specified properties for the consumer. The message is casted to the
     * correct type based on the given value.deserializer property (uses String deserializer by default)
     * <p>
     * For example, the properties of the consumer can be altered to change the default deserializer for key/value when reading.
     * In this case, for the key.deserializer property the value should be "org.apache.kafka.common.serialization.StringDeserializer"
     * and for the value.deserializer the value should be "org.apache.kafka.common.serialization.LongDeserializer".
     * <p>
     * The library will try to automatically cast the message to the type of the specified value.deserializer property.
     * So, for example, trying to read the message "hello" using LongSerializer for value.serializer will produce an error.
     *
     * <pre>
     * Example: Setting the read properties as String for key and Long for value before reading
     * {@code
     *      Then The kafka topic 'longTopic' has a message containing '1234567890' with:
     *          | key.deserializer    | org.apache.kafka.common.serialization.StringDeserializer |
     *          | value.deserializer  | org.apache.kafka.common.serialization.LongDeserializer   |
     * }
     *
     * Other common properties for a Kafka consumer:
     *
     * - bootstrap.servers (defaults to 0.0.0.0:9092)
     * - group.id (defaults to "test")
     * - enable.auto.commit (defaults to true)
     * - auto.offset.reset (defaults to 'earliest')
     * - auto.commit.intervals.ms (defaults to 1000)
     * - session.timeout (defaults to 10000)
     *
     * </pre>
     *
     * @see #assertTopicContainsMessage(String, String, String)
     * @see #assertTopicContainsPartialAvroMessageWithProperties(String, String, int, DataTable)
     * @see #assertTopicContainsAvroMessageWithProperties(String, String, DataTable)
     * @param topicName Name of the topic where to send the message
     * @param message   Message to send (will be converted to the correct type according to the value.deserializer property)
     * @param isKey     the is key
     * @param dataTable Table containing properties for consumer
     * @throws Throwable Throwable
     */
    @Then("^The kafka topic '(.+?)' has a message containing '(.+?)'( as key)? with:$")
    public void assertTopicContainsMessageWithProperties(String topicName, String message, String isKey, DataTable dataTable) throws Throwable {

        for (List<String> row : dataTable.asLists()) {
            String key = row.get(0);
            String value = row.get(1);
            commonspec.getKafkaUtils().modifyConsumerProperties(key, value);
        }

        Map<Object, Object> results = commonspec.getKafkaUtils().readTopicFromBeginning(topicName);

        if (isKey != null) {
            assertThat(results.containsKey(this.getFinalMessage("key.deserializer", message))).as("Topic does not exist or the content does not match").isTrue();
        } else {
            assertThat(results.containsValue(this.getFinalMessage("value.deserializer", message))).as("Topic does not exist or the content does not match").isTrue();
        }

    }

    private Object getFinalMessage(String type, String message) {

        String deserializer = commonspec.getKafkaUtils().getPropsConsumer().getProperty(type);
        Object finalMessage;

        switch (deserializer) {

            case "org.apache.kafka.common.serialization.StringDeserializer":
                finalMessage = message.toString();
                break;

            case "org.apache.kafka.common.serialization.LongDeserializer":
                finalMessage = Long.parseLong(message);
                break;

            default:
                finalMessage = message.toString();
        }

        return finalMessage;
    }

    /**
     * Creates an Avro record from the specified schema.
     * <p>
     * The record is created as a {@link GenericRecord} and the user can dynamically specify the properties values.
     *
     * <pre>
     * Example: To create a new GenericRecord with name 'record' using the schema under schemas/recordSchema.avsc
     * {@code
     *      Given My schema registry is running at 'localhost:8081'
     *      Then I register a new version of a schema under the subject 'record' with 'schemas/recordSchema.avsc'
     *      Then I create the avro record 'record' from the schema in 'schemas/recordSchema.avsc' with:
     *          | str1    | str1 |
     *          | str2    | str2 |
     *          | int1    |   1  |
     * }
     *
     * The library will automatically cast the variables to the correct types specified in the *.avsc file. So, trying
     * to insert an string for "int1" will generate an error
     * </pre>
     *
     * @see #sendAvroMessageToTopicWithProperties(String, String, String, DataTable)
     * @see #createNewAvroMessageFromRegistry(String, String, String, String, DataTable)
     * @param recordName Name of the Avro generic record
     * @param schemaFile File containing the schema of the message
     * @param seedFile   the seed file
     * @param table      Table contains the values for the fields on the schema. (Values will be converted according to field type)
     * @throws Throwable Throwable
     */
    @Then("^I create the avro record '(.+?)' from the schema in '(.+?)'( based on '(.+?)')? with:$")
    public void createNewAvroMessage(String recordName, String schemaFile, String seedFile, DataTable table) throws Throwable {

        String schemaString = commonspec.retrieveData(schemaFile, "json");
        this.createRecord(recordName, schemaString, seedFile, table);

    }

    /**
     * Creates a new Avro record by reading the schema directly from the schema registry for the specified subject and version
     *
     * @param recordName    Name of the record
     * @param versionNumber Verison number of the schema
     * @param subject       Subject name
     * @param seedFile      Seed file to use
     * @param table         Modifications datatable
     * @throws Throwable Throwable
     */
    @Then("^I create the avro record '(.+?)' using version '(.+?)' of subject '(.+?)' from registry( based on '(.+?)')? with:$")
    public void createNewAvroMessageFromRegistry(String recordName, String versionNumber, String subject, String seedFile, DataTable table) throws Throwable {

        String schema = this.commonspec.getKafkaUtils().getSchemaFromRegistry(subject, versionNumber);
        this.createRecord(recordName, schema, seedFile, table);

    }

    /**
     * Creates the Avro record given the final name of the record, the schema to use, a seedFile and a datatable
     *
     * @param recordName   Name of the record
     * @param schemaString Schema to use as String
     * @param seedFile     File to use as initial set of data (if null, the record will be created using the data from the datatable)
     * @param table        Datatable with values for the parameters of the schema (or set of modifications for the seed file)
     * @throws Exception   Exception
     */
    private void createRecord(String recordName, String schemaString, String seedFile, DataTable table) throws Exception {


        if (seedFile != null) {
            commonspec.getLogger().debug("Building Avro record from seed file");

            // Retrieve data
            String seedJson = commonspec.retrieveData(seedFile, "json", "ISO-8859-1");

            // Modify data
            commonspec.getLogger().debug("Modifying data {} as {}", seedJson, "json");
            String modifiedData = commonspec.modifyData(seedJson, "json", table).toString();

            commonspec.getKafkaUtils().createGenericRecord(recordName, modifiedData, schemaString);

        } else {
            commonspec.getLogger().debug("Building Avro record from datatable");

            Map<String, String> properties = new HashMap<>();

            for (List<String> row : table.asLists()) {
                properties.put(row.get(0), row.get(1));
            }

            commonspec.getKafkaUtils().createGenericRecord(recordName, properties, schemaString);
        }
    }

    /**
     * Send the previously created Avro record.
     * <p>
     * The value.serializer property for the producer is set to KafkaAvroSerializer automatically
     *
     * <pre>
     * Example:
     * {@code
     *      When I send the avro record 'record' to the kafka topic 'avroTopic' with:
     *          | key.serializer    | org.apache.kafka.common.serialization.StringSerializer |
     * }
     * </pre>
     *
     * @see #createNewAvroMessage(String, String, String, DataTable)
     * @see #createNewAvroMessageFromRegistry(String, String, String, String, DataTable)
     * @param genericRecord Name of the record to send
     * @param topicName     Topic where to send the record
     * @param recordKey     Record key
     * @param table         Table containing modifications for the producer properties
     * @throws Throwable Throwable
     */
    @When("^I send the avro record '(.+?)' to the kafka topic '(.+?)'( with key '(.+?)')? with:$")
    public void sendAvroMessageToTopicWithProperties(String genericRecord, String topicName, String recordKey, DataTable table) throws Throwable {

        for (List<String> row : table.asLists()) {
            String key = row.get(0);
            String value = row.get(1);
            commonspec.getKafkaUtils().modifyProducerProperties(key, value);
        }

        commonspec.getKafkaUtils().modifyProducerProperties("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");

        GenericRecord record = commonspec.getKafkaUtils().getAvroRecords().get(genericRecord);
        assertThat(record).as("No generic record found with name " + genericRecord).isNotNull();
        commonspec.getKafkaUtils().sendAndConfirmMessage(genericRecord, recordKey, topicName, 1);

    }

    /**
     * Search the topic for the given Avro record.
     * <p>
     * Reads the specified topic from beginning for the specified avro record. The consumer value.deserializer property is automatically
     * set to KafkaAvroDeserializer
     *
     * <pre>
     * Example:
     * {@code
     *      Then The kafka topic 'avroTopic' has an avro message 'record' with:
     *          | key.deserializer    | org.apache.kafka.common.serialization.StringDeserializer |
     * }
     * </pre>
     *
     * @see #createNewAvroMessage(String, String, String, DataTable)
     * @see #createNewAvroMessageFromRegistry(String, String, String, String, DataTable)
     * @see #sendAvroMessageToTopicWithProperties(String, String, String, DataTable)
     * @param topicName  Topic to read from
     * @param avroRecord Name of the record to read
     * @param dataTable  Table containing modifications for the consumer properties
     * @throws Throwable Throwable
     */
    @Then("^The kafka topic '(.+?)' has an avro message '(.+?)' with:$")
    public void assertTopicContainsAvroMessageWithProperties(String topicName, String avroRecord, DataTable dataTable) throws Throwable {

        for (List<String> row : dataTable.asLists()) {
            String key = row.get(0);
            String value = row.get(1);
            commonspec.getKafkaUtils().modifyConsumerProperties(key, value);
        }

        commonspec.getKafkaUtils().modifyConsumerProperties("value.deserializer", "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        assertThat(this.getCommonSpec().getKafkaUtils().getSchemaRegistryUrl()).as("Could not build avro consumer since no schema registry was defined").isNotNull();
        commonspec.getKafkaUtils().modifyConsumerProperties("schema.registry.url", this.getCommonSpec().getKafkaUtils().getSchemaRegistryUrl());


        this.assertTopicExists(topicName);
        Map<Object, Object> results = commonspec.getKafkaUtils().readTopicFromBeginning(topicName);
        this.commonspec.getLogger().debug("Found " + results.size() + "records in topic " + topicName);
        assertThat(results.containsValue(commonspec.getKafkaUtils().getAvroRecords().get(avroRecord))).as("Topic does not contain message that matches the specified record").isTrue();

    }

    /**
     * Modify consumer properties
     * <p>
     * A single step for modifying the consumer properties for the rest of the scenario.
     *
     * <pre>
     * Example: To change consumer properties
     * {@code
     *      Then I configure the kafka consumers with:
     *          | group.id           | !{ID}                                                                                               |
     *          | key.deserializer   | org.apache.kafka.common.serialization.StringDeserializer                                            |
     *          | value.deserializer | org.apache.kafka.common.serialization.StringDeserializer                                            |
     *          | bootstrap.servers  | srdc1kafkassl5.privalia.pin:9092,srdc1kafkassl9.privalia.pin:9092,srdc1kafkassl10.privalia.pin:9092 |
     * }
     * </pre>
     * @see #configureProducerProperties(DataTable)
     * @param dataTable table with consumer properties
     */
    @Then("^I configure the kafka consumers with:$")
    public void configureConsumerProperties(DataTable dataTable) {

        for (List<String> row : dataTable.asLists()) {
            String key = row.get(0);
            String value = row.get(1);
            this.getCommonSpec().getLogger().debug("Setting kafka consumer property: " + key + " -> " + value);
            commonspec.getKafkaUtils().modifyConsumerProperties(key, value);
        }

    }


    /**
     * Modify producer properties
     * <p>
     * A single step for modifying the producer properties for the rest of the scenario.
     *
     * <pre>
     * Example: To change producer properties settings:
     * {@code
     *      Then I configure the kafka producer with:
     *          | client.id         | QAderInjector                                                                                       |
     *          | key.serializer    | org.apache.kafka.common.serialization.StringSerializer                                              |
     *          | value.serializer  | org.apache.kafka.common.serialization.StringSerializer                                              |
     *          | bootstrap.servers | srdc1kafkassl5.privalia.pin:9092,srdc1kafkassl9.privalia.pin:9092,srdc1kafkassl10.privalia.pin:9092 |
     * }
     * </pre>
     *
     * @see #configureConsumerProperties(DataTable)
     * @param dataTable table with consumer properties
     */
    @Then("^I configure the kafka producer with:$")
    public void configureProducerProperties(DataTable dataTable) {

        for (List<String> row : dataTable.asLists()) {
            String key = row.get(0);
            String value = row.get(1);
            this.getCommonSpec().getLogger().debug("Setting kafka producer property: " + key + " -> " + value);
            commonspec.getKafkaUtils().modifyProducerProperties(key, value);
        }

    }

    /**
     * Close the connection to kafka.
     *
     * <pre>
     * Example:
     * {@code
     *      Then I close the connection to kafka
     * }
     * </pre>
     *
     * @see #connectToKafka(String, String)
     * @throws Throwable the throwable
     */
    @Then("^I close the connection to kafka$")
    public void disconnectFromKafka() throws Throwable {

        this.getCommonSpec().getLogger().debug("Closing connection to kafka..");
        if (this.getCommonSpec().getKafkaUtils().getZkUtils() != null) {
            this.getCommonSpec().getKafkaUtils().getZkUtils().close();
        }

    }

    /**
     * Performs a partial property matching on the avro records returned
     *
     * <pre>
     * Example:
     * {@code
     *      Then The kafka topic 'avroTopic' has at least '1' an avro message with:
     *          | user.id    | Paul |
     * }
     * </pre>
     *
     * @see #assertTopicContainsAvroMessageWithProperties(String, String, DataTable)
     * @param topicName     Name of the topic to read messages from
     * @param atLeast       Indicates to find at least the expectedCount. If ignored, asserts the exact quantity is found
     * @param expectedCount Expected amount of records to find that match the given conditions
     * @param datatable     Expected conditions
     * @throws Throwable    the throwable
     */
    @And("^The kafka topic '(.+?)' has( at least)? '(.+?)' an avro message with:$")
    public void assertTopicContainsPartialAvroMessageWithProperties(String topicName, String atLeast, int expectedCount, DataTable datatable) throws Throwable {

        commonspec.getKafkaUtils().modifyConsumerProperties("value.deserializer", "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        assertThat(this.getCommonSpec().getKafkaUtils().getSchemaRegistryUrl()).as("Could not build avro consumer since no schema registry was defined").isNotNull();
        commonspec.getKafkaUtils().modifyConsumerProperties("schema.registry.url", this.getCommonSpec().getKafkaUtils().getSchemaRegistryUrl());
        this.assertTopicExists(topicName);

        Map<Object, Object> results = commonspec.getKafkaUtils().readTopicFromBeginning(topicName);

        int matches = results.size();
        for (Object result: results.values()) {

            if (result instanceof GenericRecord) {
                GenericRecord avroMessage = (GenericRecord) result;

                String jsonString = avroMessage.toString();

                for (List<String> row : datatable.asLists()) {
                    String expression = row.get(0);
                    String condition = row.get(1);
                    String expectedResult = row.get(2);

                    String value = commonspec.getJSONPathString(jsonString, expression, null);
                    try {
                        commonspec.evaluateJSONElementOperation(value, condition, expectedResult);
                    } catch (AssertionError e) {
                        matches--;
                        break;
                    }

                }
            }
        }

        this.getCommonSpec().getLogger().debug("Found " + matches + "records in topic " + topicName + " that match the specified conditions");

        if (atLeast != null) {
            assertThat(matches).as("No matches found").isGreaterThanOrEqualTo(expectedCount);
        } else {
            assertThat(matches).as("No matches found").isEqualTo(expectedCount);
        }

    }
}