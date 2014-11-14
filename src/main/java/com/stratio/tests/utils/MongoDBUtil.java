package com.stratio.tests.utils;
/**
 * Singelton class of MongoDBUtils.
 * @author Hugo Dominguez
 * @author Javier Delgado
 *
 */
public enum MongoDBUtil {
    INSTANCE;

    private final MongoDBUtils cUtils = new MongoDBUtils();

    public MongoDBUtils getMongoDBUtils() {
        return cUtils;
    }

}