package com.privalia.qa.utils;

/**
 * Singleton class to retrieve {@link FileParserUtils} instance
 */
public enum FileParserUtil {

    INSTANCE;

    private final FileParserUtils fileParserUtils = new FileParserUtils();

    public FileParserUtils getFileParserUtils() {
        return fileParserUtils;
    }
}
