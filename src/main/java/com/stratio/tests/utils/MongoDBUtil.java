package com.stratio.tests.utils;

public enum MongoDBUtil {
    INSTANCE;

    private final MongoDBUtils cUtils = new MongoDBUtils();

    public MongoDBUtils getMongoDBUtils() {
        return cUtils;
    }

}