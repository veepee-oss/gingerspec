package com.privalia.qa.specs;

import com.privalia.qa.utils.BaseGTest;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = {"src/test/resources/features/mobile.feature"},
        glue = "com.privalia.qa.specs"
)
public class MobileGIT extends BaseGTest {


}
