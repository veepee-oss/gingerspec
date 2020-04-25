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

package com.privalia.qa.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import kafka.admin.AdminOperationException;
import kafka.admin.AdminUtils;
import kafka.admin.BrokerMetadata;
import kafka.admin.RackAwareMode;
import kafka.common.KafkaException;
import kafka.common.TopicAlreadyMarkedForDeletionException;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import okhttp3.*;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.collections.map.HashedMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.collection.JavaConversions;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    private String schemaRegistryConnect;

    public Properties getPropsConsumer() {
        return propsConsumer;
    }

    public Map<String, GenericRecord> avroRecords = new HashedMap();

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
        this.schemaRegistryConnect = System.getProperty("SCHEMA_REGISTRY_HOST", "http://localhost:8081");
        this.rackAwareMode = RackAwareMode.Enforced$.MODULE$;
        this.topicConfig = new Properties();
        this.props = new Properties();
        props.put("bootstrap.servers", System.getProperty("KAFKA_HOSTS", "0.0.0.0:9092"));
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaQAProducer");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        this.propsConsumer = new Properties();
        propsConsumer.put("bootstrap.servers", System.getProperty("KAFKA_HOSTS", "0.0.0.0:9092"));
        propsConsumer.put("group.id", "QAConsumerGroup");
        propsConsumer.put("enable.auto.commit", "true");
        propsConsumer.put("auto.offset.reset", "earliest");
        propsConsumer.put("auto.commit.interval.ms", "1000");
        propsConsumer.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        propsConsumer.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        propsConsumer.put("session.timeout.ms", "10000");
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
        this.zkUtils = new ZkUtils(zkClient, new ZkConnection(zookeeperConnect), isSecureKafkaCluster);
    }

    public void setZkHost(String host, String port, String zkPath) {
        if ((zkPath != null) && (!(zkPath.matches("")))  && (!(zkPath.matches("null")))) {
            this.zookeeperConnect = host + ":" + port + "/" + zkPath;
        } else {
            this.zookeeperConnect = host + ":" + port;
        }
    }

    public Map<String, GenericRecord> getAvroRecords() {
        return avroRecords;
    }

    public ZkUtils getZkUtils() {
        return zkUtils;
    }

    /**
     * Returns the number of partitions for the given topic
     *
     * @param topicName Name of the topic
     * @return Number of partitions for the topic
     */
    public int getPartitions(String topicName) {
        List<?> partitions = JavaConversions.seqAsJavaList(
                zkUtils.getPartitionsForTopics(
                        JavaConversions.asScalaBuffer(Collections.singletonList(topicName))).head()._2());
        return partitions.size();

    }


    /**
     * Create a Kafka topic.
     *
     * @param topicName name of topic.
     * @return true if the topic has been created and false if the topic has not been created.
     * @throws KafkaException the kafka exception
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
     * @throws KafkaException                         the kafka exception
     * @throws TopicAlreadyMarkedForDeletionException the topic already marked for deletion exception
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
     * @throws KafkaException the kafka exception
     */
    public List<String> listTopics() throws KafkaException {
        return scala.collection.JavaConversions.seqAsJavaList(zkUtils.getAllTopics());
    }


    /**
     * Modify number of partition of a Kafka topic.
     *
     * @param topicName     name of topic.
     * @param numPartitions the num partitions
     * @throws KafkaException the kafka exception
     */
    public void modifyTopicPartitioning(String topicName, int numPartitions) throws KafkaException {
        if (AdminUtils.topicExists(zkUtils, topicName)) {
            logger.debug("Altering topic {}", topicName);
            try {

                Seq<String> names = JavaConverters.asScalaBuffer(Arrays.asList(topicName));
                Seq<BrokerMetadata> brokers = AdminUtils.getBrokerMetadatas(zkUtils, RackAwareMode.Enforced$.MODULE$, Option.empty());
                scala.collection.mutable.Map<String, scala.collection.Map<Object, Seq<Object>>> assignment = (scala.collection.mutable.Map<String, scala.collection.Map<Object, Seq<Object>>>) zkUtils.getPartitionAssignmentForTopics(names);
                Map<String, scala.collection.Map<Object, Seq<Object>>> partitionaAssigmentMap = JavaConverters.mutableMapAsJavaMap(assignment);
                AdminUtils.addPartitions(zkUtils, topicName, partitionaAssigmentMap.get(topicName), brokers, numPartitions, Option.empty(), false);

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
     * @param message   the message
     * @param topicName name of topic.
     */
    @Deprecated
    public void sendMessage(String message, String topicName) {
        Producer<String, String> producer = new KafkaProducer<>(props);
        producer.send(new ProducerRecord<String, String>(topicName, message));
        producer.close();
    }

    /**
     * Send (and confirm) a message. The system automatically infers the type of the key and value based
     * on the producer properties for key and value serializer types
     *
     * @param message        The message to be sent
     * @param key            the key
     * @param topicName      name of topic
     * @param timeoutSeconds Number of seconds to wait for acknowledgement by Kafka
     * @throws InterruptedException InterruptedException
     * @throws ExecutionException   ExecutionException
     * @throws TimeoutException     TimeoutException
     */
    public void sendAndConfirmMessage(String message, String key, String topicName, long timeoutSeconds) throws InterruptedException, ExecutionException, TimeoutException {

        String keySerializer = this.props.getProperty("key.serializer");
        String valueSerializer = this.props.getProperty("value.serializer");
        Class keyClass = this.getProperClass(keySerializer);
        Class valueClass = this.getProperClass(valueSerializer);

        Object finalMessage = null;
        Object finalKey = null;

        //Value can be string, int or avro
        if (valueClass.equals(String.class)) {
            finalMessage = message.toString();
        }
        if (valueClass.equals(Long.class)) {
            finalMessage = Long.parseLong(message);
        }
        if (valueClass.equals(GenericRecord.class)) {
            GenericRecord record = this.getAvroRecords().get(message);
            finalMessage = record;
        }

        //Key can be string or int
        if (key != null) {
            if (keyClass.equals(String.class)) {
                finalKey = key.toString();
            }
            if (keyClass.equals(Long.class)) {
                finalKey = Long.parseLong(key);
            }
        } else {
            finalKey = null;
        }


        this.sendAndConfirmMessage(finalMessage, finalKey, topicName, timeoutSeconds, keyClass, valueClass);

    }

    private <K, V> void sendAndConfirmMessage(Object message, Object key, String topicName, long timeoutSeconds, K keyClass, V valueClass) throws InterruptedException, ExecutionException, TimeoutException {
        Producer<K, V> producer = new KafkaProducer<>(props);
        try {
            long time = System.currentTimeMillis();

            ProducerRecord<K, V> record;
            if (key != null) {
                record = new ProducerRecord(topicName, key, message);
            } else {
                record = new ProducerRecord(topicName, message);
            }

            RecordMetadata metadata = (RecordMetadata) producer.send(record).get(timeoutSeconds, TimeUnit.SECONDS);
            long elapsedTime = System.currentTimeMillis() - time;
            logger.debug("Message sent and acknowlegded by Kafka(key=%s value=%s) meta(partition=%d, offset=%d) time=%d", record.key(), record.value(), metadata.partition(), metadata.offset(), elapsedTime);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Message not sent or acknowlegded by Kafka {}", e.getMessage());
            throw e;
        } finally {
            producer.flush();
            producer.close();
        }
    }


    /**
     * Returns the appropiate class for the given property
     *
     * @param item
     * @return
     */
    private Class getProperClass(String item) {
        switch (item) {

            case "org.apache.kafka.common.serialization.StringSerializer":
                return String.class;

            case "org.apache.kafka.common.serialization.LongSerializer":
                return Long.class;

            case "org.apache.kafka.common.serialization.StringDeserializer":
                return String.class;

            case "org.apache.kafka.common.serialization.LongDeserializer":
                return Long.class;

            case "io.confluent.kafka.serializers.KafkaAvroSerializer":
                return GenericRecord.class;

            case "io.confluent.kafka.serializers.KafkaAvroDeserializer":
                return GenericRecord.class;

            default:
                return String.class;
        }
    }

    /**
     * Fetch messages from the given partition using poll(). Since there is no warranty that the pool will
     * return messages, the function is enclosed in a loop for 5 seconds
     *
     * @param topic Name of the topic from which retrieve messages
     * @return List of messages in the topic
     */
    public Map<Object, Object> readTopicFromBeginning(String topic) {

        String key = this.propsConsumer.getProperty("key.deserializer");
        String value = this.propsConsumer.getProperty("value.deserializer");
        Class keyClass = this.getProperClass(key);
        Class valueClass = this.getProperClass(value);

        return this.readTopicFromBeginning(topic, keyClass, valueClass);

    }

    public <K, V> Map readTopicFromBeginning(String topic, K keyClass, V valueClass) {
        Map<K, V> result = new LinkedHashMap<>();
        KafkaConsumer<K, V> consumer = new KafkaConsumer<>(propsConsumer);
        consumer.subscribe(Arrays.asList(topic));

        try {
            long endTimeMillis = System.currentTimeMillis() + 5000;
            while ((System.currentTimeMillis() < endTimeMillis)) {
                ConsumerRecords<K, V> records = consumer.poll(100);
                for (ConsumerRecord<K, V> record : records) {
                    logger.debug(record.offset() + ": " + record.value());
                    result.put(record.key(), record.value());
                }
                consumer.commitSync();
            }
        } finally {
            consumer.unsubscribe();
            consumer.close();
        }


        logger.debug("Found " + result.size() + " messages in topic " + topic + ". " + result.toString());
        return result;
    }


    /**
     * Set remote schema registry url and port for all future requests
     *
     * @param host host (defaults to http://0.0.0.0:8081)
     */
    public void setSchemaRegistryUrl(String host) {
        logger.debug("Setting schema registry remote url to " + host);
        this.schemaRegistryConnect = host;
    }

    public String getSchemaRegistryUrl() {
        logger.debug("Getting schema registry url");
        return this.schemaRegistryConnect;
    }

    /**
     * Publish a new version of the schema under the given subject
     *
     * @param subject Name of the subject
     * @param schema  Schema object as string
     * @return response object from the schema server
     * @throws IOException IOException
     */
    public Response registerNewSchema(String subject, String schema) throws IOException {
        logger.debug("Registering new version of schema for subject " + subject);

        String jsonEncodedString = "{\"schema\": " + JSONObject.quote(schema) + "}";

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/vnd.schemaregistry.v1+json");
        RequestBody body = RequestBody.create(mediaType, jsonEncodedString);
        Request request = new Request.Builder()
                .url(this.schemaRegistryConnect + "/subjects/" + subject + "/versions")
                .post(body)
                .addHeader("Content-Type", "application/vnd.schemaregistry.v1+json")
                .build();

        return client.newCall(request).execute();

    }

    /**
     * Fetch version of the schema registered under the specified subject in the registry
     *
     * @param subject Subject name
     * @param version Version of the schema to fetch
     * @return Json encoded string of the schema
     * @throws IOException IOException
     */
    public String getSchemaFromRegistry(String subject, String version) throws IOException {
        logger.debug("Fetching schema version " + version + " from subject " + subject);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(this.schemaRegistryConnect + "/subjects/" + subject + "/versions/" + version)
                .get()
                .addHeader("Content-Type", "application/json")
                .build();

        ObjectMapper om = new ObjectMapper();
        ResponseBody response = client.newCall(request).execute().body();
        Map fieldMapped = om.readValue(response.byteStream(), Map.class);
        String schema = (String) fieldMapped.get("schema");
        return schema;

    }

    /**
     * Modify a single property of the producer
     *
     * @param key   Property name
     * @param value Property new value
     */
    public void modifyProducerProperties(String key, String value) {
        this.props.put(key, value);
    }


    /**
     * Modify a single property of the producer
     *
     * @param key   Property name
     * @param value Property new value
     */
    public void modifyConsumerProperties(String key, String value) {
        this.propsConsumer.put(key, value);
    }


    /**
     * given a json representation of the data, creates a generic record with the given schema
     *
     * @param key    Name of the generic record
     * @param json   Json string with data
     * @param schema Schema to be used to serialize the object
     * @throws IOException IOException
     */
    public void createGenericRecord(String key, String json, String schema) throws IOException {

        /*
          This is the official way of creating a generic record using the standard
          library. However, the library requires the fields of type byte to be represented
          in a very special way, which I never figured out what it was. Thats why i decided to
          create my own method for creating a generic record
         */

        /*
        Schema.Parser schemaParser = new Schema.Parser();
        Schema s = schemaParser.parse(schema);
        DecoderFactory decoderFactory = new DecoderFactory();
        Decoder decoder = decoderFactory.jsonDecoder(s, json);
        DatumReader<GenericData.Record> reader =
                new GenericDatumReader<>(s);
        GenericRecord genericRecord = reader.read(null, decoder);
        this.avroRecords.put(key, genericRecord);
        */


        /*
          My way of creating a generic record
         */
        Map<String, String> propertyList = new HashedMap();
        HashMap<String, String> result = new ObjectMapper().readValue(json, HashMap.class);

        for (String item : result.keySet()) {

            try {
                propertyList.put(item, result.get(item));
            } catch (Exception e) {
                propertyList.put(item, new Gson().toJson(result.get(item)));
            }

        }

        this.createGenericRecord(key, propertyList, schema);

    }

    /**
     * Creates a {@link GenericRecord} to be sent through kafka using avro serializers.
     * The message is stored in an internal map using the specified key for later use.
     *
     * @param key          Name of the generic record
     * @param propertyList List of properties and values
     * @param schema       Schema to be used to serialize the object
     */
    public void createGenericRecord(String key, Map<String, String> propertyList, String schema) {

        try {
            this.avroRecords.put(key, this.buildRecord(schema, propertyList));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Creates a {@link GenericRecord} given its schema and the list of key -> value
     *
     * @param schema       Schema as string
     * @param propertyList Property list (Key -> Value)
     * @return {@link GenericRecord}
     * @throws IOException
     */
    private GenericRecord buildRecord(String schema, Map<String, String> propertyList) throws IOException {

        Schema.Parser parser = new Schema.Parser();
        Schema s = parser.parse(schema);
        GenericRecord avroRecord = new GenericData.Record(s);

        //for cases of null records
        if (propertyList == null) {
            return null;
        }

        for (Map.Entry<String, String> entry : propertyList.entrySet()) {

            Schema.Field field = s.getField(entry.getKey());
            if (field != null) {

                String type = field.schema().getType().getName().toLowerCase();
                String value;
                try {
                    value = entry.getValue();
                } catch (Exception e) {
                    value = new Gson().toJson(entry.getValue());
                }


                if (type.matches("int")) {
                    avroRecord.put(entry.getKey(), Integer.valueOf(value));
                } else if (type.matches("bytes")) {
                    avroRecord.put(entry.getKey(), ByteBuffer.wrap(new BigDecimal(value).unscaledValue().toByteArray()));
                } else if (type.matches("long")) {
                    avroRecord.put(entry.getKey(), Long.parseLong(value));
                } else if (type.matches("float")) {
                    avroRecord.put(entry.getKey(), Float.parseFloat(value));
                } else if (type.matches("string")) {
                    avroRecord.put(entry.getKey(), value);
                } else if (type.matches("boolean")) {
                    avroRecord.put(entry.getKey(), (value.matches("true") ? true : false));
                } else if (type.matches("array")) {
                    List<Object> objectArray = new ArrayList<>();
                    objectArray = this.getObjectsfromArray(entry.getKey(), value, s);
                    avroRecord.put(entry.getKey(), objectArray);
                } else if (type.matches("record")) {
                    HashMap<String, String> result = null;
                    try {
                        result = new ObjectMapper().readValue(value, HashMap.class);
                    } catch (IOException e) {
                        throw new IOException("Could not map " + value + " to a record type", e);
                    }
                    GenericRecord temp = this.buildRecord(s.getField(entry.getKey()).schema().toString(), result);
                    avroRecord.put(entry.getKey(), temp);
                } else if (type.matches("union")) {

                    for (Schema sch : field.schema().getTypes()) {

                        String t = sch.getName().toLowerCase();

                        switch (t) {

                            case "null":
                                break;

                            case "array":
                                List<Object> objectArray = new ArrayList<>();
                                objectArray = this.getObjectsfromArray(entry.getKey(), value, sch);
                                avroRecord.put(entry.getKey(), objectArray);
                                break;

                            case "string":
                                avroRecord.put(entry.getKey(), value);
                                break;

                            case "bytes":
                                avroRecord.put(entry.getKey(), ByteBuffer.wrap(new BigDecimal(value).unscaledValue().toByteArray()));
                                break;

                            case "long":
                                avroRecord.put(entry.getKey(), Long.parseLong(value));
                                break;

                            case "float":
                                avroRecord.put(entry.getKey(), Float.parseFloat(value));
                                break;

                            case "int":
                                avroRecord.put(entry.getKey(), Integer.valueOf(value));
                                break;

                            case "boolean":
                                avroRecord.put(entry.getKey(), (value.matches("true") ? true : false));
                                break;


                            default:
                                if (value != null) {
                                    GenericRecord temp = this.buildRecord(sch.toString(), new ObjectMapper().readValue(value, HashMap.class));
                                    avroRecord.put(entry.getKey(), temp);
                                } else {
                                    avroRecord.put(entry.getKey(), null);
                                }

                        }

                    }

                } else {
                    logger.warn("Unrecognized type in schema: " + type);
                    avroRecord.put(entry.getKey(), value);
                }
            } else {
                logger.warn("the field " + entry.getKey() + " is not present in the schema and will be ignored");
            }
        }

        return avroRecord;
    }

    private List<Object> getObjectsfromArray(String entry, String value, Schema s) throws IOException {

        List<Object> objectArray = new ArrayList<>();
        String t = null;

        try {
            t = s.getField(entry).schema().getElementType().getType().getName().toLowerCase();
        } catch (Exception e) {
            t = s.getElementType().getType().getName().toLowerCase();
        }


        if (t.matches("record")) {

            List<HashMap<String, String>> elements = null;
            try {
                elements = new ObjectMapper().readValue(value, List.class);
            } catch (IOException e) {
                throw new IOException("Could not map " + value + " to array", e);
            }
            String elementsSchema = null;
            try {
                elementsSchema = s.getField(entry).schema().getElementType().toString();
            } catch (Exception e) {
                elementsSchema = s.getElementType().toString();
            }

            for (HashMap<String, String> element : elements) {
                objectArray.add(this.buildRecord(elementsSchema, element));
            }
        } else {

            List<String> elements = null;
            for (String element : elements) {

                switch (t) {
                    case "string":
                        objectArray.add(element);
                        break;

                    case "bytes":
                        objectArray.add(ByteBuffer.wrap(new BigDecimal(element).unscaledValue().toByteArray()));
                        break;

                    case "long":
                        objectArray.add(Long.parseLong(element));
                        break;

                    case "boolean":
                        objectArray.add((element.matches("true") ? true : false));
                        break;

                    case "int":
                        objectArray.add(Integer.valueOf(element));
                        break;

                    default:
                        objectArray.add(element);
                }
            }
        }

        return objectArray;

    }

}