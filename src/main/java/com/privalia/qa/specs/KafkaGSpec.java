package com.privalia.qa.specs;

import com.privalia.qa.utils.ThreadProperty;
import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import gherkin.formatter.model.DataTableRow;
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
 * Class for all Kafka-related cucumber steps
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
     *
     * @param zkHost ZK host
     * @param foo    required parameter by gherkin for matching the optional string
     * @param zkPath ZK port
     * @throws UnknownHostException exception
     */
    @Given("^I connect to kafka at '(.+?)'( using path '(.+?)')?$")
    public void connectKafka(String zkHost, String foo, String zkPath) throws UnknownHostException {
        String zkPort = zkHost.split(":")[1];
        zkHost = zkHost.split(":")[0];
        commonspec.getKafkaUtils().setZkHost(zkHost, zkPort, zkPath);
        commonspec.getKafkaUtils().connect();
    }

    /**
     * Create a Kafka topic.
     *
     * @param topic_name topic name
     * @param ifExists   String fro matching optional text in Gherkin
     * @throws Exception Exception
     */
    @When("^I create a Kafka topic named '(.+?)'( if it doesn't exists)?")
    public void createKafkaTopic(String topic_name, String ifExists) throws Exception {
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
     *
     * @param topic_name topic name
     * @throws Exception Exception
     */
    @When("^I delete a Kafka topic named '(.+?)'")
    public void deleteKafkaTopic(String topic_name) throws Exception {
        commonspec.getKafkaUtils().deleteTopic(topic_name);
    }

    /**
     * Modify partitions in a Kafka topic by increasing the current number of partitions in the topic by the specified
     * number. Mind that the number of partitions for a topic can only be increased once its created
     *
     * @param numPartitions number of partitions to add to the current amount of partitions for the topic
     * @param topic_name    topic name
     * @throws Exception Exception
     */
    @When("^I increase '(.+?)' partitions in a Kafka topic named '(.+?)'")
    public void modifyPartitions(int numPartitions, String topic_name) throws Exception {
        int currentPartitions = commonspec.getKafkaUtils().getPartitions(topic_name);
        commonspec.getKafkaUtils().modifyTopicPartitioning(topic_name, currentPartitions + numPartitions);
        assertThat(commonspec.getKafkaUtils().getPartitions(topic_name)).as("Number of partitions is not the expected after operation").isEqualTo(currentPartitions + numPartitions);
    }

    /**
     * Sends a message to a Kafka topic. By default, this steps uses StringSerializer and StringDeserializer for
     * the key/value of the message, and default properties for the producer. This steps can also verify if a message
     * with the corresponding key and value already exists in the topic before inserting. Remember that for a consumer to
     * be able to read all the messages from a topic, it must use a new group.id (not used before in that topic)
     *
     * @param message    string that you send to topic
     * @param topic_name topic name
     * @param foo        the foo
     * @param recordKey  the record key
     * @param ifExists   for handling optional text in Gherkin
     * @throws Exception Exception
     */
    @When("^I send a message '(.+?)' to the kafka topic named '(.+?)'( with key '(.+?)')?( if not exists)?$")
    public void sendAMessage(String message, String topic_name, String foo, String recordKey, String ifExists) throws Exception {
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
     * Sends a message to a Kafka topic. This steps allows to modify any property of the producer before sending
     *
     * @param message    Message to send (will be converted to the proper type specified by the value.serializer prop). String is default
     * @param topic_name Name of the topic where to send the message
     * @param foo        Parameter required by Gherkin for optional text
     * @param recordKey  Key of the kafka record
     * @param table      Table containing alternative properties for the producer
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException   ExecutionException
     * @throws TimeoutException     TimeoutException
     */
    @Given("I send a message '(.+?)' to the kafka topic named '(.+?)'( with key '(.+?)')? with:$")
    public void sendAMessageWithDatatable(String message, String topic_name, String foo, String recordKey, DataTable table) throws InterruptedException, ExecutionException, TimeoutException {

        /*Modify properties of producer*/
        for (DataTableRow row : table.getGherkinRows()) {
            String key = row.getCells().get(0);
            String value = row.getCells().get(1);
            commonspec.getKafkaUtils().modifyProducerProperties(key, value);
        }

        commonspec.getKafkaUtils().sendAndConfirmMessage(message, recordKey, topic_name, 1);

    }

    /**
     * Check that a kafka topic does not exist
     *
     * @param topic_name name of topic
     * @throws KeeperException      KeeperException
     * @throws InterruptedException InterruptedException
     */
    @Then("^A kafka topic named '(.+?)' does not exist")
    public void kafkaTopicNotExist(String topic_name) throws KeeperException, InterruptedException {
        assert !commonspec.getKafkaUtils().getZkUtils().pathExists("/" + topic_name) : "There is a topic with that name";
    }

    /**
     * Check that the number of partitions is the expected for the given topic.
     *
     * @param topic_name      Name of kafka topic
     * @param numOfPartitions Number of partitions
     * @throws Exception Exception
     */
    @Then("^The number of partitions in topic '(.+?)' should be '(.+?)''?$")
    public void checkNumberOfPartitions(String topic_name, int numOfPartitions) throws Exception {
        assertThat(commonspec.getKafkaUtils().getPartitions(topic_name)).isEqualTo(numOfPartitions);

    }

    /**
     * Pools the given topic for messages and checks if any have the given value. By default, this method
     * uses String Serializer/Deserializer to read the messages from the topic (as well as all the default properties for
     * the consumer)
     *
     * @param topic   Topic to poll
     * @param content Value to look for (as String)
     * @param key     key of the record
     * @throws InterruptedException InterruptedException
     */
    @Then("^The kafka topic '(.*?)' has a message containing '(.*?)'( as key)?$")
    public void checkMessages(String topic, String content, String key) throws InterruptedException {
        if (key != null) {
            assertThat(commonspec.getKafkaUtils().readTopicFromBeginning(topic).containsKey(content)).as("Topic does not exist or the content does not match").isTrue();
        } else {
            assertThat(commonspec.getKafkaUtils().readTopicFromBeginning(topic).containsValue(content)).as("Topic does not exist or the content does not match").isTrue();
        }
    }

    /**
     * Check that a kafka topic exist
     *
     * @param topic_name name of topic
     * @throws KeeperException      KeeperException
     * @throws InterruptedException InterruptedException
     */
    @Then("^A kafka topic named '(.+?)' exists")
    public void kafkaTopicExist(String topic_name) throws KeeperException, InterruptedException {
        List<String> topics = this.commonspec.getKafkaUtils().listTopics();
        assertThat(topics.contains(topic_name)).as("There is no topic with that name").isTrue();
    }


    /**
     * Initializes the remote URL of the schema registry service for all future requests. Also sets the property
     * schema.registry.url in the consumer and producer properties
     *
     * @param host Remote host and port (defaults to http://0.0.0.0:8081)
     * @throws Throwable Throwable
     */
    @Given("^My schema registry is running at '(.+)'$")
    public void mySchemaRegistryIsRunningAtLocalhost(String host) throws Throwable {
        commonspec.getKafkaUtils().setSchemaRegistryUrl("http://" + host);
        commonspec.getKafkaUtils().modifyProducerProperties("schema.registry.url", "http://" + host);
    }

    /**
     * Generates a POST to the schema register to add a new schema for the given subject
     *
     * @param subjectName Name of the subject where register the new schema
     * @param filepath    Path of the file containing the schema
     * @throws Throwable Throwable
     */
    @Then("^I register a new version of a schema under the subject '(.+)' with '(.+)'$")
    public void iRegisterANewVersionOfASchemaUnderTheSubject(String subjectName, String filepath) throws Throwable {

        String retrievedData = commonspec.retrieveData(filepath, "json");
        Response response = commonspec.getKafkaUtils().registerNewSchema(subjectName, retrievedData);
        assertThat(response.code()).as("Schema registry returned " + response.code() + " response, body: " + response.body().string()).isEqualTo(200);

    }

    /**
     * Reads messages from the beginning of the topic with the specified properties for the consumer. The message is casted to the
     * correct type based on the given value.deserializer property (uses String deserializer by default)
     *
     * @param topicName Name of the topic where to send the message
     * @param message   Message to send (will be converted to the correct type according to the value.deserializer property)
     * @param isKey     the is key
     * @param dataTable Table containing properties for consumer
     * @throws Throwable Throwable
     */
    @Then("^The kafka topic '(.+?)' has a message containing '(.+?)'( as key)? with:$")
    public void theKafkaTopicStringTopicHasAMessageContainingHelloWith(String topicName, String message, String isKey, DataTable dataTable) throws Throwable {

        /*Modify properties of consumer*/
        for (DataTableRow row : dataTable.getGherkinRows()) {
            String key = row.getCells().get(0);
            String value = row.getCells().get(1);
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
     * Creates an Avro record from the specified schema. The record is created as a {@link GenericRecord}
     *
     * @param recordName Name of the Avro generic record
     * @param schemaFile File containing the schema of the message
     * @param foo        parameter required by Gherkin to match optional text
     * @param seedFile   the seed file
     * @param table      Table containen the values for the fields on the schema. (Values will be converted according to field type)
     * @throws Throwable Throwable
     */
    @Then("^I create the avro record '(.+?)' from the schema in '(.+?)'( based on '(.+?)')? with:$")
    public void iCreateTheAvroRecordRecord(String recordName, String schemaFile, String foo, String seedFile, DataTable table) throws Throwable {

        String schemaString = commonspec.retrieveData(schemaFile, "json");
        this.createRecord(recordName, schemaString, seedFile, table);

    }

    /**
     * Creates a new Avro record by reading the schema directly from the schema registry for the specified subject and version
     *
     * @param recordName    Name of the record
     * @param versionNumber Verison number of the schema
     * @param subject       Subject name
     * @param foo           parameter required by Gherkin to match optional text
     * @param seedFile      Seed file to use
     * @param table         Modifications datatable
     * @throws Throwable Throwable
     */
    @Then("^I create the avro record '(.+?)' using version '(.+?)' of subject '(.+?)' from registry( based on '(.+?)')? with:$")
    public void iCreateTheAvroRecordRecordUsingVersionOfSubjectRecordFromRegistryWith(String recordName, String versionNumber, String subject, String foo, String seedFile, DataTable table) throws Throwable {

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
            for (DataTableRow row : table.getGherkinRows()) {
                properties.put(row.getCells().get(0), row.getCells().get(1));
            }

            commonspec.getKafkaUtils().createGenericRecord(recordName, properties, schemaString);
        }
    }

    /**
     * Send the previously created Avro record to the given topic. The value.serializer property for the producer is set to KafkaAvroSerializer
     * automatically
     *
     * @param genericRecord Name of the record to send
     * @param topicName     Topic where to send the record
     * @param foo           parameter required by Gherkin to match optional text
     * @param recordKey     Record key
     * @param table         Table containing modifications for the producer properties
     * @throws Throwable Throwable
     */
    @When("^I send the avro record '(.+?)' to the kafka topic '(.+?)'( with key '(.+?)')? with:$")
    public void iSendTheAvroRecordRecordToTheKafkaTopic(String genericRecord, String topicName, String foo, String recordKey, DataTable table) throws Throwable {

        /*Modify properties of producer*/
        for (DataTableRow row : table.getGherkinRows()) {
            String key = row.getCells().get(0);
            String value = row.getCells().get(1);
            commonspec.getKafkaUtils().modifyProducerProperties(key, value);
        }

        commonspec.getKafkaUtils().modifyProducerProperties("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");

        GenericRecord record = commonspec.getKafkaUtils().getAvroRecords().get(genericRecord);
        assertThat(record).as("No generic record found with name " + genericRecord).isNotNull();
        commonspec.getKafkaUtils().sendAndConfirmMessage(genericRecord, recordKey, topicName, 1);

    }

    /**
     * Reads the specified topic from beginning for the specified avro record. The consumer value.deserializer property is automatically
     * set to KafkaAvroDeserializer
     *
     * @param topicName  Topic to read from
     * @param avroRecord Name of the record to read
     * @param dataTable  Table containing modifications for the consumer properties
     * @throws Throwable Throwable
     */
    @Then("^The kafka topic '(.+?)' has an avro message '(.+?)' with:$")
    public void theKafkaTopicAvroTopicHasAnAvroMessageRecordWith(String topicName, String avroRecord, DataTable dataTable) throws Throwable {

        /*Modify properties of consumer*/
        for (DataTableRow row : dataTable.getGherkinRows()) {
            String key = row.getCells().get(0);
            String value = row.getCells().get(1);
            commonspec.getKafkaUtils().modifyConsumerProperties(key, value);
        }

        commonspec.getKafkaUtils().modifyConsumerProperties("value.deserializer", "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        assertThat(this.getCommonSpec().getKafkaUtils().getSchemaRegistryUrl()).as("Could not build avro consumer since no schema registry was defined").isNotNull();
        commonspec.getKafkaUtils().modifyConsumerProperties("schema.registry.url", this.getCommonSpec().getKafkaUtils().getSchemaRegistryUrl());


        this.kafkaTopicExist(topicName);
        Map<Object, Object> results = commonspec.getKafkaUtils().readTopicFromBeginning(topicName);
        this.commonspec.getLogger().debug("Found " + results.size() + "records in topic " + topicName);
        assertThat(results.containsValue(commonspec.getKafkaUtils().getAvroRecords().get(avroRecord))).as("Topic does not contain message that matches the specified record").isTrue();

    }

    /**
     * A single step for modifying the consumer properties for the rest of the scenario.
     *
     * @param dataTable table with consumer properties
     */
    @Then("^I configure the kafka consumers with:$")
    public void iConfigureConsumerProperties(DataTable dataTable) {

        for (DataTableRow row : dataTable.getGherkinRows()) {
            String key = row.getCells().get(0);
            String value = row.getCells().get(1);
            this.getCommonSpec().getLogger().debug("Setting kafka consumer property: " + key + " -> " + value);
            commonspec.getKafkaUtils().modifyConsumerProperties(key, value);
        }
    }


    /**
     * A single step for modifying the producer properties for the rest of the scenario.
     *
     * @param dataTable table with consumer properties
     */
    @Then("^I configure the kafka producer with:$")
    public void iConfigureProducerProperties(DataTable dataTable) {

        for (DataTableRow row : dataTable.getGherkinRows()) {
            String key = row.getCells().get(0);
            String value = row.getCells().get(1);
            this.getCommonSpec().getLogger().debug("Setting kafka producer property: " + key + " -> " + value);
            commonspec.getKafkaUtils().modifyProducerProperties(key, value);
        }
    }

    /**
     * Close the connection to kafka.
     *
     * @throws Throwable the throwable
     */
    @Then("^I close the connection to kafka$")
    public void iCloseTheConnectionToKafka() throws Throwable {

        this.getCommonSpec().getLogger().debug("Closing connection to kafka..");
        if (this.getCommonSpec().getKafkaUtils().getZkUtils() != null) {
            this.getCommonSpec().getKafkaUtils().getZkUtils().close();
        }

    }

    /**
     * Performs a partial property matching on the avro records returned
     *
     * @param topicName     Name of the topic to read messages from
     * @param atLeast       Indicates to find at least the expectedCount. If ignored, asserts the exact quantity is found
     * @param expectedCount Expected amount of records to find that match the given conditions
     * @param datatable     Expected conditions
     * @throws Throwable    the throwable
     */
    @And("^The kafka topic '(.+?)' has( at least)? '(.+?)' an avro message with:$")
    public void theKafkaTopicHasAnAvroMessageWith(String topicName, String atLeast, int expectedCount, DataTable datatable) throws Throwable {

        commonspec.getKafkaUtils().modifyConsumerProperties("value.deserializer", "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        assertThat(this.getCommonSpec().getKafkaUtils().getSchemaRegistryUrl()).as("Could not build avro consumer since no schema registry was defined").isNotNull();
        commonspec.getKafkaUtils().modifyConsumerProperties("schema.registry.url", this.getCommonSpec().getKafkaUtils().getSchemaRegistryUrl());
        this.kafkaTopicExist(topicName);

        Map<Object, Object> results = commonspec.getKafkaUtils().readTopicFromBeginning(topicName);

        int matches = results.size();
        for (Object result: results.values()) {

            if (result instanceof GenericRecord) {
                GenericRecord avroMessage = (GenericRecord) result;

                String jsonString = avroMessage.toString();

                for (DataTableRow row : datatable.getGherkinRows()) {
                    String expression = row.getCells().get(0);
                    String condition = row.getCells().get(1);
                    String expectedResult = row.getCells().get(2);

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