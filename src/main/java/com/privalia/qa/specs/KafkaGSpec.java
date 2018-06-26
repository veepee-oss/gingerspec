package com.privalia.qa.specs;

import com.sun.org.apache.regexp.internal.recompile;
import cucumber.api.DataTable;
import cucumber.api.PendingException;
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
 * @author José Fernández
 */
public class KafkaGSpec extends BaseGSpec {

    public KafkaGSpec(CommonG spec) {
        this.commonspec = spec;
    }


    /**
     * Connect to Kafka.
     *
     * @param zkHost ZK host
     * @param zkPath ZK port
     * @throws UnknownHostException exception
     */
    @Given("^I connect to kafka at '(.+)'( using path '(.+)')?$")
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
     */
    @When("^I delete a Kafka topic named '(.+?)'")
    public void deleteKafkaTopic(String topic_name) throws Exception {
        commonspec.getKafkaUtils().deleteTopic(topic_name);
    }

    /**
     * Modify partitions in a Kafka topic by increasing the current number of partitions in the topic by the specified
     * number. Mind that the number of partitions for a topic can only be increased once its created
     *
     * @param topic_name    topic name
     * @param numPartitions number of partitions to add to the current amount of partitions for the topic
     */
    @When("^I increase '(.+?)' partitions in a Kafka topic named '(.+?)'")
    public void modifyPartitions(int numPartitions, String topic_name) throws Exception {
        int currentPartitions = commonspec.getKafkaUtils().getPartitions(topic_name);
        commonspec.getKafkaUtils().modifyTopicPartitioning(topic_name, currentPartitions + numPartitions);
        assertThat(commonspec.getKafkaUtils().getPartitions(topic_name)).as("Number of partitions is not the expected after operation").isEqualTo(currentPartitions + numPartitions);
    }

    /**
     * Sending a message in a Kafka topic.
     *
     * @param topic_name topic name
     * @param message    string that you send to topic
     */
    @When("^I send a message '(.+?)' to the kafka topic named '(.+?)'$")
    public void sendAMessage(String message, String topic_name) throws Exception {
        commonspec.getKafkaUtils().sendAndConfirmMessage(message, topic_name, 1);
    }

    /**
     * Modify properties of producer before sending
     * @param message
     * @param topic_name
     * @param table
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    @Given("I send a message '(.+?)' to the kafka topic named '(.+?)' with:$")
    public void sendAMessageWithDatatable(String message, String topic_name, DataTable table) throws InterruptedException, ExecutionException, TimeoutException {

        /*Modify properties of producer*/
        for (DataTableRow row : table.getGherkinRows()) {
            String key = row.getCells().get(0);
            String value = row.getCells().get(1);
            commonspec.getKafkaUtils().modifyProducerProperties(key, value);
        }

