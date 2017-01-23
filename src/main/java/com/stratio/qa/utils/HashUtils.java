package com.stratio.qa.utils;

public final class HashUtils {
    private static final int HASH = 7;
    private static final int MULTIPLIER = 31;

    private HashUtils() {
    }

    /**
     * doHash.
     *
     * @param str
     * @return String
     */
    public static String doHash(String str) {

        Integer hash = HASH;
        for (Integer i = 0; i < str.length(); i++) {
            hash = hash * MULTIPLIER + str.charAt(i);
        }

        return hash.toString();
    }
}