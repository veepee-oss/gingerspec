package com.privalia.qa.BTests;

import com.privalia.qa.utils.BaseGTest;
import cucumber.api.CucumberOptions;

@CucumberOptions(
        features = {"src/test/resources/featuresB/soapServiceSteps.feature"},
        glue = "com.privalia.qa.specs")
public class SoapServicesStepsIT extends BaseGTest {

}
