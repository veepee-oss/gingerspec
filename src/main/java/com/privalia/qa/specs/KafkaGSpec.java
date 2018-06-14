package com.privalia.qa.specs;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.zookeeper.KeeperException;

import java.net.UnknownHostException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Class for all Kafka-related cucumber steps
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
     * Modify partitions in a Kafka topic.
     *
     * @param topic_name    topic name
     * @param numPartitions number of partitions
     */
    @When("^I increase '(.+?)' partitions in a Kafka topic named '(.+?)'")
    public void modifyPartitions(int numPartitions, String topic_name) throws Exception {
        commonspec.getKafkaUtils().modifyTopicPartitioning(topic_name, numPartitions);
    }

    /**
     * Sending a message in a Kafka topic.
     *
     * @param topic_name topic name
     * @param message    string that you send to topic
     */
    @When("^I send a message '(.+?)' to the kafka topic named '(.+?)'")
    public void sendAMessage(String message, String topic_name) throws Exception {
        //commonspec.getKafkaUtils().sendMessage(message, topic_name);
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
     * Check that the number of partitions is like expected.
     *
     * @param topic_name      Name of kafka topic
     * @param numOfPartitions Number of partitions
     * @throws Exception
     */
    @Then("^The number of partitions in topic '(.+?)' should be '(.+?)''?$")
    public void checkNumberOfPartitions(String topic_name, int numOfPartitions) throws Exception {
        assertThat(commonspec.getKafkaUtils().getPartitions(topic_name)).isEqualTo(numOfPartitions);

    }

    @Then("^The kafka topic '(.*?)' has a message containing '(.*?)'$")
    public void checkMessages(String topic, String content) {
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
        //assert commonspec.getKafkaUtils().getZkUtils().pathExists("/" + topic_name) : "There is no topic with that name";
    }


}
