package com.stratio.tests.utils;

public final class MongoDBUtil {

    private static MongoDBUtil instance = new MongoDBUtil();
    private final MongoDBUtils cUtils = new MongoDBUtils();

    private MongoDBUtil() {
    }

    public static MongoDBUtil getInstance() {
        return instance;
    }

    public MongoDBUtils getMongoDBUtils() {
        return cUtils;
    }

}
