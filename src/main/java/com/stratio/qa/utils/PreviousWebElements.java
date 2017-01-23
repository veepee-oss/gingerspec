package com.stratio.qa.utils;

import org.openqa.selenium.WebElement;

import java.util.List;

public final class PreviousWebElements {

    private List<WebElement> previousWebElements;

    public PreviousWebElements(List<WebElement> previousWebElements) {
        this.previousWebElements = previousWebElements;
    }

    public List<WebElement> getPreviousWebElements() {
        return previousWebElements;
    }

    public void setPreviousWebElements(List<WebElement> previousWebElements) {
        this.previousWebElements = previousWebElements;
    }

}