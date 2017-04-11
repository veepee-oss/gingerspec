/*
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.qa.utils;

import com.google.common.base.Joiner;
import kafka.admin.AdminUtils;
import kafka.common.KafkaException;
import kafka.utils.ZkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

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
    public void createTopicTest() {
        if (AdminUtils.topicExists(kafka_utils.getZkUtils(), "testTopic")) {
            kafka_utils.deleteTopic("testTopic");
        }
        kafka_utils.createTopic("testTopic");
        assertThat(AdminUtils.topicExists(kafka_utils.getZkUtils(), "testTopic")).isTrue();
        kafka_utils.deleteTopic("testTopic");
    }

    @Test
    public void listTopicsTest() {
        if (AdminUtils.topicExists(kafka_utils.getZkUtils(), "testList")) {
            kafka_utils.deleteTopic("testList");
        }
        kafka_utils.createTopic("testList");
        kafka_utils.createTopic("testList2");
        assertThat(kafka_utils.listTopics()).contains("testList");
        logger.debug("Kafka contains next topics: " + Joiner.on(",").join(kafka_utils.listTopics()));
        assertThat(kafka_utils.listTopics()).contains("testList2");
        kafka_utils.deleteTopic("testList");
        kafka_utils.deleteTopic("testList2");
    }

    @Test
    public void writeAndReadKafkaTest() throws InterruptedException, ExecutionException, TimeoutException {
        String topic = "kafkaTest";
        String oneMessage = "Opening message";
        String anotherMessage = "This is a test";

        if (!AdminUtils.topicExists(kafka_utils.getZkUtils(), topic)) {
            kafka_utils.createTopic(topic);
        }
        kafka_utils.sendAndConfirmMessage(oneMessage, topic, 1);
        kafka_utils.sendAndConfirmMessage(anotherMessage, topic, 1);
        assertThat(kafka_utils.readTopicFromBeginning(topic).contains("This is a test")).as("Topic {} contains {}", topic, anotherMessage);
        kafka_utils.deleteTopic(topic);
    }

    @Test
    public void modifyPartitionsTest() {
        if (AdminUtils.topicExists(kafka_utils.getZkUtils(), "testPartitions")) {
            kafka_utils.deleteTopic("testPartitions");
        }
        kafka_utils.createTopic("testPartitions");
        assertThat(kafka_utils.getPartitions("testPartitions")).isEqualTo(1);
        kafka_utils.modifyTopicPartitioning("testPartitions", 2);
        assertThat(kafka_utils.getPartitions("testPartitions")).isEqualTo(2);
        kafka_utils.deleteTopic("testPartitions");
    }

    @Test
    public void modifyPartitionsNotKnownTopicTest() {
        if (AdminUtils.topicExists(kafka_utils.getZkUtils(), "testPartitions2")) {
            kafka_utils.deleteTopic("testPartitions2");
        }
        kafka_utils.modifyTopicPartitioning("testPartitions2", 2);
    }

    @Test
    public void setZkHostTest() {
        ZkUtils zkOpts = kafka_utils.getZkUtils();
        kafka_utils.setZkHost(zkOpts.zkConnection().getServers(),"2181","/");
    }

    @Test
    public void sendMessageTopicTest() {
        kafka_utils.createTopic("testMessage");
        kafka_utils.sendMessage("hello, its me", "testMessage");
        assertThat(kafka_utils.readTopicFromBeginning("testMessage")).contains("hello, its me");
    }
}
