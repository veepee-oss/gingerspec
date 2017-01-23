package com.stratio.qa.ATests;

import com.stratio.qa.cucumber.testng.CucumberRunner;
import com.stratio.qa.utils.BaseGTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;

@CucumberOptions(features = {"src/test/resources/features/authentificationClusterIT.feature"})
public class AuthentificationClusterIT extends BaseGTest {
    @Test
    public void simpleTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
