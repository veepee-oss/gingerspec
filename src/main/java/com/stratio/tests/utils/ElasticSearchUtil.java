package com.stratio.tests.utils;

public class ElasticSearchUtil {

	private static ElasticSearchUtil instance = new ElasticSearchUtil();
	private final ElasticSearchUtils esUtils = new ElasticSearchUtils();

	private ElasticSearchUtil() {
	}

	public static ElasticSearchUtil getInstance() {
		return instance;
	}

	public ElasticSearchUtils getElasticSearchUtils() {
		return esUtils;
	}
}
