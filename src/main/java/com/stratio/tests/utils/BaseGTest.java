package com.stratio.tests.utils;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

public abstract class BaseGTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    /**
     * Method executed before a suite.
     * @param context
     */
    @BeforeSuite(alwaysRun = true)
    public void beforeGSuite(ITestContext context) {
    }
    /**
     * Method executed after a suite.
     * @param context
     */
    @AfterSuite(alwaysRun = true)
    public void afterGSuite(ITestContext context) {
        logger.info("Done executing this test-run.");
    }
    /**
     * Method executed before a test class.
     * @param context
     */
    @BeforeClass(alwaysRun = true)
    public void beforeGClass(ITestContext context) {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
    }
    /**
     * Method executed after a test method.
     * @param method
     */
    @BeforeMethod(alwaysRun = true)
    public void beforeGMethod(Method method) {
    }
    /**
     * Method executed before method.
     * @param method
     */
    @AfterMethod(alwaysRun = true)
    public void afterGMethod(Method method) {
    }
    /**
     * Method executed before a class.
     */
    @AfterClass()
    public void afterGClass() {
    }
}
