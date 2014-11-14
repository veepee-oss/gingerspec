package com.stratio.tests.utils;

/**
 * @author Javier Delgado
 * @author Hugo Dominguez
 * 
 */
public enum AerospikeUtil {
    INSTANCE;

    private final AerospikeUtils cUtils = new AerospikeUtils();

    public AerospikeUtils getAeroSpikeUtils() {
        return cUtils;
    }

}