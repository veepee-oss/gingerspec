package com.stratio.qa.specs;

import com.stratio.qa.cucumber.testng.CucumberRunner;
import com.stratio.qa.utils.BaseGTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;

@CucumberOptions(features = {"src/test/resources/features/checkValue.feature"})
public class ThenGCheckValueIT extends BaseGTest {

    @Test(expectedExceptions = {})
    public void simpleTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
