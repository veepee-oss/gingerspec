package com.stratio.specs;

import com.stratio.cucumber.testng.CucumberRunner;
import com.stratio.tests.utils.BaseGTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;

@CucumberOptions(features = {"src/test/resources/features/executeCommand.feature"})
public class GivenGIT extends BaseGTest {

    @Test (expectedExceptions = {})
    public void simpleTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
