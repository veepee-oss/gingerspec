package com.stratio.specs;

import java.util.List;

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
}
