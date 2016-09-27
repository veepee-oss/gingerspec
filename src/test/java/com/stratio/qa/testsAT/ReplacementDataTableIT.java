package com.stratio.qa.testsAT;

import com.stratio.cucumber.testng.CucumberRunner;
import com.stratio.tests.utils.BaseGTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;

@CucumberOptions(features = {
            "src/test/resources/features/replacementDataTable.feature",
    })
    public class ReplacementDataTableIT extends BaseGTest{

        @Test
        public void ReplacementDataTableIT() throws Exception {
            new CucumberRunner(this.getClass()).runCukes();
        }
    }
