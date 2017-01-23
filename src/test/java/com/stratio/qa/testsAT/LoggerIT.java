package com.stratio.qa.testsAT;

import com.stratio.cucumber.testng.CucumberRunner;
import com.stratio.tests.utils.BaseGTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CucumberOptions(features = {
            "src/test/resources/features/titlesReplacements.feature",
            "src/test/resources/features/logger.feature",
            "src/test/resources/features/backgroundlogger.feature",
            "src/test/resources/features/outlineReplacements.feature",
    })
    public class LoggerIT extends BaseGTest{

        @Test (expectedExceptions = {})
        public void simpleTest() throws Exception {
            new CucumberRunner(this.getClass()).runCukes();
        }
    }
