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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.privalia.qa.utils.JiraConnector;
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
import org.apache.logging.log4j.core.config.Configurator;
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
import org.testng.SkipException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static io.restassured.RestAssured.given;


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

    JiraConnector jiraConnector = new JiraConnector();

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
     * @param scenario  scenario reference
     */
    @Before(order = 0)
    public void globalSetup(Scenario scenario) {
        /*Removes unnecessary logging messages that are produced by dependencies that make use of java.util.logging.Logger*/
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(Level.WARNING);

        /*Clears the exceptions stacktrace for the new test*/
        commonspec.getExceptions().clear();

        /*Get list of tags present in the Scenario*/
        Collection<String> tags = scenario.getSourceTagNames();
        String ticket = this.jiraConnector.getFirstTicketReference(new ArrayList(tags));

        /*If theres a jira ticket in the scenario*/
        if (ticket != null) {
            try {
                if (!jiraConnector.entityShouldRun(ticket)) {
                    String message = String.format("Scenario skipped!, it is in a non runnable status in Jira (check %s/browse/%s)", jiraConnector.getProperty("jira.server.url", null), ticket);
                    scenario.log(message);
                    throw new SkipException(message);
                }
            } catch (SkipException se) {
                throw se;
            } catch (Exception e) {
                LOGGER.error("Could not retrieve info of ticket " + ticket + " from jira: " + e.getMessage() + ". Proceeding with execution...");
            }

        }
    }

    /**
     * If the feature has the @web annotation, creates a new selenium web driver
     * before each scenario. This method allows the configuration of certain capabilities
     * for the initialization of the driver, specifically, the ones used by selenium for
     * browser selection: browserName, platform, version. These variables should be passed
     * as VM arguments (-DbrowserName, -Dplatform, -Dversion).
     *
     * Additionally, then using a selenium grid (-DSELENIUM_GRID), the user can use the
     * VM argument -DCAPABILITIES=/path/to/capabilities.json, to override the default capabilities
     * with the ones from the json file
     *
     * @throws MalformedURLException MalformedURLException
     */
    @Before(order = 10, value = "@web")
    public void seleniumSetup() throws Exception {

        MutableCapabilities mutableCapabilities = null;
        ObjectMapper mapper = new ObjectMapper();
        boolean isLocal = ((System.getProperty("SELENIUM_GRID") != null) ? false : true);
        String[] arguments = System.getProperty("SELENIUM_ARGUMENTS", "--ignore-certificate-errors;--no-sandbox").split(";");

        switch (System.getProperty("browserName", "chrome").toLowerCase()) {
            case "chrome":

                mutableCapabilities = DesiredCapabilities.chrome();

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

            case "microsoftedge":
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
                commonspec.getLogger().error("Unknown browser: " + System.getProperty("browserName") + ". For using local browser, only Chrome/Opera/MicrosoftEdge/IE/Firefox/Safari are supported");
                throw new WebDriverException("Unknown browser: " + System.getProperty("browserName") + ". For using local browser, only Chrome/Opera/MicrosoftEdge/IE/Firefox/Safari are supported");
        }

        if (isLocal) {
            /**
             * Execute the tests using a local browser
             */
            this.getCommonSpec().getLogger().debug("Setting local driver with capabilities {}", mutableCapabilities.toJson().toString());
            commonspec.setDriver(driver);
        } else {
            /**
             * The user can provide the variables "platform", "version" and "platformName" in case the default capabilities need to be changed
             */
            if (System.getProperty("platform") != null) {
                mutableCapabilities.setCapability("platform", System.getProperty("platform"));
            }
            if (System.getProperty("version") != null) {
                mutableCapabilities.setCapability("version", System.getProperty("version"));
            }

            /*
            If the user includes the VM argument -DCAPABILITIES=/path/to/capabilities.json, the capabilities
            from that file will be used instead of the default ones
             */
            if (System.getProperty("CAPABILITIES") != null) {
                this.commonspec.getLogger().debug("Using capabilities from file: " + System.getProperty("CAPABILITIES"));
                mutableCapabilities = new MutableCapabilities();
                this.addCapabilitiesFromFile(System.getProperty("CAPABILITIES"), mutableCapabilities);
            }

            this.getCommonSpec().getLogger().debug("Setting RemoteWebDriver with capabilities {}", mutableCapabilities.toJson().toString());
            commonspec.setDriver(new RemoteWebDriver(new URL(System.getProperty("SELENIUM_GRID")), mutableCapabilities));
        }

        commonspec.getDriver().manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT, TimeUnit.SECONDS);
        commonspec.getDriver().manage().timeouts().implicitlyWait(IMPLICITLY_WAIT, TimeUnit.SECONDS);
        commonspec.getDriver().manage().timeouts().setScriptTimeout(SCRIPT_TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * If the feature has the @mobile annotation, creates a new Appium driver
     * before each scenario. By default, the system will try to create a set of default
     * capabilities for the driver. However, the user can set any of the
     * <a href="https://appium.io/docs/en/writing-running-appium/caps/">general capabilities</a> of Appium
     * (i.e. -Dapp=/path/to/app, -DplatformName=android, etc).
     *
     * The user can use the VM argument -DCAPABILITIES=/path/to/capabilities.json, to
     * override the default capabilities with the ones from the json file
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
            grid = "http://127.0.0.1:4723/wd/hub";
            this.getCommonSpec().getLogger().warn("Appium server not specified!");
            this.getCommonSpec().getLogger().warn("Use VM argument -DSELENIUN_GRID=<url> to set the appium server. Using SELENIUN_GRID=" + grid);
        }

        /**
         * These Capabilities span multiple drivers.
         * If you need to pass more or more specific capabilities, use -DCAPABILITIES=/path/to/capabilities.json
         */
        String[] generalCaps = {"automationName", "platformName", "platformVersion", "deviceName", "app", "otherApps", "browserName", "newCommandTimeout", "language",
            "locale", "udid", "orientation", "autoWebview", "noReset", "fullReset", "eventTimings", "enablePerformanceLogging", "printPageSourceOnFindFailure", "clearSystemFiles"};
        for (String cap : generalCaps) {
            if (System.getProperty(cap) != null) {
                capabilities.setCapability(cap, System.getProperty(cap));
            }
        }

        if (capabilities.getCapability("platformName") == null) {
            commonspec.getLogger().warn("No platformName capability found, using Android......");
            capabilities.setCapability("platformName", "Android");
        }

        this.getCommonSpec().getLogger().debug("Setting MobileWebDriver with capabilities {}", capabilities.toJson().toString());

        switch (capabilities.getCapability("platformName").toString().toLowerCase()) {

            case "android":
                if (System.getProperty("automationName") != null) {
                    capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, System.getProperty("automationName"));
                } else {
                    commonspec.getLogger().warn("Using default automationName=UiAutomator2 for Android. Change this by using -DautomationName='<name>'");
                    capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "UiAutomator2");
                }
                if (System.getProperty("deviceName") != null) {
                    capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, System.getProperty("deviceName"));
                }

                /*
                If the user includes the VM argument -DCAPABILITIES=/path/to/capabilities.json, the capabilities
                from that file will be used instead of the default ones
                */
                if (System.getProperty("CAPABILITIES") != null) {
                    this.commonspec.getLogger().debug("Using capabilities from file: " + System.getProperty("CAPABILITIES"));
                    capabilities = new MutableCapabilities();
                    this.addCapabilitiesFromFile(System.getProperty("CAPABILITIES"), capabilities);
                }

                commonspec.getLogger().debug("Building AndroidDriver with capabilities {}", capabilities.toJson().toString());
                commonspec.setDriver(new AndroidDriver(new URL(grid), capabilities));
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

                /*
                If the user includes the VM argument -DCAPABILITIES=/path/to/capabilities.json, the capabilities
                from that file will be used instead of the default ones
                */
                if (System.getProperty("CAPABILITIES") != null) {
                    this.commonspec.getLogger().debug("Using capabilities from file: " + System.getProperty("CAPABILITIES"));
                    capabilities = new MutableCapabilities();
                    this.addCapabilitiesFromFile(System.getProperty("CAPABILITIES"), capabilities);
                }

                commonspec.getLogger().debug("Building IOSDriver with capabilities {}", capabilities.toJson().toString());
                commonspec.setDriver(new IOSDriver(new URL(grid), capabilities));
                break;

            default:
                commonspec.getLogger().error("Unknown platformName: {}, only android/ios is allowed", capabilities.getCapability("platformName").toString());
                throw new WebDriverException("Unknown platformName: " + capabilities.getCapability("platformName").toString() + ", only android/ios is allowed");
        }

    }

    public void addCapabilitiesFromFile(String filePath, MutableCapabilities capabilities) throws IOException {

        Map<String, Object> capsMap;
        ObjectMapper mapper = new ObjectMapper();
        capsMap = mapper.readValue(Files.readAllBytes(Paths.get(filePath)), Map.class);

        for (Map.Entry<String, Object> entry : capsMap.entrySet()) {
            capabilities.setCapability(entry.getKey(), entry.getValue());
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
     * Will check if the scenario contains any reference to a Jira ticket and will try to update
     * its status based on the result of the scenario execution. It will also try to close any remaining
     * SSH connection
     * @param scenario  Scenario
     */
    @After(order = 10)
    public void teardown(Scenario scenario) {

        if (scenario.isFailed()) {
            Collection<String> tags = scenario.getSourceTagNames();
            String ticket = this.jiraConnector.getFirstTicketReference(new ArrayList(tags));

            if (ticket != null) {
                try {
                    commonspec.getLogger().debug("Updating ticket " + ticket + " in Jira...");
                    this.jiraConnector.transitionEntity(ticket);

                    String fileLocation = new File(scenario.getUri()).getPath();
                    this.jiraConnector.postCommentToEntity(ticket, "Scenario '" + scenario.getName() + "' failed at: " + fileLocation.substring(fileLocation.indexOf("/src") + 1).trim());

                } catch (Exception e) {
                    commonspec.getLogger().warn("Could not change the status of entity " + ticket + " in jira: " + e.getMessage());
                }
            }
        }

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

    /**
     * Changes the logging level of the log4j logger used in the specs package to the given value
     * @param scenario  Scenario
     */
    @Before(value = "@debug or @trace or @info or @warn or @error or @fatal")
    public void activateLogLevel(Scenario scenario) {

        /*Get list of tags present in the Scenario*/
        Collection<String> tags = scenario.getSourceTagNames();

        for (String tag: tags) {
            if (tag.matches("(?i)@debug|@trace|@info|@warn|@error|@fatal")) {
                Configurator.setLevel("com.privalia.qa.specs", org.apache.logging.log4j.Level.getLevel(tag.replace("@", "").toUpperCase()));
            }
        }
    }

    /**
     * Returns logging level back to default value (WARN)
     */
    @After(value = "@debug or @trace or @info or @warn or @error or @fatal")
    public void deactivateLogLevel() {
        Configurator.setLevel("com.privalia.qa.specs", org.apache.logging.log4j.Level.getLevel(System.getProperty("logLevel", "WARN")));
    }

}
