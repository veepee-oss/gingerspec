package com.stratio.tests.utils;

import java.util.Properties;

public class ThreadProperty {

	private final static ThreadLocal<Properties> props = new ThreadLocal<Properties>() {
		protected Properties initialValue() {
			return new Properties();
		}
	};

	public static void set(String key, String value) {
		props.get().setProperty(key, value);
	}

	public static String get(String key) {
		return props.get().getProperty(key);
	}
}
