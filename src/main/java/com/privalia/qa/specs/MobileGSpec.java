package com.privalia.qa.specs;

import com.privalia.qa.assertions.Assertions;
import com.privalia.qa.cucumber.converter.NullableStringConverter;
import com.privalia.qa.utils.PreviousMobileElements;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.appium.java_client.MobileElement;
import org.openqa.selenium.Keys;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MobileGSpec extends BaseGSpec {

    /**
     * Generic constructor.
     *
     * @param spec object
     */
    public MobileGSpec(CommonG spec) {
        this.commonspec = spec;

    }


    /**
     * Launches the app, which was provided in the capabilities at session creation,
     * and (re)starts the session.
     */
    @Given("^I open the application$")
    public void iOpenTheApplication() {
        this.commonspec.getMobileDriver().launchApp();
    }

    /**
     * Click on an numbered {@code url} previously found mobile element.
     *
     * @param index                     Index of the Mobile element in the list
     */
    @And("^I click on the mobile element on index '(.*?)'$")
    public void iClickOnTheElementWithIdId(Integer index) {

        assertThat(commonspec.getPreviousMobileElements().getPreviousMobileElements().size()).as("There are less found mobile elements than required")
                .isGreaterThanOrEqualTo(index + 1);
        this.commonspec.getPreviousMobileElements().getPreviousMobileElements().get(index).click();

    }

    /**
     * Close the app which was provided in the capabilities at session creation
     * and quits the session.
     */
    @Given("^I close the application$")
    public void iCloseTheApplication() {
        this.commonspec.getMobileDriver().closeApp();
    }

    /**
     * Checks if {@code expectedCount} mobile elements are found, with a location {@code method}.
     *
     * @param atLeast       asserts that the amount of elements if greater or equal to expectedCount. If null, asserts the amount of element is equal to expectedCount
     * @param expectedCount the expected count of elements to find
     * @param method        method to locate the elements (id, class, css, xpath, name, tagName, linkText, partialLinkText)
     * @param element       the element
     * @throws ClassNotFoundException   the class not found exception
     * @throws NoSuchFieldException     the no such field exception
     * @throws SecurityException        the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException   the illegal access exception
     */
    @Then("^(at least )?'(\\d+?)' mobile elements? exists? with '([^:]*?):(.+?)'$")
    public void assertSeleniumNElementExists(String atLeast, Integer expectedCount, String method, String element) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        List<MobileElement> wel;

        if (atLeast != null) {
            wel = commonspec.locateMobileElement(method, element, -1);
            assertThat(wel.size()).as("Element count doesnt match").isGreaterThanOrEqualTo(expectedCount);
        } else {
            wel = commonspec.locateMobileElement(method, element, expectedCount);
        }

        PreviousMobileElements pwel = new PreviousMobileElements(wel);
        commonspec.setPreviousMobileElements(pwel);
    }


    /**
     * Type a {@code text} on an numbered {@code index} previously found mobile element.
     *
     * @param input      Text to write on the mobile element
     * @param index     Index of the mobile element in the list
     */
    @When("^I type '(.+?)' on the mobile element on index '(\\d+?)'$")
    public void seleniumType(String input, Integer index) {

        NullableStringConverter converter = new NullableStringConverter();
        String text = converter.transform(input);

        assertThat(commonspec.getPreviousMobileElements().getPreviousMobileElements().size()).as("There are less found mobile elements than required")
                .isGreaterThanOrEqualTo(index + 1);

        while (text.length() > 0) {
            if (-1 == text.indexOf("\\n")) {
                commonspec.getPreviousMobileElements().getPreviousMobileElements().get(index).sendKeys(text);
                text = "";
            } else {
                commonspec.getPreviousMobileElements().getPreviousMobileElements().get(index).sendKeys(text.substring(0, text.indexOf("\\n")));
                commonspec.getPreviousMobileElements().getPreviousMobileElements().get(index).sendKeys(Keys.ENTER);
                text = text.substring(text.indexOf("\\n") + 2);
            }
        }
    }


    /**
     * Verifies that a mobile element previously found has {@code text} as text
     *
     * @param index the index of the mobileelement
     * @param text  the text to verify
     */
    @Then("^the mobile element on index '(\\d+?)' has '(.+?)' as text$")
    public void assertSeleniumTextOnElementPresent(Integer index, String text) {
        assertThat(commonspec.getPreviousMobileElements().getPreviousMobileElements().size()).as("There are less found mobile elements than required")
                .isGreaterThanOrEqualTo(index + 1);

        Assertions.assertThat(commonspec.getPreviousMobileElements().getPreviousMobileElements().get(index)).contains(text);
    }

}
