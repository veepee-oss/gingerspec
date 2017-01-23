package com.stratio.tests.utils;

public enum ZookeeperUtil {
    INSTANCE;

    private final ZookeeperUtils zUtils = new ZookeeperUtils();

    public ZookeeperUtils getZookeeperUtils() {
        return zUtils;
    }

}