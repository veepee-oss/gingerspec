package com.stratio.assertions;

import java.util.List;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.internal.Strings;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SeleniumAssert extends AbstractAssert<SeleniumAssert, Object> {

    public SeleniumAssert(WebElement actual) {
        super(actual, SeleniumAssert.class);
    }

    public SeleniumAssert(List<WebElement> actual) {
        super(actual, SeleniumAssert.class);
    }

    public SeleniumAssert(WebDriver actual) {
        super(actual, SeleniumAssert.class);
    }

    public static SeleniumAssert assertThat(WebElement actual) {
        return new SeleniumAssert(actual);
    }

    public static SeleniumAssert assertThat(List<WebElement> actual) {
        return new SeleniumAssert(actual);
    }

    public static SeleniumAssert assertThat(WebDriver actual) {
        return new SeleniumAssert(actual);
    }

    public SeleniumAssert contains(CharSequence... values) {
        if (actual instanceof WebDriver) {
            Strings.instance().assertContains(info, ((WebDriver) actual).getPageSource(), values);
        } else if (actual instanceof WebElement) {
            Strings.instance().assertContains(info, ((WebElement) actual).getText(), values);
        }
        return this;
    }   
}
