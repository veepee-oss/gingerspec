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

package com.stratio.qa.specs;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.stratio.qa.exceptions.DBException;
import com.stratio.qa.utils.ThreadProperty;
import com.thoughtworks.selenium.SeleniumException;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.internal.ApacheHttpClient;
import org.openqa.selenium.remote.internal.HttpClientFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.fail;

public class HookGSpec extends BaseGSpec {

    public static final int ORDER_10 = 10;

    public static final int ORDER_20 = 20;

    public static final int PAGE_LOAD_TIMEOUT = 120;

    public static final int IMPLICITLY_WAIT = 10;

    public static final int SCRIPT_TIMEOUT = 30;

    /**
     * Default constructor.
     *
     * @param spec
     */
    public HookGSpec(CommonG spec) {
        this.commonspec = spec;
    }

    /**
     * Clean the exception list.
     */
    @Before(order = 0)
    public void globalSetup() {
        commonspec.getExceptions().clear();
    }


    /**
     * Connect to selenium.
     *
     * @throws MalformedURLException
     */
    @Before(order = ORDER_10, value = {"@mobile,@web"})
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

        DesiredCapabilities capabilities = null;

        switch (browser.toLowerCase()) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--ignore-certificate-errors");
                capabilities = DesiredCapabilities.chrome();
                capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
                break;
            case "firefox":
                capabilities = DesiredCapabilities.firefox();
                break;
            case "phantomjs":
                capabilities = DesiredCapabilities.phantomjs();
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
                throw new SeleniumException("Unknown browser: " + browser);
        }

        capabilities.setVersion(version);

        grid = "http://" + grid + "/wd/hub";
        HttpClient.Factory factory = new ApacheHttpClient.Factory(new HttpClientFactory(60000, 60000));
        HttpCommandExecutor executor = new HttpCommandExecutor(new HashMap<String, CommandInfo>(), new URL(grid), factory);
        commonspec.setDriver(new RemoteWebDriver(executor, capabilities));
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
     * Close selenium web driver.
     */
    @After(order = ORDER_20, value = {"@mobile,@web"})
    public void seleniumTeardown() {
        if (commonspec.getDriver() != null) {
            commonspec.getLogger().debug("Shutdown Selenium client");
            commonspec.getDriver().close();
            commonspec.getDriver().quit();
        }
    }

    /**
     * Close logger.
     */
    @After(order = 0)
    public void teardown() {
    }

    @Before(order = 10, value = "@rest")
    public void restClientSetup() throws Exception {
        commonspec.getLogger().debug("Starting a REST client");

        commonspec.setClient(new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setAcceptAnyCertificate(true).setAllowPoolingConnections(false)
                .build()));
    }

    @After(order = 10, value = "@rest")
    public void restClientTeardown() throws IOException {
        commonspec.getLogger().debug("Shutting down REST client");
        commonspec.getClient().close();
    }

    @After(order = 10)
    public void remoteSSHConnectionTeardown() throws Exception {
        if (commonspec.getRemoteSSHConnection() != null) {
            commonspec.getLogger().debug("Closing SSH remote connection");
            commonspec.getRemoteSSHConnection().getSession().disconnect();
        }
    }
}
