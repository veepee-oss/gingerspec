package com.privalia.qa.utils;

/**
 * Singleton class to retrieve {@link SqlUtils} instance
 */
public enum SqlUtil {
    INSTANCE;

    private final SqlUtils sqlUtils = new SqlUtils();

    public SqlUtils getSqlUtils() {
        return sqlUtils;
    }
}
