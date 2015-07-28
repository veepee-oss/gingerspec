package com.stratio.tests.utils;

public enum ElasticSearchUtil {
    INSTANCE;

    private final ElasticSearchUtils cUtils = new ElasticSearchUtils();

    public ElasticSearchUtils getElasticSearchUtils() {
        return cUtils;
    }

}