package com.privalia.qa.ATests;

import com.privalia.qa.cucumber.testng.CucumberRunner;
import com.privalia.qa.data.BrowsersDataProvider;
import com.privalia.qa.utils.BaseTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

@CucumberOptions(format = "json:target/cucumber.json",
        features = {"src/test/resources/features/seleniumPoolingSteps.feature"})
public class SeleniumPoolingStepsIT extends BaseTest {

    @Factory( dataProviderClass = BrowsersDataProvider.class, dataProvider = "availableUniqueBrowsers")
    public SeleniumPoolingStepsIT(String browser) {
        this.browser = browser;
    }

    @Test(enabled = true)
    public void SeleniumPoolingStepsTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }

}
