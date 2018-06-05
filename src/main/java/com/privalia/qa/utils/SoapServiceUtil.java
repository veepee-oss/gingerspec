package com.privalia.qa.utils;

/**
 * Singleton class to retrieve {@link SoapServiceUtils} instance
 */
public enum SoapServiceUtil {

    INSTANCE;

    private final SoapServiceUtils soapServiceUtils = new SoapServiceUtils();

    public SoapServiceUtils getSoapServiceUtils() {
        return soapServiceUtils;
    }
}
