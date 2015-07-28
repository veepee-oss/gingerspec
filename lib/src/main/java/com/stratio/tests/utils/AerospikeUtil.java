package com.stratio.tests.utils;


public enum AerospikeUtil {
    INSTANCE;

    private final AerospikeUtils cUtils = new AerospikeUtils();

    public AerospikeUtils getAeroSpikeUtils() {
        return cUtils;
    }

}