package com.privalia.qa.BTests;

import com.privalia.qa.cucumber.testng.CucumberRunner;
import com.privalia.qa.data.BrowsersDataProvider;
import com.privalia.qa.utils.BaseTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

@CucumberOptions(
        features = {"src/test/resources/featuresB/seleniumPoolingSteps.feature"},
        glue = "com.privalia.qa.specs")
public class SeleniumPoolingStepsIT extends BaseTest {

    @Factory(dataProviderClass = BrowsersDataProvider.class, dataProvider = "availableUniqueBrowsers")
    public SeleniumPoolingStepsIT(String browser) {
        this.browser = browser;
    }

}
