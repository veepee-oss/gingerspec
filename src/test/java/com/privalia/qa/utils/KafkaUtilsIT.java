/*
 * Copyright (c) 2021, Veepee
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose
 * with or without fee is hereby  granted, provided that the above copyright notice
 * and this permission notice appear in all copies.
 *
 * THE SOFTWARE  IS PROVIDED "AS IS"  AND THE AUTHOR DISCLAIMS  ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING  ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS.  IN NO  EVENT  SHALL THE  AUTHOR  BE LIABLE  FOR  ANY SPECIAL,  DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 * OF USE, DATA  OR PROFITS, WHETHER IN AN ACTION OF  CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR  IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package com.privalia.qa.utils;

import com.google.common.base.Joiner;
import kafka.admin.AdminUtils;
import kafka.utils.ZkUtils;
import okhttp3.Response;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaUtilsIT {
    private final Logger logger = LoggerFactory
            .getLogger(KafkaUtilsIT.class);

    private KafkaUtils kafka_utils;

    @BeforeMethod(enabled = false)
    public void setSettingsTest() {
        kafka_utils = new KafkaUtils();
        kafka_utils.setSchemaRegistryUrl("http://localhost:8081");
        kafka_utils.connect();
    }

    @Test(enabled = false)
    public void createTopicTest() {
        if (AdminUtils.topicExists(kafka_utils.getZkUtils(), "testTopic")) {
            kafka_utils.deleteTopic("testTopic");
        }
        kafka_utils.createTopic("testTopic");
        assertThat(AdminUtils.topicExists(kafka_utils.getZkUtils(), "testTopic")).isTrue();
        kafka_utils.deleteTopic("testTopic");
    }

    @Test(enabled = false)
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

    @Test(enabled = false)
    public void writeAndReadKafkaTest() throws InterruptedException, ExecutionException, TimeoutException {
        String topic = "kafkaTest";
        String oneMessage = "Opening message";
        String anotherMessage = "This is a test";

        if (!AdminUtils.topicExists(kafka_utils.getZkUtils(), topic)) {
            kafka_utils.createTopic(topic);
        }
        kafka_utils.sendAndConfirmMessage(oneMessage, null, topic, 1);
        kafka_utils.sendAndConfirmMessage(anotherMessage, null, topic, 1);
        Map<Object, Object> messages = kafka_utils.readTopicFromBeginning(topic);
        assertThat(messages.containsValue("This is a test")).isTrue();
        kafka_utils.deleteTopic(topic);
    }

    @Test(enabled = false)
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

    @Test(enabled = false)
    public void modifyPartitionsNotKnownTopicTest() {
        if (AdminUtils.topicExists(kafka_utils.getZkUtils(), "testPartitions2")) {
            kafka_utils.deleteTopic("testPartitions2");
        }
        kafka_utils.modifyTopicPartitioning("testPartitions2", 2);
    }

    @Test(enabled = false)
    public void setZkHostTest() {
        ZkUtils zkOpts = kafka_utils.getZkUtils();
        kafka_utils.setZkHost(zkOpts.zkConnection().getServers(),"2181","/");
    }

    @Test(enabled = false)
    public void addNewSchemaTest() throws IOException {

        Response  response = kafka_utils.registerNewSchema("Kafka-key", "{\"type\": \"string\"}");
        assertThat(response.code()).as("Schema registry returned " + response.code() + " response, body: " + response.body().string()).isEqualTo(200);
    }

    @Test(enabled = false)
    public void sendAvroRecordTest() throws IOException, InterruptedException, ExecutionException, TimeoutException {

        String schema = "{\"type\":\"record\",\"name\":\"Record\",\"namespace\":\"com.mynamespace\",\"fields\":[{\"name\":\"str1\",\"type\":\"string\"},{\"name\":\"str2\",\"type\":\"string\"},{\"name\":\"int1\",\"type\":\"int\"}]}";

        Response  response = kafka_utils.registerNewSchema("Kafka-key", schema);
        assertThat(response.code()).as("Schema registry returned " + response.code() + " response, body: " + response.body().string()).isEqualTo(200);

        Schema.Parser parser = new Schema.Parser();
        Schema s = parser.parse(schema);
        GenericRecord avroRecord = new GenericData.Record(s);
        avroRecord.put("str1","str1");
        avroRecord.put("str2","str2");
        avroRecord.put("int1",1);

        if (AdminUtils.topicExists(kafka_utils.getZkUtils(), "avroTopic")) {
            kafka_utils.deleteTopic("avroTopic");
        }
        kafka_utils.createTopic("avroTopic");
        kafka_utils.getAvroRecords().put("record", avroRecord);

        kafka_utils.modifyProducerProperties("schema.registry.url", "http://localhost:8081");
        kafka_utils.modifyProducerProperties("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafka_utils.modifyProducerProperties("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
        kafka_utils.sendAndConfirmMessage("record", null,"avroTopic", 1);

    }
}