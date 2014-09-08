package com.stratio.tests.utils;

public class CassandraUtil {

    private static CassandraUtil instance = new CassandraUtil();
    private final CassandraUtils cUtils = new CassandraUtils();

    private CassandraUtil() {
    }

    public static CassandraUtil getInstance() {
        return instance;
    }

    public CassandraUtils getCassandraUtils() {
        return cUtils;
    }
}
