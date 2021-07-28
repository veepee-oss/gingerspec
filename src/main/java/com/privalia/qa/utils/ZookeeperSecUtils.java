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

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class ZookeeperSecUtils {

    private final Logger logger = LoggerFactory.getLogger(ZookeeperSecUtils.class);

    private static final String DEFAULT_ZK_HOSTS = "0.0.0.0:2181";

    private static final String DEFAULT_ZK_SESSION_TIMEOUT = "30000";

    private static final String DEFAULT_ZK_PRINCIPAL = "zookeeper/zookeeper-plugin-agent@DEMO.TEST.COM";

    private String zk_hosts;

    private int timeout;

    private ExponentialBackoffRetry retryPolicy;

    private CuratorFramework curatorZkClient;

    private Stat st;

    public ZookeeperSecUtils() {
        this.zk_hosts = System.getProperty("ZOOKEEPER_HOSTS", DEFAULT_ZK_HOSTS);
        //this.principal = System.getProperty("ZOOKEEPER_PRINCIPAL", DEFAULT_ZK_PRINCIPAL);
        this.timeout = Integer.parseInt(System.getProperty("ZOOKEEPER_SESSION_TIMEOUT", DEFAULT_ZK_SESSION_TIMEOUT));
        this.retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.curatorZkClient = CuratorFrameworkFactory.builder().connectString(this.zk_hosts).retryPolicy(this.retryPolicy).connectionTimeoutMs(this.timeout).build();

        if ("true".equals(System.getProperty("SECURIZED_ZOOKEEPER", "true"))) {
            System.setProperty("java.security.auth.login.config", System.getProperty("JAAS", "schemas/jaas.conf"));
            System.setProperty("java.security.krb5.conf", System.getProperty("KRB5", "schemas/krb5.conf"));
        }
    }

    public ZookeeperSecUtils(String hosts, int timeout) {
        this.zk_hosts = hosts;
        this.timeout = timeout;
        //this.principal = principal;
        this.retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.curatorZkClient = CuratorFrameworkFactory.builder().connectString(this.zk_hosts).retryPolicy(this.retryPolicy).connectionTimeoutMs(this.timeout).build();

        if ("true".equals(System.getProperty("SECURIZED_ZOOKEEPER", "true"))) {
            System.setProperty("java.security.auth.login.config", System.getProperty("JAAS", "schemas/jaas.conf"));
            System.setProperty("java.security.krb5.conf", System.getProperty("KRB5", "schemas/krb5.conf"));
        }
    }

    public void connectZk() throws InterruptedException {
        if (this.curatorZkClient.getState() != CuratorFrameworkState.STARTED) {
            this.curatorZkClient.start();
            this.curatorZkClient.blockUntilConnected();
        }
    }

    public String zRead(String path) throws Exception {
        logger.debug("Trying to read data at {}", path);
        byte[] b;
        String data;
        this.st = new Stat();

        b = this.curatorZkClient.getData().forPath(path);
        if (b == null) {
            data = "";
        } else {
            data = new String(b, StandardCharsets.UTF_8);
        }

        logger.debug("Requested path {} contains {}", path, data);

        return data;
    }


    public void zCreate(String path, String document, boolean isEphemeral) throws Exception {
        byte[] bDoc = document.getBytes(StandardCharsets.UTF_8);

        if (isEphemeral) {
            this.curatorZkClient.create().withMode(CreateMode.EPHEMERAL).forPath(path, bDoc);
        } else {
            this.curatorZkClient.create().withMode(CreateMode.PERSISTENT).forPath(path, bDoc);
        }
    }

    public void zCreate(String path, boolean isEphemeral) throws Exception {
        byte[] bDoc = "".getBytes(StandardCharsets.UTF_8);

        if (isEphemeral) {
            this.curatorZkClient.create().withMode(CreateMode.EPHEMERAL).forPath(path, bDoc);
        } else {
            this.curatorZkClient.create().withMode(CreateMode.PERSISTENT).forPath(path, bDoc);
        }
    }

    public Boolean isConnected() {
        return ((this.curatorZkClient != null) && (this.curatorZkClient.getZookeeperClient().isConnected()));
    }

    public Boolean exists(String path) throws Exception {
        return this.curatorZkClient.checkExists().forPath(path) != null;
    }

    public void delete(String path) throws Exception {
        this.curatorZkClient.delete().forPath(path);
    }

    public void disconnect() throws InterruptedException {
        this.curatorZkClient.getZookeeperClient().close();
    }

    public void setZookeeperSecConnection(String hosts, int timeout) {
        this.zk_hosts = hosts;
        this.timeout = timeout;
    }
}
