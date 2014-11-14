package com.stratio.tests.utils;

/**
 * Singelton class of cassandra utils.
 * @author Hugo Dominguez
 * @author Javier Delgado
 *
 */
public enum CassandraUtil {
    INSTANCE;

    private final CassandraUtils cUtils = new CassandraUtils();

    public CassandraUtils getCassandraUtils() {
        return cUtils;
    }

}