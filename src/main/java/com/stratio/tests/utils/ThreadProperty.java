package com.stratio.tests.utils;

import java.util.Properties;

public final class ThreadProperty {
	/**
	 * Default Constructor.
	 */
	private ThreadProperty() {
	}

	private static final ThreadLocal<Properties> PROPS = new ThreadLocal<Properties>() {
		protected Properties initialValue() {
			return new Properties();
		}
	};

	/**
	 * Set a string to share in other class.
	 * 
	 * @param key
	 * @param value
	 */
	public static void set(String key, String value) {
		PROPS.get().setProperty(key, value);
	}

	/**
	 * Get a property shared.
	 * 
	 * @param key
	 * @return String
	 */
	public static String get(String key) {
		return PROPS.get().getProperty(key);
	}
}
