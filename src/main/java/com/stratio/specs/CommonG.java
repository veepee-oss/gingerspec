package com.stratio.specs;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stratio.tests.utils.AerospikeUtil;
import com.stratio.tests.utils.AerospikeUtils;
import com.stratio.tests.utils.CassandraUtil;
import com.stratio.tests.utils.CassandraUtils;
import com.stratio.tests.utils.ElasticSearchUtil;
import com.stratio.tests.utils.ElasticSearchUtils;
import com.stratio.tests.utils.ExceptionList;
import com.stratio.tests.utils.MongoDBUtil;
import com.stratio.tests.utils.MongoDBUtils;
import com.stratio.tests.utils.ThreadProperty;

/**
 * @author Javier Delgado
 * @author Hugo Dominguez
 * 
 */
public class CommonG {

    private final Logger logger = LoggerFactory.getLogger(ThreadProperty.get("class"));

    private RemoteWebDriver driver = null;
    private String browserName = null;

    /**
     * Get the common logger.
     * 
     * @return
     */
    public Logger getLogger() {
        return this.logger;
    }

    /**
     * Get the exception list.
     * 
     * @return
     */
    public List<Exception> getExceptions() {
        return ExceptionList.INSTANCE.getExceptions();
    }

    /**
     * Get the cassandra utils.
     * 
     * @return
     */
    public CassandraUtils getCassandraClient() {
        return CassandraUtil.INSTANCE.getCassandraUtils();
    }

    /**
     * Get the elasticSearch utils.
     * 
     * @return
     */
    public ElasticSearchUtils getElasticSearchClient() {
        return ElasticSearchUtil.INSTANCE.getElasticSearchUtils();
    }

    /**
     * Get the Aerospike utils.
     * 
     * @return
     */
    public AerospikeUtils getAerospikeClient() {
        return AerospikeUtil.INSTANCE.getAeroSpikeUtils();
    }

    /**
     * Get the MongoDB utils.
     * 
     * @return
     */
    public MongoDBUtils getMongoDBClient() {
        return MongoDBUtil.INSTANCE.getMongoDBUtils();
    }

    /**
     * Get the remoteWebDriver.
     * 
     * @return
     */
    public RemoteWebDriver getDriver() {
        return driver;
    }

    /**
     * Set the remoteDriver.
     * 
     * @param driver
     */
    public void setDriver(RemoteWebDriver driver) {
        this.driver = driver;
    }

    /**
     * Get the browser name.
     * 
     * @return
     */
    public String getBrowserName() {
        return browserName;
    }

    /**
     * Set the browser name.
     * 
     * @param browserName
     */
    public void setBrowserName(String browserName) {
        this.browserName = browserName;
    }

    /**
     * Looks for webelements inside a selenium context. This search will be made by id, name and xpath expression
     * matching an {@code locator} value
     * 
     * @param element
     */
    public List<WebElement> locateElement(String element) {

        List<WebElement> wel = null;

        if (!element.contains(":")) {
            wel = this.locateSelenium(element);
        } else {
            String[] attrib = element.split(":");
            wel = this.locateCssSelector(attrib[0], attrib[1]);
        }
        return wel;
    }

    /**
     * Looks for webelements inside a selenium context. This search will be made by id, name and xpath expression
     * matching an {@code locator} value
     * 
     * @param element
     */
    private List<WebElement> locateSelenium(String element) {

        List<WebElement> we = new ArrayList<WebElement>();
        we = this.getDriver().findElements(By.id(element));
        if (we.size() == 0) {
            we = this.getDriver().findElements(By.name(element));
            if (we.size() == 0) {
                we = this.getDriver().findElements(By.xpath(element));
            }
        }

        return we;
    }

    /**
     * Looks for webelements inside a selenium context. This search will be made by a css selector using
     * {@code attribute} and {@code value} value
     * 
     * @param attribute
     * @param value
     */
    private List<WebElement> locateCssSelector(String attribute, String value) {
        List<WebElement> we = new ArrayList<WebElement>();
        we = this.getDriver().findElements(By.cssSelector("[" + attribute + "=\"" + value + "\"]"));

        return we;
    }

    /**
     * Replaces a placeholded element, starting with $$ with the corresponding java property
     * 
     * @param element
     */
    public String replacePlaceholders(String element) {
        String newVal = "";
        if (element.contains("${")) {
            String placeholder = element.substring(element.indexOf("${"), element.indexOf("}"));
            String modifier = "";
            String sysProp = "";
            if (placeholder.contains(".")) {
                sysProp = placeholder.substring(2, placeholder.indexOf("."));
                modifier = placeholder.substring(placeholder.indexOf(".") + 1, placeholder.length());
            } else {
                sysProp = placeholder.substring(2, placeholder.length());
            }

            newVal = System.getProperty(sysProp, "");
            if ("toLower".equals(modifier)) {
                newVal = newVal.toLowerCase();
            } else if ("toUpper".equals(modifier)) {
                newVal = newVal.toUpperCase();
            }
        } else {
            newVal = element;
        }
        return newVal;
    }
}
