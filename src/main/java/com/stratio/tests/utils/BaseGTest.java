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

    final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    @BeforeSuite(alwaysRun = true)
    public void beforeGSuite(ITestContext context) {
    }

    @AfterSuite(alwaysRun = true)
    public void afterGSuite(ITestContext context) {
        logger.info("Done executing this test-run.");
    }

    @BeforeClass(alwaysRun = true)
    public void beforeGClass(ITestContext context) {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeGMethod(Method method) {
    }

    @AfterMethod(alwaysRun = true)
    public void afterGMethod(Method method) {
    }

    @AfterClass()
    public void afterGClass() {
    }
}
