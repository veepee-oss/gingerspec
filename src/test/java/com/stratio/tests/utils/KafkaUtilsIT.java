
package com.stratio.tests.utils;

import kafka.admin.AdminUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

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