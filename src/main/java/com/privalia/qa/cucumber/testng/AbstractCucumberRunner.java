package com.privalia.qa.cucumber.testng;

import cucumber.api.testng.AbstractTestNGCucumberTests;
import cucumber.api.testng.CucumberFeatureWrapper;
import cucumber.api.testng.PickleEventWrapper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This is a custom implementation of {@link AbstractTestNGCucumberTests} that makes use of the custom {@link CucumberRunner}
 * class. Test classes must extend this class in order to be executed with TestNG
 *</p>
 * @author Jose Fernandez
 */
public class AbstractCucumberRunner {

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
}
