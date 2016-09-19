package com.stratio.qa.testsAT;

import com.stratio.cucumber.testng.CucumberRunner;
import com.stratio.exceptions.NonReplaceableException;
import com.stratio.tests.utils.BaseGTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;

@CucumberOptions(features = {"src/test/resources/features/replacementFailures.feature"})
    public class FailedLoggerIT extends BaseGTest{

        @Test (expectedExceptions = {NonReplaceableException.class})
        public void simpleNegativeTest() throws Exception {
            new CucumberRunner(this.getClass()).runCukes();
        }
    }
