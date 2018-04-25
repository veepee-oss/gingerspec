package com.privalia.qa.utils;

import com.privalia.qa.utils.ThreadProperty;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;

import static com.privalia.qa.assertions.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class ElementCountByMethod implements ExpectedCondition<List<WebElement>> {

    private String method;

    private String element;

    private Integer expectedCount;

    private final Logger logger = LoggerFactory.getLogger(ThreadProperty.get("class"));

    public ElementCountByMethod(String method, String element, Integer expectedCount) {
        this.element = element;
        this.method = method;
        this.expectedCount = expectedCount;
    }

    @Nullable
    @Override
    public List<WebElement> apply(@Nullable WebDriver input) {
        WebDriver driver = (WebDriver) input;

        List<WebElement> wel = null;

        if ("id".equals(method)) {
            logger.debug("Locating {} by id", element);
            wel = driver.findElements(By.id(element));
        } else if ("name".equals(method)) {
            logger.debug("Locating {} by name", element);
            wel = driver.findElements(By.name(element));
        } else if ("class".equals(method)) {
            logger.debug("Locating {} by class", element);
            wel = driver.findElements(By.className(element));
        } else if ("xpath".equals(method)) {
            logger.debug("Locating {} by xpath", element);
            wel = driver.findElements(By.xpath(element));
        } else if ("css".equals(method)) {
            wel = driver.findElements(By.cssSelector(element));
        } else {
            fail("Unknown search method: " + method);
        }

        return wel.size() > 0 ? wel : null;

    }
}
