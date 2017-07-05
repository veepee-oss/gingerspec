/*
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.qa.assertions;

import com.stratio.qa.specs.CommonG;
import com.stratio.qa.utils.PreviousWebElements;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Condition;
import org.assertj.core.internal.Booleans;
import org.assertj.core.internal.Conditions;
import org.assertj.core.internal.Integers;
import org.assertj.core.internal.Strings;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class SeleniumAssert extends AbstractAssert<SeleniumAssert, Object> {

    private CommonG commonspec;

    /**
     * Constructor with WebElement.
     *
     * @param actual webElement used in assert
     */
    public SeleniumAssert(WebElement actual) {
        super(actual, SeleniumAssert.class);
    }

    /**
     * Constructor with a list of WebElements.
     *
     * @param actual webElement used in assert
     */
    public SeleniumAssert(List<WebElement> actual) {
        super(actual, SeleniumAssert.class);
    }

    /**
     * Constructor with WebDriver.
     *
     * @param actual webElement used in assert
     */
    public SeleniumAssert(WebDriver actual) {
        super(actual, SeleniumAssert.class);
    }


    /**
     * Constructor with CommonG and WebDriver.
     *
     * @param commong common object that contains relevant execution info common object
     * @param actual webElement used in assert
     */
    public SeleniumAssert(CommonG commong, WebDriver actual) {
        super(actual, SeleniumAssert.class);
        this.commonspec = commong;
    }


    /**
     * Constructor with CommonG and WebElement.
     *
     * @param commong common object that contains relevant execution info common object
     * @param actual webElement used in assert
     */
    public SeleniumAssert(CommonG commong, WebElement actual) {
        super(actual, SeleniumAssert.class);
        this.commonspec = commong;
    }


    /**
     * Constructor with CommonG and {@code List<WebElement>}.
     *
     * @param commong common object that contains relevant execution info common object
     * @param actual webElement used in assert
     */
    public SeleniumAssert(CommonG commong, List<WebElement> actual) {
        super(actual, SeleniumAssert.class);
        this.commonspec = commong;
    }


    /**
     * Constructor with CommonG and previousWebElements.
     *
     * @param commong common object that contains relevant execution info common object
     * @param actual webElement used in assert
     */
    public SeleniumAssert(CommonG commong, PreviousWebElements actual) {
        super(actual, SeleniumAssert.class);
        this.commonspec = commong;
    }


    /**
     * Constructor with CommonG and boolean.
     *
     * @param commong common object that contains relevant execution info common object
     * @param actual webElement used in assert
     */
    public SeleniumAssert(CommonG commong, boolean actual) {
        super(actual, SeleniumAssert.class);
        this.commonspec = commong;
    }


    /**
     * Constructor with CommonG and String.
     *
     * @param commong common object that contains relevant execution info common object
     * @param actual webElement used in assert
     */
    public SeleniumAssert(CommonG commong, String actual) {
        super(actual, SeleniumAssert.class);
        this.commonspec = commong;
    }

    /**
     * Checks a selenium WebElement.
     *
     * @param actual webElement used in assert
     * @return SeleniumAssert
     */
    public static SeleniumAssert assertThat(WebElement actual) {
        return new SeleniumAssert(actual);
    }

    /**
     * Checks a selenium a list of WebElements.
     *
     * @param actual webElement used in assert
     * @return SeleniumAssert
     */
    public static SeleniumAssert assertThat(List<WebElement> actual) {
        return new SeleniumAssert(actual);
    }

    /**
     * Checks a selenium WebDriver.
     *
     * @param actual webElement used in assert
     * @return SeleniumAssert
     */
    public static SeleniumAssert assertThat(WebDriver actual) {
        return new SeleniumAssert(actual);
    }

    /**
     * Checks a selenium WebDriver.
     *
     * @param commong common object that contains relevant execution info
     * @param actual webElement used in assert
     * @return SeleniumAssert
     */
    public static SeleniumAssert assertThat(CommonG commong, WebDriver actual) {
        return new SeleniumAssert(commong, actual);
    }

    /**
     * Checks a PreviousWebElements.
     *
     * @param commong common object that contains relevant execution info
     * @param actual webElement used in assert
     * @return SeleniumAssert
     */
    public static SeleniumAssert assertThat(CommonG commong, PreviousWebElements actual) {
        return new SeleniumAssert(commong, actual);
    }

    /**
     * Checks a selenium WebElement.
     *
     * @param commong common object that contains relevant execution info
     * @param actual webElement used in assert
     * @return SeleniumAssert
     */
    public static SeleniumAssert assertThat(CommonG commong, WebElement actual) {
        return new SeleniumAssert(commong, actual);
    }

    /**
     * Checks a selenium list of WebElements.
     *
     * @param commong common object that contains relevant execution info
     * @param actual webElement used in assert
     * @return SeleniumAssert
     */
    public static SeleniumAssert assertThat(CommonG commong, List<WebElement> actual) {
        return new SeleniumAssert(commong, actual);
    }

    /**
     * Checks a boolean.
     *
     * @param commong common object that contains relevant execution info
     * @param actual webElement used in assert
     * @return SeleniumAssert
     */
    public static SeleniumAssert assertThat(CommonG commong, boolean actual) {
        return new SeleniumAssert(commong, actual);
    }

    /**
     * Checks a String.
     *
     * @param commong common object that contains relevant execution info
     * @param actual webElement used in assert
     * @return SeleniumAssert
     */
    public static SeleniumAssert assertThat(CommonG commong, String actual) {
        return new SeleniumAssert(commong, actual);
    }

    /**
     * Returns the commonspec
     *
     * @return CommonG
     */
    public CommonG getCommonspec() {
        return this.commonspec;
    }

    /**
     * Checks if a webDriver or WebElement has values.
     *
     * @param values char sequence compared
     * @return SeleniumAssert
     */
    public SeleniumAssert contains(CharSequence... values) {
        if (actual instanceof WebDriver) {
            Strings.instance().assertContains(info,
                    ((WebDriver) actual).getPageSource(), values);
        } else if (actual instanceof WebElement) {
            Strings.instance().assertContains(info,
                    ((WebElement) actual).getText(), values);
        }
        return this;
    }


    /**
     * Checks if a WebElement is a TextField.
     *
     * @param cond webElement used in condition
     * @return SeleniumAssert
     */
    public SeleniumAssert isTextField(Condition<WebElement> cond) {
        if (actual instanceof List) {
            Conditions.instance().equals(cond);
        }
        return this;
    }


    /**
     * Checks if a {@code List<WebElement>} has at least @size elements.
     *
     * @param size integer used in size condition
     * @return SeleniumAssert
     */
    public SeleniumAssert hasAtLeast(Integer size) {
        if (actual instanceof List) {
            Integers.instance().assertGreaterThan(info, ((List<WebElement>) actual).size(), size);
        } else if (actual instanceof PreviousWebElements) {
            Integers.instance().assertGreaterThan(info, ((PreviousWebElements) actual).getPreviousWebElements().size(), size);
        }
        return this;
    }


    /**
     * Checks if a {@code List<WebElement>} has size @size.
     *
     * @param size integer used in size condition
     * @return SeleniumAssert
     */
    public SeleniumAssert hasSize(Integer size) {
        if (actual instanceof PreviousWebElements) {
            Integers.instance().assertEqual(info, ((PreviousWebElements) actual).getPreviousWebElements().size(), size);
        }
        return this;
    }


    /**
     * Checks boolean value.
     *
     * @param value boolean used in compare condition
     * @return SeleniumAssert
     */
    public SeleniumAssert isEqualTo(boolean value) {
        Booleans.instance().assertEqual(info, (Boolean) actual, value);
        return this;
    }


    /**
     * Checks boolean value.
     *
     * @param value integer used in size condition
     * @return SeleniumAssert
     */
    public SeleniumAssert isEqualTo(String value) {
        Strings.instance().assertMatches(info, (String) actual, value);
        return this;
    }


    /**
     * Checks String is not null.
     *
     * @return SeleniumAssert
     */
    public SeleniumAssert isNotNull() {
        Strings.instance().assertNotEmpty(info, (String) actual);
        return this;
    }


    /**
     * Checks string matches value.
     *
     * @param value String used in compare condition
     * @return SeleniumAssert
     */
    public SeleniumAssert matches(String value) {
        Strings.instance().assertMatches(info, (String) actual, value);
        return this;
    }

}
