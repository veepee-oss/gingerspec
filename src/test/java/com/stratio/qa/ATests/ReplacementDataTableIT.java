package com.stratio.qa.ATests;

import com.stratio.qa.cucumber.testng.CucumberRunner;
import com.stratio.qa.utils.BaseGTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;

@CucumberOptions(features = {
        "src/test/resources/features/replacementDataTable.feature",
})
public class ReplacementDataTableIT extends BaseGTest {

    @Test
    public void ReplacementDataTableIT() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
