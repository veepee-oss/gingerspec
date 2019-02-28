package com.privalia.qa.BTests;

import com.privalia.qa.utils.BaseGTest;
import cucumber.api.CucumberOptions;


@CucumberOptions(
        features = {"src/test/resources/featuresB/feature_test.feature"},
        glue = "com.privalia.qa.specs"
)
public class initialBIT extends BaseGTest {

}
