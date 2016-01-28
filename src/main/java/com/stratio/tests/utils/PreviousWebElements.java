package com.stratio.tests.utils;

import java.util.List;

import org.openqa.selenium.WebElement;

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