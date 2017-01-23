package com.stratio.qa.utils;

/**
 * Singelton class of cassandra utils.
 */
public enum CassandraUtil {
    INSTANCE;

    private final CassandraUtils cUtils = new CassandraUtils();

    public CassandraUtils getCassandraUtils() {
        return cUtils;
    }

}