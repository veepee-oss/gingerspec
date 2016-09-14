package com.stratio.qa.testsAT;

import com.stratio.cucumber.testng.CucumberRunner;
import com.stratio.tests.utils.BaseGTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;

@CucumberOptions(features = { "src/test/resources/features/logger.feature", "src/test/resources/features/backgroundlogger.feature" })
    public class LoggerIT extends BaseGTest{

        @Test
        public void simpleTest() throws Exception {
            new CucumberRunner(this.getClass()).runCukes();
        }
    }
