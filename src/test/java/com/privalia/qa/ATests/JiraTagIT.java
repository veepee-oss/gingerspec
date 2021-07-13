package com.privalia.qa.ATests;

import com.privalia.qa.utils.BaseGTest;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = {
                "src/test/resources/features/jiraTag.feature",
        },
        glue = "com.privalia.qa.specs")
public class JiraTagIT extends BaseGTest {
}
