package com.stratio.tests.utils;

public enum KafkaUtil {
    INSTANCE;

    private final KafkaUtils cUtils = new KafkaUtils();

    public KafkaUtils getKafkaUtils() {
        return cUtils;
    }

}