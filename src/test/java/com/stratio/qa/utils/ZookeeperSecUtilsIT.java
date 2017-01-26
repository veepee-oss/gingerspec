package com.stratio.qa.utils;

import com.stratio.qa.specs.BaseGSpec;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ZookeeperSecUtilsIT extends BaseGSpec {

    @Test
    public void zookeeperSecTest() throws Exception {
        ZookeeperSecUtils zkUtils = new ZookeeperSecUtils();

        // Connect
        zkUtils.connectZk();

        // createNonEphemeralZnode
        String znodePath = "/mypath";
        if (zkUtils.exists(znodePath)) {
            zkUtils.delete(znodePath);
        }
        zkUtils.zCreate(znodePath, false);
        assertThat(zkUtils.exists(znodePath)).isTrue();

        // createAnEphemeralZnode
        znodePath = "/mypath3";
        if (zkUtils.exists(znodePath)) {
            zkUtils.delete(znodePath);
        }
        zkUtils.zCreate(znodePath, true);
        assertThat(zkUtils.exists(znodePath)).isTrue();

        // deleteANonEphemeralZnode
        znodePath = "/mypath5";
        if (zkUtils.exists(znodePath)) {
            zkUtils.delete(znodePath);
        }
        zkUtils.zCreate(znodePath, false);
        zkUtils.delete(znodePath);
        assertThat(zkUtils.exists(znodePath)).isFalse();

        // deleteAnEphemeralZnode
        znodePath = "/mypath6";
        if (zkUtils.exists(znodePath)) {
            zkUtils.delete(znodePath);
        }
        zkUtils.zCreate(znodePath, true);
        zkUtils.delete(znodePath);
        assertThat(zkUtils.exists(znodePath)).isFalse();

        // verifyWriteAndReadDataToAnEphemeralZnode
        String znodeContent = "hello";
        znodePath = "/mypath7";
        if (zkUtils.exists(znodePath)) {
            zkUtils.delete(znodePath);
        }
        zkUtils.zCreate(znodePath, znodeContent, false);
        assertThat(zkUtils.zRead(znodePath)).isEqualToIgnoringCase(znodeContent);
        zkUtils.delete(znodePath);

        // Disconnect
        zkUtils.disconnect();
    }
}
