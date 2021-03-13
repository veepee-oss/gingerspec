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

package com.privalia.qa.specs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.privalia.qa.utils.ThreadProperty;
import io.appium.java_client.MobileDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.restassured.http.ContentType;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.fail;


/**
 * This class contains functions that are executed before and after each test.
 * For this, it makes use of cucumber hooks
 *
 * @author Jose Fernandez
 * @see <a href="https://cucumber.io/docs/cucumber/api/#hooks">https://cucumber.io/docs/cucumber/api/#hooks</a>
 */
public class HookGSpec extends BaseGSpec {

    public static final int PAGE_LOAD_TIMEOUT = 120;

    public static final int IMPLICITLY_WAIT = 10;

    public static final int SCRIPT_TIMEOUT = 30;

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HookGSpec.class);


    protected WebDriver driver;

    /**
     * Default constructor.
     *
     * @param spec commonG object
     */
    public HookGSpec(CommonG spec) {
        this.commonspec = spec;
    }


    /**
     * Clean the exception list before each scenario.
     */
    @Before(order = 0)
    public void globalSetup() {
        /*Removes unnecessary logging messages that are produced by dependencies that make use of java.util.logging.Logger*/
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(Level.WARNING);

        /*Clears the exceptions stacktrace for the new test*/
        commonspec.getExceptions().clear();

    }


    /**
     * If the feature has the @web annotation, creates a new selenium web driver
     * before each scenario
     *
     * @throws MalformedURLException MalformedURLException
     */
    @Before(order = 10, value = "@web")
    public void seleniumSetup() throws Exception {

        MutableCapabilities mutableCapabilities = null;
        Map<String, String> capabilities = new HashMap<String, String>();
        ObjectMapper mapper = new ObjectMapper();
        boolean isLocal = ((System.getProperty("SELENIUM_GRID") != null) ? false : true);
        String[] arguments = System.getProperty("SELENIUM_ARGUMENTS", "--ignore-certificate-errors;--no-sandbox").split(";");


        switch (System.getProperty("browserName", "chrome").toLowerCase()) {
            case "chrome":

                if ("android".matches(System.getProperty("platformName", "").toLowerCase())) {
                    mutableCapabilities = DesiredCapabilities.android(); //Testing in chrome for android
                } else if ("ios".matches(System.getProperty("platformName", "").toLowerCase())) {
                    mutableCapabilities = DesiredCapabilities.iphone(); //Testing in chrome for iphone
                } else {
                    mutableCapabilities = DesiredCapabilities.chrome(); //Testing in desktop version of chrome
                }

                ChromeOptions chromeOptions = new ChromeOptions();
                for (String argument : arguments) {
                    chromeOptions.addArguments(argument);
                }

                mutableCapabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

                if (isLocal) {
                    System.setProperty("webdriver.chrome.silentOutput", "true"); //removes logging messages
                    WebDriverManager.chromedriver().setup();
                    driver = new ChromeDriver(chromeOptions);
                }

                break;

            case "opera":
                mutableCapabilities = DesiredCapabilities.operaBlink();

                OperaOptions operaOptions = new OperaOptions();
                for (String argument : arguments) {
                    operaOptions.addArguments(argument);
                }

                mutableCapabilities.setCapability(OperaOptions.CAPABILITY, operaOptions);

                if (isLocal) {
                    System.setProperty("webdriver.opera.silentOutput", "true"); //removes logging messages
                    WebDriverManager.operadriver().setup();
                    driver = new OperaDriver(operaOptions);
                }

                break;

            case "edge":
                mutableCapabilities = DesiredCapabilities.edge();
                mutableCapabilities.setCapability("platform", "ANY");

                if (System.getProperty("SELENIUM_ARGUMENTS") != null) {
                    LOGGER.warn("Use of -DSELENIUM_ARGUMENTS not supported for edge browser. Continuing....");
                }

                if (isLocal) {
                    EdgeOptions edgeOptions = new EdgeOptions();
                    System.setProperty("webdriver.edge.silentOutput", "true"); //removes logging messages
                    WebDriverManager.edgedriver().setup();
                    driver = new EdgeDriver(edgeOptions);
                }
                break;

            case "ie":
                mutableCapabilities = DesiredCapabilities.edge();

                if (System.getProperty("SELENIUM_ARGUMENTS") != null) {
                    LOGGER.warn("Use of -DSELENIUM_ARGUMENTS not supported for Internet Explorer browser. Continuing....");
                }

                if (isLocal) {
                    InternetExplorerOptions ieOptions = new InternetExplorerOptions();
                    System.setProperty("webdriver.edge.silentOutput", "true"); //removes logging messages
                    ieOptions.setCapability("ignoreZoomSetting", true);
                    WebDriverManager.iedriver().setup();
                    driver = new InternetExplorerDriver(ieOptions);
                }
                break;

            case "firefox":
                mutableCapabilities = DesiredCapabilities.firefox();

                FirefoxOptions firefoxOptions = new FirefoxOptions();
                for (String argument : arguments) {
                    firefoxOptions.addArguments(argument);
                }

                mutableCapabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, firefoxOptions);

                if (isLocal) {
                    System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true"); //removes logging messages
                    System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");  //removes logging messages
                    WebDriverManager.firefoxdriver().setup();
                    driver = new FirefoxDriver(firefoxOptions);
                }

                break;

            case "phantomjs":
                mutableCapabilities = DesiredCapabilities.phantomjs();

                if (System.getProperty("SELENIUM_ARGUMENTS") != null) {
                    LOGGER.warn("Use of -DSELENIUM_ARGUMENTS not supported for Phantomjs browser. Continuing....");
                }

                if (isLocal) {
                    throw new WebDriverException("phantomjs is not supported for local execution");
                }
                break;

            case "safari":

                if ("ios".matches(System.getProperty("platformName", "").toLowerCase())) {
                    mutableCapabilities = DesiredCapabilities.iphone(); //Testing in safari for iphone
                } else {
                    mutableCapabilities = new SafariOptions();          //Testing in Safari desktop browser
                }

                if (System.getProperty("SELENIUM_ARGUMENTS") != null) {
                    LOGGER.warn("Use of -DSELENIUM_ARGUMENTS not supported for Safari browser. Continuing....");
                }

                if (isLocal) {
                    SafariOptions safariOptions = new SafariOptions();
                    driver = new SafariDriver(safariOptions);
                }

                break;

            default:
                commonspec.getLogger().error("Unknown browser: " + System.getProperty("browserName") + ". For using local browser, only Chrome/Opera/Edge/IE/Firefox are supported");
                throw new WebDriverException("Unknown browser: " + System.getProperty("browserName") + ". For using local browser, only Chrome/Opera/Edge/IE/Firefox are supported");
        }



        if (isLocal) {
            /**
             * Execute the tests using a local browser
             */
            this.getCommonSpec().getLogger().debug("Setting local driver with capabilities %s", mutableCapabilities.toJson().toString());
            commonspec.setDriver(driver);
        } else {
            /**
             * When using the special constructor in the runner class, the variable "browser" contains a json string of the capabilities
             * of the remote node to use. If the variable is present, is forced to use those values
             */
            if (ThreadProperty.get("browser") != null) {
                capabilities = mapper.readValue(ThreadProperty.get("browser"), Map.class);
                mutableCapabilities.setCapability("browserName", capabilities.get("browserName"));
                mutableCapabilities.setCapability("version", capabilities.getOrDefault("version", ""));
                mutableCapabilities.setCapability("platform", capabilities.getOrDefault("platform", "ANY"));
                mutableCapabilities.setCapability("platformName", capabilities.getOrDefault("platformName", "ANY"));
            } else {
                /**
                 * The user can provide the variables "platform", "version" and "platformName" in case the default capabilities need to be changed
                 */
                if (System.getProperty("platform") != null)
                    mutableCapabilities.setCapability("platform", System.getProperty("platform"));
                if (System.getProperty("version") != null)
                    mutableCapabilities.setCapability("version", System.getProperty("version"));
                if (System.getProperty("platformName") != null)
                    mutableCapabilities.setCapability("platformName", System.getProperty("platformName"));
            }

            this.getCommonSpec().getLogger().debug("Setting RemoteWebDriver with capabilities %s", mutableCapabilities.toJson().toString());
            commonspec.setDriver(new RemoteWebDriver(new URL("http://" + System.getProperty("SELENIUM_GRID") + "/wd/hub"), mutableCapabilities));
        }

        commonspec.getDriver().manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT, TimeUnit.SECONDS);
        commonspec.getDriver().manage().timeouts().implicitlyWait(IMPLICITLY_WAIT, TimeUnit.SECONDS);
        commonspec.getDriver().manage().timeouts().setScriptTimeout(SCRIPT_TIMEOUT, TimeUnit.SECONDS);

    }

    /**
     * If the feature has the @mobile annotation, creates a new Appium driver
     * before each scenario
     *
     * @throws MalformedURLException MalformedURLException
     */
    @Before(order = 20, value = "@mobile")
    public void AppiumSetup() throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        MutableCapabilities capabilities = new DesiredCapabilities();

        String grid = System.getProperty("SELENIUM_GRID");
        String b = ThreadProperty.get("browser");

        if (grid == null) {
            fail("Selenium grid not available. You must use -DSELENIUM_GRID");
        }

        if (ThreadProperty.get("browser") != null) {
            Map<String, String> capabilitiesMap = mapper.readValue(b, Map.class);

            //This capabilities are removed since they can cause problems when testing mobile apps
            capabilitiesMap.remove("platform");
            capabilitiesMap.remove("maxInstances");
            capabilitiesMap.remove("seleniumProtocol");

            //Assign all found capabilities found returned by the selenium node
            for (Map.Entry<String, String> entry : capabilitiesMap.entrySet()) {
                capabilities.setCapability(entry.getKey(), (Object) entry.getValue());
            }
        } else {
            if (System.getProperty("app") != null)
                capabilities.setCapability(MobileCapabilityType.APP, System.getProperty("app"));
            if (System.getProperty("platformName") != null)
                capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, System.getProperty("platformName"));
            if (System.getProperty("udid") != null)
                capabilities.setCapability(MobileCapabilityType.UDID, System.getProperty("udid"));
            if (System.getProperty("deviceName") != null)
                capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, System.getProperty("deviceName"));
            if (System.getProperty("platformVersion") != null)
                capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, System.getProperty("platformVersion"));
            if (System.getProperty("browserName") != null)
                capabilities.setCapability(MobileCapabilityType.BROWSER_NAME, System.getProperty("browserName"));
        }

        if (capabilities.getCapability("platformName") == null){
            commonspec.getLogger().warn("No platformName capability found, using Android......");
            capabilities.setCapability("platformName", "Android");
        }

        this.getCommonSpec().getLogger().debug("Setting MobileWebDriver with capabilities %s", capabilities.toJson().toString());

        switch (capabilities.getCapability("platformName").toString().toLowerCase()) {

            case "android":
                if (System.getProperty("automationName") != null)
                    capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, System.getProperty("automationName", "Appium"));
                if (System.getProperty("deviceName") != null)
                    capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, System.getProperty("deviceName"));

                commonspec.getLogger().debug("Building AndroidDriver with capabilities %s", capabilities.toJson().toString());
                commonspec.setDriver(new AndroidDriver(new URL("http://" + grid + "/wd/hub"), capabilities));
                break;

            case "ios":
                if (System.getProperty("automationName") != null) {
                    capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, System.getProperty("automationName"));
                } else {
                    commonspec.getLogger().warn("Using default automationName=XCUITest for ios. Change this by using -DautomationName='<name>'");
                    capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "XCUITest");
                }
                if (System.getProperty("deviceName") != null) {
                    capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, System.getProperty("deviceName"));
                } else {
                    commonspec.getLogger().warn("deviceName capability is required for ios!! trying to use deviceName='My iphone'. Change this by using -DdeviceName='<name>'");
                    capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "My iphone");
                }

                commonspec.getLogger().debug("Building IOSDriver with capabilities %s", capabilities.toJson().toString());
                commonspec.setDriver(new IOSDriver(new URL("http://" + grid + "/wd/hub"), capabilities));
                break;

            default:
                commonspec.getLogger().error("Unknown platformName: %s, only android/ios is allowed", capabilities.getCapability("platformName").toString());
                throw new WebDriverException("Unknown platformName: " + capabilities.getCapability("platformName").toString() + ", only android/ios is allowed");
        }

    }

    /**
     * If the feature has the @web or @mobile annotation, closes selenium web driver after each scenario is completed.
     *
     * @param scenario Instance of the scenario just executed
     * @throws IOException The IOException
     */
    @After(order = 20, value = "@web or @mobile")
    public void seleniumTeardown(Scenario scenario) throws IOException {
        if (commonspec.getDriver() != null) {
            try {
                if (scenario.isFailed()) {
                    //Include the page source in the report
                    commonspec.getLogger().debug("Scenario failed. Adding page source to report");
                    String source = driver.getPageSource();
                    scenario.attach(source.getBytes(), "text/html", "Page source html");

                    //Include screenshot in the report
                    commonspec.getLogger().debug("Adding screenshot to report");
                    byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                    scenario.attach(screenshot, "image/png", "Screenshot");

                    //Take screenshot and save it in the target/execution folder
                    commonspec.getLogger().debug("Adding screenshot target/execution folder");
                    if (this.commonspec.getDriver() instanceof MobileDriver) {
                        this.commonspec.captureEvidence(driver, "mobileScreenCapture", "exception");
                        this.commonspec.captureEvidence(driver, "mobilePageSource", "exception");
                    } else {
                        this.commonspec.captureEvidence(driver, "framehtmlSource", "exception");
                        this.commonspec.captureEvidence(driver, "htmlSource", "exception");
                        this.commonspec.captureEvidence(driver, "screenCapture", "exception");
                    }
                }
            } finally {
                //Close the selenium driver
                commonspec.getLogger().debug("Shutting down Selenium client");
                commonspec.getDriver().quit();
            }
        }
    }


    /**
     * If the feature has the @rest annotation, creates a new REST client before each scenario
     */
    @Before(order = 10, value = "@rest")
    public void restClientSetup() {
        commonspec.getLogger().debug("Starting a REST client");

        commonspec.setClient(new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setAcceptAnyCertificate(true).setAllowPoolingConnections(false)
                .build()));

        commonspec.setRestRequest(given().contentType(ContentType.JSON));

    }

    /**
     * If the feature has the @rest annotation, closes the REST client after each scenario is completed
     */
    @After(order = 10, value = "@rest")
    public void restClientTeardown() {
        commonspec.getLogger().debug("Shutting down REST client");
        commonspec.getClient().close();

    }

    /**
     * Disconnect any remaining open SSH connection after each scenario is completed
     */
    @After(order = 10)
    public void remoteSSHConnectionTeardown() {
        if (commonspec.getRemoteSSHConnection() != null) {
            commonspec.getLogger().debug("Closing SSH remote connection");
            commonspec.getRemoteSSHConnection().getSession().disconnect();
        }
    }

    /**
     * If the feature has the @sql annotation, closes any open connection to a database after each scenario is completed
     *
     * @throws Exception Exception
     */
    @After(value = "@sql")
    public void sqlConnectionClose() throws Exception {
        if ((commonspec.getSqlClient() != null) && (commonspec.getSqlClient().connectionStatus())) {
            commonspec.getLogger().debug("Closing SQL remote connection");
            commonspec.getSqlClient().disconnect();
        }
    }
}
