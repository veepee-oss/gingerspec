package com.stratio.tests.utils;

import java.util.Properties;

public final class ThreadProperty {

    private ThreadProperty() {
    }

    private static final ThreadLocal<Properties> PROPS = new ThreadLocal<Properties>() {
        protected Properties initialValue() {
            return new Properties();
        }
    };

    public static void set(String key, String value) {
        PROPS.get().setProperty(key, value);
    }

    public static String get(String key) {
        return PROPS.get().getProperty(key);
    }
}
