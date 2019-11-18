package com.privalia.qa.utils;

import io.appium.java_client.MobileElement;

import java.util.List;

public class PreviousMobileElements {

    private List<MobileElement> previousMobileElements;

    public PreviousMobileElements(List<MobileElement> previousMobileElements) {
        this.previousMobileElements = previousMobileElements;
    }

    public List<MobileElement> getPreviousMobileElements() {
        return previousMobileElements;
    }

    public void setPreviousMobileElements(List<MobileElement> previousMobileElements) {
        this.previousMobileElements = previousMobileElements;
    }
}
