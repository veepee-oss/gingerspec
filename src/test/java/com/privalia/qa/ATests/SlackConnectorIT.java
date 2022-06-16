package com.privalia.qa.ATests;

import com.privalia.qa.utils.BaseGTest;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(plugin = {
        "com.privalia.qa.cucumber.reporter.gingerHtmlFormatter:target/documentation",
},
        features = {"src/test/resources/features/slack.feature"},
        glue = "com.privalia.qa.specs")
public class SlackConnectorIT extends BaseGTest {
}
