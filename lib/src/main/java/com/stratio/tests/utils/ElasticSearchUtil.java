package com.stratio.tests.utils;

/**
 * @author Javier Delgado
 * @author Hugo Dominguez
 * 
 */
public enum ElasticSearchUtil {
    INSTANCE;

    private final ElasticSearchUtils cUtils = new ElasticSearchUtils();

    public ElasticSearchUtils getElasticSearchUtils() {
        return cUtils;
    }

}