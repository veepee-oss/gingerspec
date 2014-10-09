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

public class CommonG {

    private final Logger logger = LoggerFactory.getLogger(ThreadProperty.get("class"));

    private RemoteWebDriver driver = null;
    private String browserName = null;

    public Logger getLogger() {
        return this.logger;
    }

    public List<Exception> getExceptions() {
        return ExceptionList.INSTANCE.getExceptions();
    }

    public CassandraUtils getCassandraClient() {
        return CassandraUtil.INSTANCE.getCassandraUtils();
    }

    public ElasticSearchUtils getElasticSearchClient() {
        return ElasticSearchUtil.INSTANCE.getElasticSearchUtils();
    }

    public AerospikeUtils getAerospikeClient() {
        return AerospikeUtil.INSTANCE.getAeroSpikeUtils();
    }

    public MongoDBUtils getMongoDBClient() {
        return MongoDBUtil.INSTANCE.getMongoDBUtils();
    }

    public RemoteWebDriver getDriver() {
        return driver;
    }

    public void setDriver(RemoteWebDriver driver) {
        this.driver = driver;
    }

    public String getBrowserName() {
        return browserName;
    }

    public void setBrowserName(String browserName) {
        this.browserName = browserName;
    }
}
