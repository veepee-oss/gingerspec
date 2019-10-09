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
import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.internal.ApacheHttpClient;
import org.openqa.selenium.remote.internal.HttpClientFactory;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.fail;

/**
 * This class contains functions that are executed before and after each test.
 * For this, it makes use of cucumber hooks
 *
 * @see <a href="https://cucumber.io/docs/cucumber/api/#hooks">https://cucumber.io/docs/cucumber/api/#hooks</a>
 * @author Jose Fernandez
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
     * @param spec  commonG object
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
     * @throws MalformedURLException    MalformedURLException
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
        commonspec.setBrowserName(browser);
        commonspec.getLogger().debug("Setting up selenium for {}", browser);

        MutableCapabilities capabilities = null;


        switch (browser.toLowerCase()) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--ignore-certificate-errors");
                capabilities = new ChromeOptions();
                capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

                if (grid.matches("local")) {
                    System.setProperty("webdriver.chrome.silentOutput", "true"); //removes logging messages
                    WebDriverManager.chromedriver().setup();
                    driver = new ChromeDriver(chromeOptions);
                }

                break;
            case "firefox":
                capabilities = new FirefoxOptions();

                if (grid.matches("local")) {
                    System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true"); //removes logging messages
                    System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");  //removes logging messages
                    WebDriverManager.firefoxdriver().setup();
                    driver = new FirefoxDriver(capabilities);
                }

                break;
            case "phantomjs":
                capabilities = DesiredCapabilities.phantomjs();

                if (grid.matches("local")) {
                    //TODO add support for local phantonjs driver
                }

                break;
            case "iphone":
            case "safari":
                capabilities = DesiredCapabilities.iphone();
                capabilities.setCapability("platformName", "iOS");
                capabilities.setCapability("platformVersion", "8.1");
                capabilities.setCapability("deviceName", "iPhone Simulator");
                break;
            case "android":
                capabilities = DesiredCapabilities.android();
                capabilities.setCapability("platformName", "Android");
                capabilities.setCapability("platformVersion", "6.0");
                capabilities.setCapability("deviceName", "Android Emulator");
                capabilities.setCapability("app", "Browser");
                break;
            default:
                commonspec.getLogger().error("Unknown browser: " + browser);
                throw new WebDriverException("Unknown browser: " + browser);
        }

        if (grid.matches("local")) {

            commonspec.setDriver(driver);

        } else {

            grid = "http://" + grid + "/wd/hub";
            HttpClient.Factory factory = new ApacheHttpClient.Factory(new HttpClientFactory(60000, 60000));
            HttpCommandExecutor executor = new HttpCommandExecutor(new HashMap<String, CommandInfo>(), new URL(grid), factory);
            executor.setLocalLogs(null);
            commonspec.setDriver(new RemoteWebDriver(executor, capabilities));

        }

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
     * @throws Exception    Exception
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
     * @throws IOException  IOException
     */
    @After(order = 10, value = "@rest")
    public void restClientTeardown() throws IOException {
        commonspec.getLogger().debug("Shutting down REST client");
        commonspec.getClient().close();

    }

    /**
     * Disconnect any remaining open SSH connection after each scenario is completed
     * @throws Exception    Exception
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
     * @throws Exception    Exception
     */
    @After(value = "@sql")
    public void sqlConnectionClose() throws Exception {
        if ((commonspec.getSqlClient() != null) && (commonspec.getSqlClient().connectionStatus())) {
            commonspec.getLogger().debug("Closing SQL remote connection");
            commonspec.getSqlClient().disconnect();
        }
    }
}
