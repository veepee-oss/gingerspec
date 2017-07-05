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

import com.mongodb.DBObject;
import com.stratio.qa.specs.CommonG;
import com.stratio.qa.utils.PreviousWebElements;
import cucumber.api.DataTable;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class Assertions extends org.assertj.core.api.Assertions {
    /**
     * Check if two WebElements are equals.
     *
     * @param actual actual webElement
     * @return SeleniumAssert assertion
     */
    public static SeleniumAssert assertThat(WebElement actual) {
        return new SeleniumAssert(actual);
    }

    public static SeleniumAssert assertThat(PreviousWebElements actualList) {
        return new SeleniumAssert(actualList.getPreviousWebElements());
    }


    /**
     * Check if two WebDrivers are equals.
     *
     * @param actual webElement
     * @return SeleniumAssert assert
     */
    public static SeleniumAssert assertThat(WebDriver actual) {
        return new SeleniumAssert(actual);
    }


    public static SeleniumAssert assertThat(CommonG common, WebDriver actual) {
        return new SeleniumAssert(common, actual);
    }

    public static SeleniumAssert assertThat(CommonG common, WebElement actual) {
        return new SeleniumAssert(common, actual);
    }

    public static SeleniumAssert assertThat(CommonG common, List<WebElement> actual) {
        return new SeleniumAssert(common, actual);
    }

    public static SeleniumAssert assertThat(CommonG common, PreviousWebElements actual) {
        return new SeleniumAssert(common, actual);
    }

    public static SeleniumAssert assertThat(CommonG common, boolean actual) {
        return new SeleniumAssert(common, actual);
    }

    public static SeleniumAssert assertThat(CommonG common, String actual) {
        return new SeleniumAssert(common, actual);
    }

    public static DBObjectsAssert assertThat(DataTable data, ArrayList<DBObject> actual) {
        return new DBObjectsAssert(actual);
    }

}
