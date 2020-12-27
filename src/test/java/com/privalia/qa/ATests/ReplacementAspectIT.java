package com.privalia.qa.ATests;

import com.privalia.qa.utils.BaseGTest;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(plugin = {
                "json:target/ReplacementAspectIT.json",
                "html:target/ReplacementAspectIT.html",
                "junit:target/ReplacementAspectIT.xml"
        },
        features = {"src/test/resources/features/replacementAspect.feature"},
        glue = "com.privalia.qa.specs")
public class ReplacementAspectIT extends BaseGTest {
}
