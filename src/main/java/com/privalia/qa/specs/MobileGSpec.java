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

package com.privalia.qa.specs;

import io.cucumber.java.en.Given;
import io.appium.java_client.MobileDriver;
import org.openqa.selenium.ScreenOrientation;

import static org.testng.Assert.fail;

/**
 * Step definition for Appium (mobile testing).
 * <p>
 * This class contains the functionality that is only available in the {@link MobileDriver}. most of the steps
 * definitions for selenium ( in {@link SeleniumGSpec}) can be used for mobile testing
 *
 * Cast the instance of {@link org.openqa.selenium.WebDriver} to {@link MobileDriver} to access the specific
 * functions for mobile (take the existing functions as reference)
 *
 * @author José Fernandez
 */
public class MobileGSpec extends BaseGSpec {

    /**
     * Generic constructor.
     *
     * @param spec object
     */
    public MobileGSpec(CommonG spec) {
        this.commonspec = spec;

    }


    /**
     * Launches an application,
     * <p>
     * Launches the app, which was provided in the capabilities at session creation,
     * and (re)starts the session.
     * <pre>
     * Example:
     * {@code
     *      Scenario: Opening an closing the app
     *          Given I open the application
     *          And I wait '5' seconds
     * }
     * </pre>
     *
     * @see #closeApplication()
     */
    @Given("^I open the application$")
    public void launchApplication() {
        ((MobileDriver) this.commonspec.getDriver()).launchApp();
    }


    /**
     * Closes an application
     * <p>
     * Close the app which was provided in the capabilities at session creation
     * and quits the session.
     * <pre>
     * Example:
     * {@code
     *      Scenario: Opening an closing the app
     *          Given I open the application
     *          And I wait '5' seconds
     *          Given I close the application
     * }
     * </pre>
     * @see #launchApplication()
     */
    @Given("^I close the application$")
    public void closeApplication() {
        ((MobileDriver) this.commonspec.getDriver()).closeApp();
    }


    /**
     * Changes the device orientation
     * <pre>
     * Example
     * {@code
     *      Scenario: Changing orientation
     *          Given I rotate the device to 'landscape' mode
     *          And I wait '3' seconds
     *          Given I rotate the device to 'portrait' mode
     * }
     * </pre>
     *
     * @param orientation   Device orientation (portrait/landscape)
     * @throws Throwable    Throwable
     */
    @Given("^I rotate the device to '(landscape|portrait)' mode$")
    public void rotateDevice(String orientation) throws Throwable {

        if (orientation.matches("landscape")) {
            ((MobileDriver) this.commonspec.getDriver()).rotate(ScreenOrientation.LANDSCAPE);
        } else if (orientation.matches("portrait")) {
            ((MobileDriver) this.commonspec.getDriver()).rotate(ScreenOrientation.PORTRAIT);
        } else {
            fail("Unrecognized orientation: " + orientation);
        }
    }
}
