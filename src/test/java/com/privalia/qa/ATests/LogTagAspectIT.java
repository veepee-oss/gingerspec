package com.privalia.qa.ATests;

import com.privalia.qa.cucumber.testng.CucumberRunner;
import com.privalia.qa.utils.BaseGTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;

@CucumberOptions(format = "json:target/cucumber.json", features = {
        "src/test/resources/features/logTagAspect.feature"},
        glue = "classpath:com/privalia/qa/specs/*")
public class LogTagAspectIT extends BaseGTest {

    @Test(expectedExceptions = {})
    public void simpleTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
