package com.stratio.assertions;

import org.assertj.core.api.iterable.Extractor;
import org.openqa.selenium.WebElement;

public class SeleniumExtractor implements Extractor<WebElement, String> {

    private SeleniumExtractor() {
    }

    public static Extractor<WebElement, String> linkText() {
        return new SeleniumExtractor();
    }

    @Override
    public String extract(WebElement input) {
        return input.getText();
    }
}
