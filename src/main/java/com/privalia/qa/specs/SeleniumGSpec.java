package com.privalia.qa.specs;

import com.privalia.qa.utils.PreviousWebElements;
import cucumber.api.java.en.Then;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Class for all different steps related to Selenium
 */
public class SeleniumGSpec extends BaseGSpec {


    /**
     * Generic constructor.
     *
     * @param spec object
     */
    public SeleniumGSpec(CommonG spec) {
        this.commonspec = spec;

    }


    @Then("^I check every '(\\d+)' seconds for at least '(\\d+)' seconds until '(\\d+)' elements exists with '([^:]*?):([^:]*?)' and is '(visible|clickable|present|hidden)'$")
    public void waitWebElementWithPooling(int poolingInterval, int poolMaxTime, int elementsCount, String method, String element, String type) throws Throwable {

        List<WebElement> wel = commonspec.locateElementWithPooling(poolingInterval, poolMaxTime, method, element, elementsCount, type);
        PreviousWebElements pwel = new PreviousWebElements(wel);
        commonspec.setPreviousWebElements(pwel);

    }

    @Then("^I check every '(\\d+)' seconds for at least '(\\d+)' seconds until an alert appears$")
    public void waitAlertWithPooling(int poolingInterval, int poolMaxTime) throws Throwable {

        Alert alert = commonspec.waitAlertWithPooling(poolingInterval, poolMaxTime);
        commonspec.setSeleniumAlert(alert);
    }

    @Then("^I dismiss the alert$")
    public void iAcceptTheAlert() throws Throwable {
        commonspec.dismissSeleniumAlert();
    }

    @Then("^I accept the alert$")
    public void iDismissTheAlert() throws Throwable {
        commonspec.acceptSeleniumAlert();
    }
}
