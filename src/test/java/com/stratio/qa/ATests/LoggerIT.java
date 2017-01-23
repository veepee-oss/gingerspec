package com.stratio.qa.ATests;

import com.stratio.qa.cucumber.testng.CucumberRunner;
import com.stratio.qa.utils.BaseGTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;

@CucumberOptions(features = {
        "src/test/resources/features/titlesReplacements.feature",
        "src/test/resources/features/logger.feature",
        "src/test/resources/features/backgroundlogger.feature",
        "src/test/resources/features/outlineReplacements.feature",
})
public class LoggerIT extends BaseGTest {

    @Test(expectedExceptions = {})
    public void simpleTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
