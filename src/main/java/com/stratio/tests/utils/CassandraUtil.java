package com.stratio.tests.utils;

/**
 * Singelton class of cassandra utils.
 *
 */
public enum CassandraUtil {
    INSTANCE;

    private final CassandraUtils cUtils = new CassandraUtils();

    public CassandraUtils getCassandraUtils() {
        return cUtils;
    }

}