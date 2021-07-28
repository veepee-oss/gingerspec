/*
 * Copyright (c) 2021, Veepee
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose
 * with or without fee is hereby  granted, provided that the above copyright notice
 * and this permission notice appear in all copies.
 *
 * THE SOFTWARE  IS PROVIDED "AS IS"  AND THE AUTHOR DISCLAIMS  ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING  ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS.  IN NO  EVENT  SHALL THE  AUTHOR  BE LIABLE  FOR  ANY SPECIAL,  DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 * OF USE, DATA  OR PROFITS, WHETHER IN AN ACTION OF  CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR  IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
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

import static org.testng.Assert.fail;

/**
 * Custom ExpectedCondition to evaluate if the amount of web elements in a page
 * match the expected count. This class is to be used with FluentWait (i.e. wait.until(new ElementCountByMethod(..)))
 * @author Jose Fernandez
 */
public class ElementCountByMethod implements ExpectedCondition<List<WebElement>> {

    private String method;

    private String element;

    private Integer expectedCount;

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

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

        return wel.size() == this.expectedCount ? wel : null;

    }
}
