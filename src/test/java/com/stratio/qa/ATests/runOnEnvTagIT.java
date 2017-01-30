package com.stratio.qa.ATests;

import com.stratio.qa.cucumber.testng.CucumberRunner;
import com.stratio.qa.utils.BaseGTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;

@CucumberOptions(features = {
        "src/test/resources/features/runOnEnvTag.feature"
})
public class runOnEnvTagIT extends BaseGTest {

    @Test(expectedExceptions = {})
    public void runOnEnvTagTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
