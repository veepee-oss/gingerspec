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
import org.openqa.selenium.safari.SafariOptions;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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

        String grid = System.getProperty("SELENIUM_GRID");

        if (grid == null) {
            this.useLocalDriver();
        } else {
            this.useRemoteGrid(grid);
        }
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
        String grid = System.getProperty("SELENIUM_GRID");
        String b = ThreadProperty.get("browser");

        if (grid == null) {
            fail("Selenium grid not available");
        }

        if ("".equals(b)) {
            fail("No available nodes connected");
        }

        Map<String, String> capabilitiesMap = mapper.readValue(b, Map.class);

        commonspec.setBrowserName(capabilitiesMap.get("automationName"));
        commonspec.getLogger().debug("Setting up selenium for {}", capabilitiesMap.get("automationName"));

        grid = "http://" + grid + "/wd/hub";
        MutableCapabilities capabilities = null;
        capabilities = new DesiredCapabilities();

        //This capabilities are removed since they can cause problems when testing mobile apps
        capabilitiesMap.remove("platform");
        capabilitiesMap.remove("maxInstances");
        capabilitiesMap.remove("seleniumProtocol");

        //Assign all found capabilities found returned by the selenium node
        for (Map.Entry<String, String> entry : capabilitiesMap.entrySet()) {
            capabilities.setCapability(entry.getKey(), (Object) entry.getValue());
        }

        /*
          When testing mobile apps, the "app" capability is necessary to indicate the app under test
          This variable can also be provided using the maven variable -DAPP=/full/path/to/file.
          If both, an app capability and -DAPP variable are provided, the -DAPP will take precedence
         */

        String app = capabilitiesMap.get("app");

        if (System.getProperty("APP") != null) {
            app = System.getProperty("APP");
        }

        if (app == null) {
            fail("No app specified (The absolute local path or remote http URL of an .apk or .ipa file). You can specify this in the node capabilities or using -DAPP=/full/path/to/file");
        }

        capabilities.setCapability("app", app);

        switch (capabilitiesMap.get("platformName").toLowerCase()) {

            case "android":
                commonspec.setDriver(new AndroidDriver(new URL(grid), capabilities));
                break;

            case "ios":
                commonspec.setDriver(new IOSDriver(new URL(grid), capabilities));
                break;

            default:
                commonspec.getLogger().error("Unknown platform: " + capabilitiesMap.get("platformName"));
                throw new WebDriverException("Unknown platform: " + capabilitiesMap.get("platformName"));
        }

    }

    /**
     * Connects to a remote selenium grid to execute the tests
     *
     * @param grid            Address of the remote selenium grid
     * @throws MalformedURLException MalformedURLException
     */
    private void useRemoteGrid(String grid) throws MalformedURLException, JsonProcessingException {

        MutableCapabilities mutableCapabilities = null;
        Map<String, String> capabilitiesMap = null;
        ObjectMapper mapper = new ObjectMapper();
        String[] arguments = System.getProperty("SELENIUM_ARGUMENTS", "--ignore-certificate-errors;--no-sandbox").split(";");
        grid = "http://" + grid + "/wd/hub";


        if (ThreadProperty.get("browser") != null) {
            capabilitiesMap = mapper.readValue(ThreadProperty.get("browser"), Map.class);
        } else {
            String capabilities = System.getProperty("SELENIUM_CAPABILITIES", "{ \"browserName\": \"chrome\", \"platformName\": \"LINUX\" }");
            capabilitiesMap = mapper.readValue(capabilities, Map.class);
        }

        String platformName = capabilitiesMap.getOrDefault("platformName", "LINUX");
        commonspec.getLogger().debug("Using platformName: {}", platformName);

        switch (capabilitiesMap.getOrDefault("browserName", "Chrome").toLowerCase()) {
            case "chrome":

                commonspec.getLogger().debug("Setting up selenium for chrome in {}", platformName);

                if ("android".matches(platformName.toLowerCase())) {
                    //Testing in chrome for android
                    mutableCapabilities = DesiredCapabilities.android();

                } else if ("ios".matches(platformName.toLowerCase())) {
                    //Testing in chrome for iphone
                    mutableCapabilities = DesiredCapabilities.iphone();

                } else {
                    //Testing in desktop version of chrome
                    ChromeOptions chromeOptions = new ChromeOptions();

                    commonspec.getLogger().debug("Configuring chrome desktop with arguments {} ", String.join(";", arguments));
                    for (String argument: arguments) {
                        chromeOptions.addArguments(argument);
                    }
                    mutableCapabilities = new ChromeOptions();
                    mutableCapabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
                }

                break;

            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();

                commonspec.getLogger().debug("Configuring firefox desktop with arguments {} ", String.join(";", arguments));
                for (String argument: arguments) {
                    firefoxOptions.addArguments(argument);
                }
                mutableCapabilities = new FirefoxOptions();
                mutableCapabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, firefoxOptions);
                break;

            case "phantomjs":
                mutableCapabilities = DesiredCapabilities.phantomjs();
                break;

            case "safari":

                commonspec.getLogger().debug("Setting up selenium for chrome in {}", platformName);

                if ("ios".matches(platformName.toLowerCase())) {
                    //Testing in safari for iphone
                    mutableCapabilities = DesiredCapabilities.iphone();

                } else {
                    //Testing in Safari desktop browser
                    mutableCapabilities = new SafariOptions();
                }

                break;

            default:
                commonspec.getLogger().error("Unknown browser: " + capabilitiesMap.get("browserName"));
                throw new WebDriverException("Unknown browser: " + capabilitiesMap.get("browserName"));
        }

        //Assign all found capabilities found returned by the selenium node
        for (Map.Entry<String, String> entry : capabilitiesMap.entrySet()) {
            mutableCapabilities.setCapability(entry.getKey(), (Object) entry.getValue());
        }

        this.getCommonSpec().getLogger().debug("Setting RemoteWebDriver with capabilities %s", mutableCapabilities.toJson().toString());
        commonspec.setDriver(new RemoteWebDriver(new URL(grid), mutableCapabilities));
        commonspec.getDriver().manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT, TimeUnit.SECONDS);
        commonspec.getDriver().manage().timeouts().implicitlyWait(IMPLICITLY_WAIT, TimeUnit.SECONDS);
        commonspec.getDriver().manage().timeouts().setScriptTimeout(SCRIPT_TIMEOUT, TimeUnit.SECONDS);

    }

    /**
     * Makes use of WebDriverManager to automatically download the appropriate local driver
     */
    private void useLocalDriver() throws JsonProcessingException {

        MutableCapabilities mutableCapabilities = null;

        String[] arguments = System.getProperty("SELENIUM_ARGUMENTS", "--ignore-certificate-errors;--no-sandbox").split(";");
        String capabilities = System.getProperty("SELENIUM_CAPABILITIES", "{ \"browserName\": \"chrome\" }");

        Map<String, String> capabilitiesMap = null;
        ObjectMapper mapper = new ObjectMapper();
        capabilitiesMap = mapper.readValue(capabilities, Map.class);

        switch (capabilitiesMap.getOrDefault("browserName", "Chrome").toLowerCase()) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();

                commonspec.getLogger().debug("Configuring chrome desktop with arguments {} ", String.join(";", arguments));
                for (String argument: arguments) {
                    chromeOptions.addArguments(argument);
                }
                mutableCapabilities = new ChromeOptions();
                mutableCapabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
                System.setProperty("webdriver.chrome.silentOutput", "true"); //removes logging messages
                WebDriverManager.chromedriver().setup();
                driver = new ChromeDriver(chromeOptions);
                break;

            case "opera":
                OperaOptions operaOptions = new OperaOptions();

                commonspec.getLogger().debug("Configuring chrome desktop with arguments {} ", String.join(";", arguments));
                for (String argument: arguments) {
                    operaOptions.addArguments(argument);
                }
                mutableCapabilities = new OperaOptions();
                mutableCapabilities.setCapability(OperaOptions.CAPABILITY, operaOptions);
                System.setProperty("webdriver.opera.silentOutput", "true"); //removes logging messages
                WebDriverManager.operadriver().setup();
                driver = new OperaDriver(operaOptions);
                break;

            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                commonspec.getLogger().debug("Configuring edge desktop with arguments {} ", String.join(";", arguments));
                System.setProperty("webdriver.edge.silentOutput", "true"); //removes logging messages
                WebDriverManager.edgedriver().setup();
                driver = new EdgeDriver(edgeOptions);
                break;

            case "ie":
                InternetExplorerOptions ieOptions = new InternetExplorerOptions();
                commonspec.getLogger().debug("Configuring Internet Explorer desktop with arguments {} ", String.join(";", arguments));
                System.setProperty("webdriver.edge.silentOutput", "true"); //removes logging messages
                ieOptions.setCapability("ignoreZoomSetting", true);
                WebDriverManager.iedriver().setup();
                driver = new InternetExplorerDriver(ieOptions);
                break;

            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();

                commonspec.getLogger().debug("Configuring firefox desktop with arguments {} ", String.join(";", arguments));
                for (String argument: arguments) {
                    firefoxOptions.addArguments(argument);
                }
                mutableCapabilities = new FirefoxOptions();
                mutableCapabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, firefoxOptions);
                System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true"); //removes logging messages
                System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");  //removes logging messages
                WebDriverManager.firefoxdriver().setup();
                driver = new FirefoxDriver(firefoxOptions);
                break;

            default:
                commonspec.getLogger().error("Unknown browser: " + capabilitiesMap.get("browserName") + ". For using local browser, only Chrome/Firefox/Opera are supported");
                throw new WebDriverException("Unknown browser: " + capabilitiesMap.get("browserName") + ". For using local browser, only Chrome/Firefox/Opera are supported");
        }

        this.getCommonSpec().getLogger().debug("Setting local driver with capabilities %s", mutableCapabilities.toJson().toString());
        commonspec.setDriver(driver);
        commonspec.getDriver().manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT, TimeUnit.SECONDS);
        commonspec.getDriver().manage().timeouts().implicitlyWait(IMPLICITLY_WAIT, TimeUnit.SECONDS);
        commonspec.getDriver().manage().timeouts().setScriptTimeout(SCRIPT_TIMEOUT, TimeUnit.SECONDS);

    }

    /**
     * If the feature has the @web or @mobile annotation, closes selenium web driver after each scenario is completed.
     * @param scenario      Instance of the scenario just executed
     * @throws IOException  The IOException
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
     * */
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
