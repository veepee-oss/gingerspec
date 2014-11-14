package com.stratio.tests.utils;

/**
 * @author Javier Delgado
 * @author Hugo Dominguez
 * 
 */
public final class HashUtils {

    private HashUtils() {
    }
/**
 * doHash
 * @param str
 * @return
 */
    public static String doHash(String str) {

        Integer hash = 7;
        for (Integer i = 0; i < str.length(); i++) {
            hash = hash * 31 + str.charAt(i);
        }

        return hash.toString();
    }
}