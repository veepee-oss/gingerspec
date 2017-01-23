package com.stratio.qa.ATests;

import com.stratio.qa.cucumber.testng.CucumberRunner;
import com.stratio.qa.exceptions.NonReplaceableException;
import com.stratio.qa.utils.BaseGTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;

@CucumberOptions(features = {"src/test/resources/features/replacementFailure1.feature"})
public class FailedLogger1IT extends BaseGTest {

    @Test(expectedExceptions = {NonReplaceableException.class})
    public void simpleNegativeTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
