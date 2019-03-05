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

package com.privalia.qa.utils;

import com.privalia.qa.cucumber.testng.CucumberRunner;
import cucumber.api.testng.AbstractTestNGCucumberTests;
import cucumber.api.testng.CucumberFeatureWrapper;
import cucumber.api.testng.PickleEventWrapper;
import cucumber.api.testng.TestNGCucumberRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.*;

import java.lang.reflect.Method;

/**
 * This is a custom implementation of {@link AbstractTestNGCucumberTests} that makes use of the custom {@link CucumberRunner}
 * class. Test classes must extend this class in order to be executed with TestNG
 *
 * @author Jose Fernandez
 */
public abstract class BaseGTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    /**
     * Use custom implementation of {@link TestNGCucumberRunner}
     */
    private CucumberRunner cucumberRunner;

    @BeforeClass(alwaysRun = true)
    public void setUpClass() throws Exception {
        cucumberRunner = new CucumberRunner(this.getClass());
    }

    @Test(groups = "cucumber", description = "Runs Cucumber Scenarios", dataProvider = "scenarios")
    public void runScenario(PickleEventWrapper pickleWrapper, CucumberFeatureWrapper featureWrapper) throws Throwable {
        // the 'featureWrapper' parameter solely exists to display the feature file in a test report
        cucumberRunner.runScenario(pickleWrapper.getPickleEvent());
    }

    /**
     * Returns two dimensional array of PickleEventWrapper scenarios with their associated CucumberFeatureWrapper feature.
     *
     * @return a two dimensional array of scenarios features.
     */
    @DataProvider
    public Object[][] scenarios() {
        if (cucumberRunner == null) {
            return new Object[0][0];
        }
        return cucumberRunner.provideScenarios();
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() throws Exception {
        if (cucumberRunner == null) {
            return;
        }
        cucumberRunner.finish();
    }

    /**
     * Method executed before a suite.
     *
     * @param context
     */
    @BeforeSuite(alwaysRun = true)
    public void beforeGSuite(ITestContext context) {
    }

    /**
     * Method executed after a suite.
     *
     * @param context
     */
    @AfterSuite(alwaysRun = true)
    public void afterGSuite(ITestContext context) {
        logger.info("Done executing this test-run.");
    }

    /**
     * Method executed before a test class.
     *
     * @param context
     */
    @BeforeClass(alwaysRun = true)
    public void beforeGClass(ITestContext context) {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
    }

    /**
     * Method executed after a test method.
     *
     * @param method
     */
    @BeforeMethod(alwaysRun = true)
    public void beforeGMethod(Method method) {
    }

    /**
     * Method executed before method.
     *
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
