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

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.privalia.qa.utils.ThreadProperty;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import io.appium.java_client.MobileDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileBrowserType;
import io.appium.java_client.remote.MobileCapabilityType;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.restassured.http.ContentType;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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

    public static final int ORDER_10 = 10;

    public static final int ORDER_20 = 20;

    public static final int PAGE_LOAD_TIMEOUT = 120;

    public static final int IMPLICITLY_WAIT = 10;

    public static final int SCRIPT_TIMEOUT = 30;

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
     * If the feature has the @web or @mobile annotation, creates a new selenium driver
     * before each scenario
     *
     * @throws MalformedURLException MalformedURLException
     */
    @Before(order = ORDER_10, value = {"@mobile or @web"})
    public void seleniumSetup() throws MalformedURLException {

        String grid = System.getProperty("SELENIUM_GRID");

        if (grid == null) {
            fail("Selenium grid not available");
        }

        String b = ThreadProperty.get("browser");

        if ("".equals(b)) {
            fail("Non available browsers");
        }

        String browser = b.split("_")[0];
        String version = b.split("_")[1];
        String platform = b.split("_")[2];
        commonspec.setBrowserName(browser);
        commonspec.getLogger().debug("Setting up selenium for {}", browser);

        if (grid.matches("local")) {
            this.useLocalDriver(browser);
        } else {
            this.useRemoteGrid(grid, browser, version, platform);
        }

    }

    /**
     * Connects to a remote selenium grid to execute the tests
     *
     * @param grid     Address of the remote selenium grid
     * @param browser  Browser type to use
     * @param version  Browser version (it can also be the platform version if mobile)
     * @param platform Platform (LINUX, ANDROID, etc)
     * @throws MalformedURLException MalformedURLException
     */
    private void useRemoteGrid(String grid, String browser, String version, String platform) throws MalformedURLException {

        MutableCapabilities capabilities = null;

        grid = "http://" + grid + "/wd/hub";
//        HttpClient.Factory factory = new ApacheHttpClient.Factory(new HttpClientFactory(60000, 60000));
//        HttpCommandExecutor executor = new HttpCommandExecutor(new HashMap<String, CommandInfo>(), new URL(grid), factory);
//        executor.setLocalLogs(null);

        switch (browser.toLowerCase()) {
            case "chrome":

                commonspec.getLogger().debug("Setting up selenium for chrome in {}", platform);

                if (platform.toLowerCase().matches("android")) {
                    //Testing in chrome for android
                    capabilities = DesiredCapabilities.android();
                    capabilities.setCapability("platformName", "Android");
                    capabilities.setCapability("automationName", "UiAutomator2");
                    capabilities.setCapability("deviceName", "Android Emulator");
                    capabilities.setCapability("browserName", "Chrome");
                    capabilities.setCapability(MobileCapabilityType.BROWSER_NAME, MobileBrowserType.CHROME);
                    commonspec.setDriver(new RemoteWebDriver(new URL(grid), capabilities));

                } else if (platform.toLowerCase().matches("ios")) {
                    //Testing in chrome for ios
                    //TODO

                } else {
                    //Testing in desktop version of chrome
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.addArguments("--no-sandbox");
                    chromeOptions.addArguments("--ignore-certificate-errors");
                    capabilities = new ChromeOptions();
                    capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
                    commonspec.setDriver(new RemoteWebDriver(new URL(grid), capabilities));
                    this.configureWebDriver(capabilities);
                }

                break;

            case "firefox":
                capabilities = new FirefoxOptions();
                commonspec.setDriver(new RemoteWebDriver(new URL(grid), capabilities));
                this.configureWebDriver(capabilities);
                break;

            case "phantomjs":
                capabilities = DesiredCapabilities.phantomjs();
                commonspec.setDriver(new RemoteWebDriver(new URL(grid), capabilities));
                this.configureWebDriver(capabilities);
                break;

            case "mobile":

                if (platform.toLowerCase().matches("android")) {
                    capabilities = new DesiredCapabilities();
                    capabilities.setCapability("platformName", "Android");
                    capabilities.setCapability("automationName", "UiAutomator2");
                    capabilities.setCapability("deviceName", "Android Emulator");
                    capabilities.setCapability("appPackage", "com.android.calculator2");
                    capabilities.setCapability("appActivity", "com.android.calculator2.Calculator");
                    //commonspec.setDriver(new AndroidDriver(new URL(grid), capabilities));

                    MobileDriver appDriver = new AndroidDriver(new URL(grid), capabilities);

                    MobileElement el1 = (MobileElement) appDriver.findElementById("com.android.calculator2:id/digit_2");
                    el1.click();

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }

                break;


            default:
                commonspec.getLogger().error("Unknown browser: " + browser);
                throw new WebDriverException("Unknown browser: " + browser);
        }

    }

    /**
     * Makes use of WebDriverManager to automatically download the appropriate local driver
     *
     * @param browser Browser type to use (chrome/firefox)
     */
    private void useLocalDriver(String browser) {

        MutableCapabilities capabilities = null;

        switch (browser.toLowerCase()) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--ignore-certificate-errors");
                capabilities = new ChromeOptions();
                capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
                System.setProperty("webdriver.chrome.silentOutput", "true"); //removes logging messages
                WebDriverManager.chromedriver().setup();
                driver = new ChromeDriver(chromeOptions);
                break;

            case "firefox":
                capabilities = new FirefoxOptions();
                System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true"); //removes logging messages
                System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");  //removes logging messages
                WebDriverManager.firefoxdriver().setup();
                driver = new FirefoxDriver(capabilities);
                break;

            default:
                commonspec.getLogger().error("Unknown browser: " + browser);
                throw new WebDriverException("Unknown browser: " + browser);
        }

        commonspec.setDriver(driver);
        this.configureWebDriver(capabilities);

    }

    private void configureWebDriver(MutableCapabilities capabilities) {

        commonspec.getDriver().manage().timeouts().pageLoadTimeout(PAGE_LOAD_TIMEOUT, TimeUnit.SECONDS);
        commonspec.getDriver().manage().timeouts().implicitlyWait(IMPLICITLY_WAIT, TimeUnit.SECONDS);
        commonspec.getDriver().manage().timeouts().setScriptTimeout(SCRIPT_TIMEOUT, TimeUnit.SECONDS);

        commonspec.getDriver().manage().deleteAllCookies();
        if (capabilities.getCapability("deviceName") == null) {
            commonspec.getDriver().manage().window().setSize(new Dimension(1440, 900));
        }
        commonspec.getDriver().manage().window().maximize();
    }


    /**
     * If the feature has the @web or @mobile annotation, closes selenium web driver after each scenario is completed.
     */
    @After(order = ORDER_20, value = {"@mobile or @web"})
    public void seleniumTeardown() {
        if (commonspec.getDriver() != null) {
            commonspec.getLogger().debug("Shutdown Selenium client");
            //commonspec.getDriver().close(); //causes the driver instance when using firefox
            commonspec.getDriver().quit();
        }
    }

    /**
     * Close logger.
     */
    @After(order = 0)
    public void teardown() {
    }

    /**
     * If the feature has the @rest annotation, creates a new REST client before each scenario
     *
     * @throws Exception Exception
     */
    @Before(order = 10, value = "@rest")
    public void restClientSetup() throws Exception {
        commonspec.getLogger().debug("Starting a REST client");

        commonspec.setClient(new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setAcceptAnyCertificate(true).setAllowPoolingConnections(false)
                .build()));

        commonspec.setRestRequest(given().contentType(ContentType.JSON));

    }

    /**
     * If the feature has the @rest annotation, closes the REST client after each scenario is completed
     *
     * @throws IOException IOException
     */
    @After(order = 10, value = "@rest")
    public void restClientTeardown() throws IOException {
        commonspec.getLogger().debug("Shutting down REST client");
        commonspec.getClient().close();

    }

    /**
     * Disconnect any remaining open SSH connection after each scenario is completed
     *
     * @throws Exception Exception
     */
    @After(order = 10)
    public void remoteSSHConnectionTeardown() throws Exception {
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
