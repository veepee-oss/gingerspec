package com.privalia.qa.specs;

import com.privalia.qa.data.BrowsersDataProvider;
import com.privalia.qa.utils.BaseGTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Factory;

@CucumberOptions(
        features = {"src/test/resources/features/mobile.feature"},
        glue = "com.privalia.qa.specs"
)
public class MobileGIT extends BaseGTest {

    @Factory(dataProviderClass = BrowsersDataProvider.class, dataProvider = "availableUniqueBrowsers")
    public MobileGIT(String browser) {
        this.browser = browser;
    }

}
