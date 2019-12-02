package com.privalia.qa.specs;

import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import io.appium.java_client.MobileDriver;
import io.appium.java_client.TouchAction;
import org.openqa.selenium.DeviceRotation;
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
     * Launches the app, which was provided in the capabilities at session creation,
     * and (re)starts the session.
     */
    @Given("^I open the application$")
    public void iOpenTheApplication() {
        ((MobileDriver) this.commonspec.getDriver()).launchApp();
    }


    /**
     * Close the app which was provided in the capabilities at session creation
     * and quits the session.
     */
    @Given("^I close the application$")
    public void iCloseTheApplication() {
        ((MobileDriver) this.commonspec.getDriver()).closeApp();
    }


    /**
     * Changes the device orientation
     *
     * @param orientation   Device orientation (portrait/landscape)
     * @throws Throwable    Throwable
     */
    @Given("^I rotate the device to '(landscape|portrait)' mode$")
    public void iRotateTheDeviceToLandscapeMode(String orientation) throws Throwable {

        if (orientation.matches("landscape")) {
            ((MobileDriver) this.commonspec.getDriver()).rotate(ScreenOrientation.LANDSCAPE);
        } else if (orientation.matches("portrait")) {
            ((MobileDriver) this.commonspec.getDriver()).rotate(ScreenOrientation.PORTRAIT);
        } else {
            fail("Unrecognized orientation: " + orientation);
        }
    }
}
