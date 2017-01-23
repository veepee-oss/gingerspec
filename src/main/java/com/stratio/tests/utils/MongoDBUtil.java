package com.stratio.tests.utils;
/**
 * Singelton class of MongoDBUtils.
 *
 */
public enum MongoDBUtil {
    INSTANCE;

    private final MongoDBUtils cUtils = new MongoDBUtils();

    public MongoDBUtils getMongoDBUtils() {
        return cUtils;
    }

}