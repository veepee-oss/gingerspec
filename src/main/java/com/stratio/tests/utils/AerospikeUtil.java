package com.stratio.tests.utils;

public class AerospikeUtil {
	private static AerospikeUtil instance = new AerospikeUtil();
	private final AerospikeUtils cUtils = new AerospikeUtils();

	private AerospikeUtil() {
	}

	public static AerospikeUtil getInstance() {
		return instance;
	}

	public AerospikeUtils getAeroSpikeUtils() {
		return cUtils;
	}
}


