package com.stratio.tests.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.zookeeper.KeeperException;
import org.testng.annotations.Test;

import com.stratio.specs.BaseGSpec;

public class ZookeeperUtilsIT extends BaseGSpec{

    @Test
    public void createNonEphemeralZnodeTest() throws KeeperException, InterruptedException {
        ZookeeperUtils zkUtils = new ZookeeperUtils();
        zkUtils.connectZk();
        String znodePath = "/mypath";
        if (zkUtils.exists(znodePath)) {
            zkUtils.delete(znodePath);
        }
        zkUtils.zCreate(znodePath, false);
        assertThat(zkUtils.exists(znodePath)).isTrue();
        zkUtils.disconnect();
    }

    @Test
    public void verifyANonEphemeralZnodeIsPersistedTest() throws KeeperException, InterruptedException {
        ZookeeperUtils zkUtils = new ZookeeperUtils();
        zkUtils.connectZk();
        String znodePath = "/mypath2";
        if (zkUtils.exists(znodePath)) {
            zkUtils.delete(znodePath);
        }
        zkUtils.zCreate(znodePath, false);
        zkUtils.disconnect();
        zkUtils.connectZk();
        assertThat(zkUtils.exists(znodePath)).isTrue();
        zkUtils.disconnect();
    }

    @Test
    public void createAnEphemeralZnodeTest() throws KeeperException, InterruptedException {
        ZookeeperUtils zkUtils = new ZookeeperUtils();
        zkUtils.connectZk();
        String znodePath = "/mypath3";
        if (zkUtils.exists(znodePath)) {
            zkUtils.delete(znodePath);
        }
        zkUtils.zCreate(znodePath, true);
        assertThat(zkUtils.exists(znodePath)).isTrue();
        zkUtils.disconnect();
    }

    @Test
    public void verifyAnEphemeralZnodeIsNotPersistedTest() throws KeeperException, InterruptedException {
        ZookeeperUtils zkUtils = new ZookeeperUtils();
        zkUtils.connectZk();
        String znodePath = "/mypath4";
        if (zkUtils.exists(znodePath)) {
            zkUtils.delete(znodePath);
        }
        zkUtils.zCreate(znodePath, true);
        zkUtils.disconnect();
        zkUtils.connectZk();
        assertThat(zkUtils.exists(znodePath)).isFalse();
        zkUtils.disconnect();
    }
}
