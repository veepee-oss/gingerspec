package com.stratio.assertions;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.stratio.tests.utils.HttpResponse;

public class Assertions extends org.assertj.core.api.Assertions {

    public static HttpResponseAssert assertThat(HttpResponse actual) {
        return new HttpResponseAssert(actual);
    }

    public static SeleniumAssert assertThat(WebElement actual) {
        return new SeleniumAssert(actual);
    }

    public static SeleniumAssert assertThat(WebDriver actual) {
        return new SeleniumAssert(actual);
    }

}
