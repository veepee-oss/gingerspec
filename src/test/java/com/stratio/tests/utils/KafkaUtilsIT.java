
package com.stratio.tests.utils;

import com.google.common.base.Joiner;
import kafka.admin.AdminUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class KafkaUtilsIT {
    private final Logger logger = LoggerFactory
            .getLogger(KafkaUtilsIT.class);

    private KafkaUtils kafka_utils;

    @BeforeMethod
    public void setSettingsTest() {
        kafka_utils = new KafkaUtils();
        kafka_utils.connect();
    }

    @Test
    public void createTopicTest()  {
        if(AdminUtils.topicExists(kafka_utils.getZkUtils(),"testTopic")){
            kafka_utils.deleteTopic("testTopic");
        }
        kafka_utils.createTopic("testTopic");
        assertThat(AdminUtils.topicExists(kafka_utils.getZkUtils(),"testTopic")).isTrue();
        kafka_utils.deleteTopic("testTopic");
    }

    @Test
    public void listTopicsTest()  {
        if(AdminUtils.topicExists(kafka_utils.getZkUtils(),"testList")){
            kafka_utils.deleteTopic("testList");
        }
        kafka_utils.createTopic("testList");
        kafka_utils.createTopic("testList2");
        assertThat(kafka_utils.listTopics()).contains("testList");
        logger.debug("Kafka contains next topics: " +Joiner.on(",").join(kafka_utils.listTopics()));
        assertThat(kafka_utils.listTopics()).contains("testList2");
        kafka_utils.deleteTopic("testList");
        kafka_utils.deleteTopic("testList2");
    }

    @Test
    public void writeAndReadKafkaTest() throws InterruptedException, ExecutionException, TimeoutException{
        String topic = "kafkaTest";
        String oneMessage = "Opening message";
        String anotherMessage = "This is a test";

        if (!AdminUtils.topicExists(kafka_utils.getZkUtils(), topic)){
            kafka_utils.createTopic(topic);
        }
        kafka_utils.sendAndConfirmMessage(oneMessage, topic, 1);
        kafka_utils.sendAndConfirmMessage(anotherMessage, topic, 1);
        assertThat(kafka_utils.readTopicFromBeginning(topic).contains("This is a test")).as("Topic {} contains {}", topic, anotherMessage);
        kafka_utils.deleteTopic(topic);
    }

    @Test
    public void modifyPartitionsTest()  {
        if(AdminUtils.topicExists(kafka_utils.getZkUtils(),"testPartitions")){
            kafka_utils.deleteTopic("testPartitions");
        }
        kafka_utils.createTopic("testPartitions");
        assertThat(kafka_utils.getPartitions("testPartitions")).isEqualTo(1);
        kafka_utils.modifyTopicPartitioning("testPartitions",2);
        assertThat(kafka_utils.getPartitions("testPartitions")).isEqualTo(2);
        kafka_utils.deleteTopic("testPartitions");
    }

}
