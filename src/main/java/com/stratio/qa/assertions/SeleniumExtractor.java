package com.stratio.qa.assertions;

import org.assertj.core.api.iterable.Extractor;
import org.openqa.selenium.WebElement;

public final class SeleniumExtractor implements Extractor<WebElement, String> {

    private SeleniumExtractor() {
    }

    /**
     * Get selenium extractor.
     *
     * @return Extractor<WebElement, String>
     */
    public static Extractor<WebElement, String> linkText() {
        return new SeleniumExtractor();
    }

    @Override
    public String extract(WebElement input) {
        return input.getText();
    }
}
