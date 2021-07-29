/*
 * Copyright (c) 2021, Veepee
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose
 * with or without fee is hereby  granted, provided that the above copyright notice
 * and this permission notice appear in all copies.
 *
 * THE SOFTWARE  IS PROVIDED "AS IS"  AND THE AUTHOR DISCLAIMS  ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING  ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS.  IN NO  EVENT  SHALL THE  AUTHOR  BE LIABLE  FOR  ANY SPECIAL,  DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 * OF USE, DATA  OR PROFITS, WHETHER IN AN ACTION OF  CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR  IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
*/

package com.privalia.qa.utils;

import com.privalia.qa.specs.BaseGSpec;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ZookeeperSecUtilsIT extends BaseGSpec {

    @Test(enabled = false)
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
