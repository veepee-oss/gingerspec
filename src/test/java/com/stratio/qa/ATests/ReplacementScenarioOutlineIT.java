package com.stratio.qa.ATests;

import com.stratio.qa.cucumber.testng.CucumberRunner;
import com.stratio.qa.utils.BaseGTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;

@CucumberOptions(features = {
        "src/test/resources/features/replacementScenarioOutline.feature",
})
public class ReplacementScenarioOutlineIT extends BaseGTest {

    @Test
    public void ReplacementDataTableIT() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
