package com.privalia.qa.ATests;

import com.privalia.qa.cucumber.testng.CucumberRunner;
import com.privalia.qa.utils.BaseGTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;

@CucumberOptions(format = "json:target/SoapServicesStepsIT.json", features = {
        "src/test/resources/features/soapServiceSteps.feature"},
        glue = "classpath:com/privalia/qa/specs/*")
public class SoapServicesStepsIT extends BaseGTest {

    @Test
    public void SoapServicesStepsIT() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
