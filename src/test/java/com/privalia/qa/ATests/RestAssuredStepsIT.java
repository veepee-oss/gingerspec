package com.privalia.qa.ATests;

import com.privalia.qa.cucumber.testng.CucumberRunner;
import com.privalia.qa.utils.BaseGTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;

@CucumberOptions(format = "json:target/cucumber.json", features = {
        "src/test/resources/features/restAssured.feature"},
        glue = "classpath:com/privalia/qa/specs/*")
public class RestAssuredStepsIT extends BaseGTest {

    @Test
    public void RestAssuredStepsIT() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
