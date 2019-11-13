package com.privalia.qa.specs;

import cucumber.api.PendingException;
import cucumber.api.java.en.Given;

public class MobileGSpec extends BaseGSpec {

    /**
     * Generic constructor.
     *
     * @param spec object
     */
    public MobileGSpec(CommonG spec) {
        this.commonspec = spec;

    }



    @Given("^I open the application '(.*?)'$")
    public void iOpenTheApplication(String AppName) throws Throwable {
        this.commonspec.getMobileDriver().launchApp();
    }
}
