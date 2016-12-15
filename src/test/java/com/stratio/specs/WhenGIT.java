package com.stratio.specs;

import com.stratio.cucumber.testng.CucumberRunner;
import com.stratio.tests.utils.BaseGTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;

@CucumberOptions(features = {"src/test/resources/features/createJSONFile.feature"})
public class WhenGIT extends BaseGTest {

    @Test
    public void createFileTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
