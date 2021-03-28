package com.privalia.qa.ATests;

import com.privalia.qa.utils.BaseGTest;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = {"src/test/resources/features/includeTag.feature"},
        glue = "com.privalia.qa.specs"
)
public class IncludeTagIT extends BaseGTest {

}
