package com.stratio.specs;

import static org.testng.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.stratio.tests.utils.ThreadProperty;
import com.thoughtworks.selenium.SeleniumException;

import cucumber.api.java.After;
import cucumber.api.java.Before;

public class HookGSpec extends BaseGSpec {

    public HookGSpec(CommonG spec) {
        this.commonspec = spec;
    }

    @Before(order = 0)
    public void globalSetup() {
        commonspec.getLogger().info("Clearing exception list");
        commonspec.getExceptions().clear();
    }

    @Before(order = 10, value = "@C*")
    public void cassandraSetup() {
        commonspec.getLogger().info("Setting up C* client");
        commonspec.getCassandraClient().connect();
    }

    @Before(order = 10, value = "@MongoDB")
    public void mongoSetup() {
        commonspec.getLogger().info("Setting up MongoDB client");
        commonspec.getMongoDBClient().connectToMongoDB();
    }

    @Before(order = 10, value = "@elasticsearch")
    public void elasticsearchSetup() {
        commonspec.getLogger().info("Setting up elasticsearch client");
        commonspec.getElasticSearchClient().connect();
    }

    @Before(order = 10, value = "@Aerospike")
    public void aerospikeSetup() {
        commonspec.getLogger().info("Setting up Aerospike client");
        commonspec.getAerospikeClient().connect();
    }

    @Before(order = 10, value = "@web")
    public void seleniumSetup() throws MalformedURLException {
        String b = ThreadProperty.get("browser");
        if ("".equals(b)) {
            fail("Non available browsers");
        }
        String browser = b.split("_")[0];
        String version = b.split("_")[1];
        commonspec.setBrowserName(browser);
        commonspec.getLogger().info("Setting up selenium for {}", browser);

        DesiredCapabilities capabilities = null;
        if (browser.toLowerCase().equals("chrome")) {
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("test-type");
            capabilities = DesiredCapabilities.chrome();
            capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        } else if (browser.toLowerCase().equals("firefox")) {
            capabilities = DesiredCapabilities.firefox();
        } else if (browser.toLowerCase().equals("phantomjs")) {
            capabilities = DesiredCapabilities.phantomjs();
        } else {
            commonspec.getLogger().error("Unknown browser: " + browser);
            throw new SeleniumException("Unknown browser: " + browser);
        }

        capabilities.setVersion(version);

        String grid = System.getProperty("SELENIUM.GRID", "127.0.0.1:4444");
        grid = "http://" + grid + "/wd/hub";
        commonspec.setDriver(new RemoteWebDriver(new URL(grid), capabilities));
        commonspec.getDriver().manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
        commonspec.getDriver().manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        commonspec.getDriver().manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);

        commonspec.getDriver().manage().deleteAllCookies();
        commonspec.getDriver().manage().window().maximize();

    }

    @After(order = 20, value = "@web")
    public void seleniumTeardown() {
        if (commonspec.getDriver() != null) {
            commonspec.getLogger().info("Shutdown Selenium client");
            commonspec.getDriver().close();
            commonspec.getDriver().quit();
        }
    }

    @After(order = 20, value = "@C*")
    public void cassandraTeardown() {
        commonspec.getLogger().info("Shutdown  C* client");
        commonspec.getCassandraClient().disconnect();
    }

    @After(order = 20, value = "@MongoDB")
    public void mongoTeardown() {
        commonspec.getLogger().info("Shutdown MongoDB client");
        commonspec.getMongoDBClient().disconnect();
    }

    @After(order = 20, value = "@elasticsearch")
    public void elasticsearchTeardown() {
        commonspec.getLogger().info("Shutdown elasticsearch client");
        commonspec.getElasticSearchClient().disconnect();
    }

    @After(order = 20, value = "@Aerospike")
    public void aerospikeTeardown() {
        commonspec.getLogger().info("Shutdown Aerospike client");
        commonspec.getAerospikeClient().disconnect();
    }

    @After(order = 0)
    public void teardown() {
        commonspec.getLogger().info("Ended running hooks");
    }
}