        commonspec.getKafkaUtils().sendAndConfirmMessage(message, topic_name, 1);

    }

    /**
     * Check that a kafka topic not exist
     *
     * @param topic_name name of topic
     */
    @Then("^A kafka topic named '(.+?)' does not exist")
    public void kafkaTopicNotExist(String topic_name) throws KeeperException, InterruptedException {
        assert !commonspec.getKafkaUtils().getZkUtils().pathExists("/" + topic_name) : "There is a topic with that name";
    }

    /**
     * Check that the number of partitions is the expected.
     *
     * @param topic_name      Name of kafka topic
     * @param numOfPartitions Number of partitions
     * @throws Exception
     */
    @Then("^The number of partitions in topic '(.+?)' should be '(.+?)''?$")
    public void checkNumberOfPartitions(String topic_name, int numOfPartitions) throws Exception {
        assertThat(commonspec.getKafkaUtils().getPartitions(topic_name)).isEqualTo(numOfPartitions);

    }

    /**
     * Pools the given topic for messages and checks in the given content is contained
     * @param topic     Topic to poll
     * @param content   Message to look for
     * @throws InterruptedException
     */
    @Then("^The kafka topic '(.*?)' has a message containing '(.*?)'$")
    public void checkMessages(String topic, String content) throws InterruptedException {
        assertThat(commonspec.getKafkaUtils().readTopicFromBeginning(topic).contains(content)).as("Topic does not exist or the content does not match").isTrue();
    }

    /**
     * Check that a kafka topic exist
     *
     * @param topic_name name of topic
     */
    @Then("^A kafka topic named '(.+?)' exists")
    public void kafkaTopicExist(String topic_name) throws KeeperException, InterruptedException {
        List<String> topics = this.commonspec.getKafkaUtils().listTopics();
        assertThat(topics.contains(topic_name)).as("There is no topic with that name").isTrue();
    }


    /**
     * Initializes the remote URL of the schema registry service for all future requests
     * @param host          Remote host and port (defaults to http://0.0.0.0:8081)
     * @throws Throwable
     */
    @Given("^My schema registry is running at '(.+)'$")
    public void mySchemaRegistryIsRunningAtLocalhost(String host) throws Throwable {
        commonspec.getKafkaUtils().setSchemaRegistryUrl(host);
        commonspec.getKafkaUtils().modifyProducerProperties("schema.registry.url", host);
        commonspec.getKafkaUtils().modifyConsumerProperties("schema.registry.url", host);
    }

    /**
     * Generates a POST to the schema register to add a new schema for the given subject
     * @param subjectName   Name of the subject where register the new schema
     * @param filepath      Path of the file containing the schema
     */
    @Then("^I register a new version of a schema under the subject '(.+)' with '(.+)'$")
    public void iRegisterANewVersionOfASchemaUnderTheSubject(String subjectName, String filepath) throws Throwable {

        String retrievedData = commonspec.retrieveData(filepath, "json");
        Response response = commonspec.getKafkaUtils().registerNewSchema(subjectName, retrievedData);
        assertThat(response.code()).as("Schema registry returned " + response.code() + " response, body: " + response.body().string()).isEqualTo(200);

    }

    @Then("^The kafka topic '(.+?)' has a message containing '(.+?)' with:$")
    public void theKafkaTopicStringTopicHasAMessageContainingHelloWith(String topicName, String message, DataTable dataTable) throws Throwable {

        /*Modify properties of consumer*/
        for (DataTableRow row : dataTable.getGherkinRows()) {
            String key = row.getCells().get(0);
            String value = row.getCells().get(1);
            commonspec.getKafkaUtils().modifyConsumerProperties(key, value);
        }

        String deserializer = commonspec.getKafkaUtils().getPropsConsumer().getProperty("value.deserializer");
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


        List<Object> results = commonspec.getKafkaUtils().readTopicFromBeginning(topicName);

        assertThat(results.contains(finalMessage)).as("Topic does not exist or the content does not match").isTrue();
    }

    @Then("^I create the avro record '(.+?)' from the schema in '(.+?)' with:$")
    public void iCreateTheAvroRecordRecord(String recordName, String schemaFile, DataTable table) throws Throwable {

        String retrievedData = commonspec.retrieveData(schemaFile, "json");

        Map<String, String> properties = new HashMap<>();
        for (DataTableRow row : table.getGherkinRows()) {
            properties.put(row.getCells().get(0), row.getCells().get(1));
        }

        commonspec.getKafkaUtils().createGenericRecord(recordName, properties, retrievedData);

    }


    @When("^I send the avro record '(.+?)' to the kafka topic '(.+?)' with:$")
    public void iSendTheAvroRecordRecordToTheKafkaTopic(String genericRecord, String topicName, DataTable table) throws Throwable {

        /*Modify properties of producer*/
        for (DataTableRow row : table.getGherkinRows()) {
            String key = row.getCells().get(0);
            String value = row.getCells().get(1);
            commonspec.getKafkaUtils().modifyProducerProperties(key, value);
        }

        commonspec.getKafkaUtils().modifyProducerProperties("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");

        GenericRecord record = commonspec.getKafkaUtils().getAvroRecords().get(genericRecord);
        assertThat(record).as("No generic record found with name " + genericRecord).isNotNull();
        commonspec.getKafkaUtils().sendAndConfirmMessage(genericRecord, topicName, 1);

    }

    @Then("^The kafka topic '(.+?)' has an avro message '(.+?)' with:$")
    public void theKafkaTopicAvroTopicHasAnAvroMessageRecordWith(String topicName, String avroRecord, DataTable dataTable) throws Throwable {

        /*Modify properties of consumer*/
        for (DataTableRow row : dataTable.getGherkinRows()) {
            String key = row.getCells().get(0);
            String value = row.getCells().get(1);
            commonspec.getKafkaUtils().modifyConsumerProperties(key, value);
        }

        commonspec.getKafkaUtils().modifyConsumerProperties("value.deserializer", "io.confluent.kafka.serializers.KafkaAvroDeserializer");

        List<Object> results = commonspec.getKafkaUtils().readTopicFromBeginning(topicName);
        assertThat(results.contains(commonspec.getKafkaUtils().getAvroRecords().get(avroRecord))).as("Topic does not exist or the content does not match").isTrue();

    }
}
