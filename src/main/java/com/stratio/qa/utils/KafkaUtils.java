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

import kafka.admin.AdminOperationException;
import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.common.KafkaException;
import kafka.common.TopicAlreadyMarkedForDeletionException;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.requests.MetadataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * Generic utilities for operations over Kafka.
 */

public class KafkaUtils {
    private final Logger logger = LoggerFactory
            .getLogger(KafkaUtils.class);

    private ZkUtils zkUtils;
    private int partitions;
    private int replication;
    private Properties topicConfig;
    private String zookeeperConnect;
    private int sessionTimeoutMs;
    private int connectionTimeoutMs;
    private boolean isSecureKafkaCluster;
    private RackAwareMode rackAwareMode;
    private Properties props;
    private Properties propsConsumer;
    private ZkClient zkClient;

    /**
     * Generic contructor of KafkaUtils.
     */
    public KafkaUtils() {
        this.partitions = Integer.valueOf(System.getProperty("KAFKA_PARTITIONS", "1"));
        this.replication = Integer.valueOf(System.getProperty("KAFKA_REPLICATION", "1"));
        this.sessionTimeoutMs = Integer.valueOf(System.getProperty("KAFKA_SESSION_TIMEOUT", "10000"));
        this.connectionTimeoutMs = Integer.valueOf(System.getProperty("KAFKA_CONNECTION_TIMEOUT", "60000"));
        this.isSecureKafkaCluster = Boolean.valueOf(System.getProperty("KAFKA_SECURED", "false"));
        this.zookeeperConnect = System.getProperty("ZOOKEEPER_HOSTS", "0.0.0.0:2181");
        this.rackAwareMode = RackAwareMode.Enforced$.MODULE$;
        this.topicConfig = new Properties();
        this.props = new Properties();
        props.put("bootstrap.servers", System.getProperty("KAFKA_HOSTS", "0.0.0.0:9092"));
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        this.propsConsumer = new Properties();
        propsConsumer.put("bootstrap.servers", System.getProperty("KAFKA_HOSTS", "0.0.0.0:9092"));
        propsConsumer.put("group.id", "test");
        propsConsumer.put("enable.auto.commit", "true");
        propsConsumer.put("auto.offset.reset", "earliest");
        propsConsumer.put("auto.commit.interval.ms", "1000");
        propsConsumer.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        propsConsumer.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    }

    /**
     * Connect to Kafka.
     */
    public void connect() {
        logger.debug("Connecting to kafka...");
        this.zkClient = new ZkClient(
                zookeeperConnect,
                sessionTimeoutMs,
                connectionTimeoutMs,
                ZKStringSerializer$.MODULE$);
        this.zkUtils = new ZkUtils(zkClient
                , new ZkConnection(zookeeperConnect), isSecureKafkaCluster);
    }

    public void setZkHost(String host, String port, String zkPath) {
        this.zookeeperConnect = host + ":" + port + "/" + zkPath;
    }

    public ZkUtils getZkUtils() {
        return zkUtils;
    }

    public int getPartitions(String topicName) {
        MetadataResponse.TopicMetadata metaData = AdminUtils.fetchTopicMetadataFromZk(topicName, zkUtils);
        return metaData.partitionMetadata().size();
    }


    /**
     * Create a Kafka topic.
     *
     * @param topicName name of topic.
     * @return true if the topic has been created and false if the topic has not been created.
     * @throws kafka.common.KafkaException
     */
    public boolean createTopic(String topicName) throws KafkaException {
        logger.debug("Creating topic with name: " + topicName);
        AdminUtils.createTopic(zkUtils, topicName, partitions, replication, topicConfig, rackAwareMode);
        logger.debug("Topic created correctly with name: " + topicName);
        return AdminUtils.topicExists(zkUtils, topicName);
    }

    /**
     * Delete a Kafka topic.
     *
     * @param topicName name of topic.
     * @return true if the topic has been deleted and false if the topic has not been deleted.
     * @throws kafka.common.KafkaException
     */
    public boolean deleteTopic(String topicName) throws KafkaException, TopicAlreadyMarkedForDeletionException {
        logger.debug("Deleting topic with name: " + topicName);
        AdminUtils.deleteTopic(zkUtils, topicName);
        logger.debug("Topic with name: " + topicName + " correctly deleted");
        return !AdminUtils.topicExists(zkUtils, topicName);
    }

    /**
     * List all Kafka topics.
     *
     * @return list of topics.
     * @throws kafka.common.KafkaException
     */
    public List<String> listTopics() throws KafkaException {
        return scala.collection.JavaConversions.seqAsJavaList(zkUtils.getAllTopics());
    }


    /**
     * Modify number of partition of a Kafka topic.
     *
     * @param topicName     name of topic.
     * @param numPartitions
     * @throws kafka.common.KafkaException
     */
    public void modifyTopicPartitioning(String topicName, int numPartitions) throws KafkaException {
        if (AdminUtils.topicExists(zkUtils, topicName)) {
            logger.debug("Altering topic {}", topicName);
            try {
                AdminUtils.addPartitions(zkUtils, topicName, numPartitions, "", true, RackAwareMode.Enforced$.MODULE$);
                logger.debug("Topic {} altered with partitions : {}", topicName, partitions);
            } catch (AdminOperationException aoe) {
                logger.debug("Error while altering partitions for topic : {}", topicName, aoe);
            }
        } else {
            logger.debug("Topic {} doesn't exists", topicName);
        }
    }

    /**
     * Send a message to a Kafka topic.
     *
     * @param topicName name of topic.
     */
    public void sendMessage(String message, String topicName) {
        Producer<String, String> producer = new KafkaProducer<>(props);
        producer.send(new ProducerRecord<String, String>(topicName, message));
        producer.close();
    }

    /**
     * Send (and confirm) a message
     *
     * @param message        The message to be sent
     * @param topicName      name of topic
     * @param timeoutSeconds Number of seconds to wait for acknowledgement by Kafka
     */
    public void sendAndConfirmMessage(String message, String topicName, long timeoutSeconds) throws InterruptedException, ExecutionException, TimeoutException {
        Producer<String, String> producer = new KafkaProducer<>(props);
        try {
            producer.send(new ProducerRecord<String, String>(topicName, message)).get(timeoutSeconds, TimeUnit.SECONDS);
            logger.debug("Message sent and acknowlegded by Kafka");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Message not sent or acknowlegded by Kafka {}", e.getMessage());
            throw e;
        } finally {
            producer.close();
        }
    }

    public List<String> readTopicFromBeginning(String topic) {
        List<String> result = new ArrayList<>();
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(propsConsumer);
        consumer.subscribe(Arrays.asList(topic));
        ConsumerRecords<String, String> records = consumer.poll(100);
        while (records.isEmpty()) {
            records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                result.add(record.value());
            }
        }
        return result;
    }
}
