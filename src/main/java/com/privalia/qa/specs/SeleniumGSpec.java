package com.privalia.qa.specs;

import com.privalia.qa.utils.PreviousWebElements;
import cucumber.api.java.en.Then;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Generic Selenium Specs.
 * @see <a href="SeleniumGSpec-annotations.html">Selenium Steps &amp; Matching Regex</a>
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


    /**
     * Checks that a web elements exists in the page and is of the type specified. This method is similar to {@link ThenGSpec#assertSeleniumNElementExists(Integer, String, String)}
     * but implements a pooling mechanism with a maximum pooling time instead of a static wait
     * @param poolingInterval   Time between consecutive condition evaluations
     * @param poolMaxTime       Maximum time to wait for the condition to be true
     * @param elementsCount     integer. Expected number of elements.
     * @param method            class of element to be searched
     * @param element           webElement searched in selenium context
     * @param type              The expected style of the element: visible, clickable, present, hidden
     * @throws Throwable
     */
    @Then("^I check every '(\\d+)' seconds for at least '(\\d+)' seconds until '(\\d+)' elements exists with '([^:]*?):([^:]*?)' and is '(visible|clickable|present|hidden)'$")
    public void waitWebElementWithPooling(int poolingInterval, int poolMaxTime, int elementsCount, String method, String element, String type) throws Throwable {
        List<WebElement> wel = commonspec.locateElementWithPooling(poolingInterval, poolMaxTime, method, element, elementsCount, type);
        PreviousWebElements pwel = new PreviousWebElements(wel);
        commonspec.setPreviousWebElements(pwel);
    }

    /**
     * Checks if an alert message is open in the current page. The function implements a pooling interval to check if the condition is true
     * @param poolingInterval   Time between consecutive condition evaluations
     * @param poolMaxTime       Maximum time to wait for the condition to be true
     * @throws Throwable
     */
    @Then("^I check every '(\\d+)' seconds for at least '(\\d+)' seconds until an alert appears$")
    public void waitAlertWithPooling(int poolingInterval, int poolMaxTime) throws Throwable {
        Alert alert = commonspec.waitAlertWithPooling(poolingInterval, poolMaxTime);
        commonspec.setSeleniumAlert(alert);
    }

    /**
     * Accepts an alert message previously found
     * @throws Throwable
     */
    @Then("^I dismiss the alert$")
    public void iAcceptTheAlert() throws Throwable {
        commonspec.dismissSeleniumAlert();
    }

    /**
     * Dismiss an alert message previously found
     * @throws Throwable
     */
    @Then("^I accept the alert$")
    public void iDismissTheAlert() throws Throwable {
        commonspec.acceptSeleniumAlert();
    }
}
