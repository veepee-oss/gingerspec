package com.stratio.tests.utils;

import org.I0Itec.zkclient.ZkConnection;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;


import java.nio.charset.StandardCharsets;

public class ZookeeperUtils {


	private final Logger logger = LoggerFactory.getLogger(ZookeeperUtils.class);


	private String zk_hosts;
	private int timeout;
	private ZkConnection client;
	private Stat st = new Stat();
	private Watcher watcher = watchedEvent -> {};


	public ZookeeperUtils(String hosts, int timeout) {
		this.zk_hosts = hosts;
		this.timeout = timeout;
	}

	public void connectZk() {
		this.client = new ZkConnection(this.zk_hosts,this.timeout);
		this.client.connect(watcher);
	}

	public String zRead(String path){
		logger.debug("Trying to read data at " +path+ ".");
		byte[] b = new byte[0];
		String data;
		try {
			b = this.client.readData(path,this.st,false);
		} catch (KeeperException.NoNodeException e) {
			this.logger.error("No node has been found at "+e.getPath());
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (KeeperException e) {
			e.printStackTrace();
		}

		if(b==null){
			logger.info("Requested path "+path+" exists");
			data = "OK";
		}else{
			data = new String(b, StandardCharsets.UTF_8);
			logger.info("Requested path "+path+" contains "+data+".");
		}
		return data;
	}


	public void zCreate(String path, String document, boolean isEphimeral) throws KeeperException, InterruptedException {
		byte[] bDoc = document.getBytes(StandardCharsets.UTF_8);
		if(isEphimeral){
			this.client.create(path,bDoc, CreateMode.EPHEMERAL);
		}else{
			this.client.create(path,bDoc, CreateMode.PERSISTENT);
		}
	}

	public void zCreate(String path, boolean isEphimeral) throws KeeperException, InterruptedException {
		byte[] bDoc = "".getBytes(StandardCharsets.UTF_8);
		if(isEphimeral){
			this.client.create(path,bDoc, CreateMode.EPHEMERAL);
		}else{
			this.client.create(path,bDoc, CreateMode.PERSISTENT);
		}
	}

	public void delete(String path) throws KeeperException, InterruptedException {
		this.client.delete(path);
	}


	public String getZk_hosts() {
		return zk_hosts;
	}

	public void setZk_hosts(String zk_hosts) {
		this.zk_hosts = zk_hosts;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}
