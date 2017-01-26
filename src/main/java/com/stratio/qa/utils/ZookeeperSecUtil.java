package com.stratio.qa.utils;

public enum ZookeeperSecUtil {
    INSTANCE;

    private final ZookeeperSecUtils zUtils = new ZookeeperSecUtils();

    public ZookeeperSecUtils getZookeeperSecUtils() {
        return zUtils;
    }

}
