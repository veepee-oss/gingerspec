/*
 * Copyright (c) 2021, Veepee
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose
 * with or without fee is hereby  granted, provided that the above copyright notice
 * and this permission notice appear in all copies.
 *
 * THE SOFTWARE  IS PROVIDED "AS IS"  AND THE AUTHOR DISCLAIMS  ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING  ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS.  IN NO  EVENT  SHALL THE  AUTHOR  BE LIABLE  FOR  ANY SPECIAL,  DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 * OF USE, DATA  OR PROFITS, WHETHER IN AN ACTION OF  CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR  IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package com.privalia.qa.specs;

import com.privalia.qa.cucumber.converter.ArrayListConverter;
import com.privalia.qa.cucumber.converter.NullableStringConverter;
import com.privalia.qa.utils.PreviousWebElements;
import com.privalia.qa.utils.ThreadProperty;
import io.cucumber.docstring.DocString;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.privalia.qa.assertions.Assertions.assertThat;

/**
 * Steps definitions for selenium (web application automation). Check the examples provided on
 * each method to know how to use them in your own Feature files.
 *
 * @see <a href="https://www.selenium.dev/">https://www.selenium.dev/</a>
 * @author José Fernández
 */
public class SeleniumGSpec extends BaseGSpec {

    private final String LOCATORS = "id|name|class|css|xpath|linkText|partialLinkText|tagName";


    /**
     * Generic constructor.
     *
     * @param spec object
     */
    public SeleniumGSpec(CommonG spec) {
        this.commonspec = spec;
    }


    /**
     * Set app host and port.
     * <p>
     * This is an initialization step. This is used as the first step in Selenium features to configure
     * the basepath url. This step does not directly take the user to the given url, just sets the base url that
     * is later used in {@link #seleniumBrowse(String, String)}. Notice that is not necessary to specify http://
     * or https://, this is automatically inferred when using {@link #seleniumBrowse(String, String)}, check this step
     * for more concrete examples.
     * <br>
     * You can also consider using {@link #iGoToUrl(String)} instead. This step just navigates the user to the given full
     * URL and can be easier to read in the gherkin files
     * <pre>{@code
     * Example:
     *
     * Scenario: setting http://demoqa.com:80 as basepath
     *      Given My app is running in 'demoqa.com:80'
     *      When I browse to '/login'
     * }</pre>
     *
     * @deprecated This method is deprecated, use {@link #iGoToUrl(String)} instead
     * @see #seleniumBrowse(String, String)
     * @see #iGoToUrl(String)
     * @param host host where app is running (i.e "localhost" or "localhost:443")
     */
    @Deprecated
    @Given("^My app is running in '(.*)'$")
    public void setupApp(String host) {
        assertThat(host).isNotEmpty();

        String port = ":80";

        assertThat(host).as("Malformed url. No need to use http(s):// prefix").doesNotContain("http://").doesNotContain("https://");
        String[] address = host.split(":");

        if (address.length == 2) {
            host = address[0];
            port = ":" + address[1];
        }

        commonspec.setWebHost(host);
        commonspec.setWebPort(port);

    }


    /**
     * Browse to url using the current browser.
     * <p>
     * The url is relative to the basepath configured with {@link SeleniumGSpec#setupApp(String)} method. You
     * can also use {@link #iGoToUrl(String)} to directly navigate to the given url.
     * <pre>{@code
     * Example:
     *
     * Scenario: Navigating to http://demoqa.com:80/login
     *      Given My app is running in 'demoqa.com:80'
     *      Then I browse to '/'
     *      Then I browse to '/login'
     *
     * Scenario: Navigating using https
     *      Given My app is running in 'mysecuresite.com:443'
     *      Then I securely browse to '/'
     * }</pre>
     *
     * @deprecated This method is deprecated, use {@link #iGoToUrl(String)} instead
     * @see #setupApp(String)
     * @see #iGoToUrl(String)
     * @param isSecured If the connection should be secured
     * @param path      path of running app
     */
    @Deprecated
    @Given("^I( securely)? browse to '(.*)'$")
    public void seleniumBrowse(String isSecured, String path) {
        assertThat(path).isNotEmpty();

        Assertions.assertThat(commonspec.getWebHost()).as("Web host has not been set. You may need to use the 'My app is running in...' step first").isNotNull();
        Assertions.assertThat(commonspec.getWebPort()).as("Web port has not been set. You may need to use the 'My app is running in...' step first").isNotNull();

        String protocol = "http://";
        if (isSecured != null) {
            protocol = "https://";
        }

        String webURL = protocol + commonspec.getWebHost() + commonspec.getWebPort();

        commonspec.getDriver().get(webURL + path);
        commonspec.setParentWindow(commonspec.getDriver().getWindowHandle());
    }


    /**
     * Checks that a web elements exists in the page and if it is of the type specified in the given time interval.
     * <p>
     * Elements found are internally stored to be used in subsequent steps in the same scenario.
     * This method is similar to {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * but implements a pooling mechanism with a maximum pooling time instead of a static wait
     *
     * <pre>{@code
     * Example:
     *
     * Scenario: Wait until the element is clickable
     *      Given I go to 'http://mydummysite.com/login'
     *      Then I check every '1' seconds for at least '10' seconds until '1' elements exists with 'id:loginbutton' and is 'clickable'
     *      And I click on the element on index '0'
     * }</pre>
     *
     * @see #seleniumBrowse(String, String)
     * @see #assertSeleniumNElementExists(String, Integer, String, String)
     * @see #seleniumClick(Integer)
     * @param poolingInterval Time between consecutive condition evaluations
     * @param poolMaxTime     Maximum time to wait for the condition to be true
     * @param elementsCount   integer. Expected number of elements.
     * @param method          class of element to be searched
     * @param element         webElement searched in selenium context
     * @param type            The expected style of the element: visible, clickable, present, hidden
     */
    @Then("^I check every '(\\d+)' seconds for at least '(\\d+)' seconds until '(\\d+)' elements exists with '(" + LOCATORS + "):(.*)' and is '(visible|clickable|present|hidden)'$")
    public void waitWebElementWithPooling(int poolingInterval, int poolMaxTime, int elementsCount, String method, String element, String type) {
        List<WebElement> wel = commonspec.locateElementWithPooling(poolingInterval, poolMaxTime, method, element, elementsCount, type);
        PreviousWebElements pwel = new PreviousWebElements(wel);
        commonspec.setPreviousWebElements(pwel);
    }


    /**
     * Waits for the given element to be present on the page
     * <p>
     * This step can be seen as a shorter and more compact version than {@link #waitWebElementWithPooling(int, int, int, String, String, String)}.
     * It is useful in cases where a given element may take time to appear (like when loading a new page). This step will check every second
     * for a max of 10 seconds. If the element is not found before 10 secs, the step fails. If you need to wait more time than 10 seconds
     * you can try using {@link #waitWebElementWithTime(int, String, String)}
     * <pre>{@code
     * Example:
     *
     * Scenario: Wait until the element is present
     *      Given I go to 'http://demoqa.com/text-box'
     *      And I wait until element with 'id:userName' is present
     *      And I type 'John' on the element with 'id:userName'
     * }</pre>
     *
     * @see #waitWebElementWithPooling(int, int, int, String, String, String)
     * @see #waitWebElementWithTime(int, String, String)
     * @param method        Method to locate the element (id, name, class, css, xpath, linkText, partialLinkText, tagName)
     * @param element       locator
     */
    @Then("^I wait until element with '(" + LOCATORS + "):(.*)' is present")
    public void waitWebElement(String method, String element) {
        List<WebElement> wel = commonspec.locateElementWithPooling(1, 10, method, element, 1, "present");
        PreviousWebElements pwel = new PreviousWebElements(wel);
        commonspec.setPreviousWebElements(pwel);
    }


    /**
     * Waits for the given element to be present on the page
     * <p>
     * Similar to {@link #waitWebElement(String, String)} but with the ability for configuring the time to wait.
     * <pre>{@code
     * Example:
     *
     * Scenario: Wait until the element is present
     *      Given I go to 'http://demoqa.com/text-box'
     *      Then I wait '10' seconds until element with 'id:userName' is present
     *      And I type 'John' on the element with 'id:userName'
     * }</pre>
     *
     * @see #waitWebElementWithPooling(int, int, int, String, String, String)
     * @see #waitWebElement(String, String)
     * @param maxTime       Max time to wait
     * @param method        Method to locate the element (id, name, class, css, xpath, linkText, partialLinkText, tagName)
     * @param element       locator
     */
    @Then("^I wait '(.*)' seconds until element with '(" + LOCATORS + "):(.*)' is present")
    public void waitWebElementWithTime(int maxTime, String method, String element) {
        List<WebElement> wel = commonspec.locateElementWithPooling(1, maxTime, method, element, 1, "present");
        PreviousWebElements pwel = new PreviousWebElements(wel);
        commonspec.setPreviousWebElements(pwel);
    }

    /**
     * Checks if an alert message is open in the current page. The function implements a pooling interval to check if the condition is true
     * <p>
     * This step stores the reference to the alert to be used in other steps such as {@link #iAcceptTheAlert()} or
     * by {@link #iDismissTheAlert()}
     *
     * <pre>{@code
     * Example:
     *
     * Scenario: Wait for an alert
     *      Given I go to 'http://mydummysite.com/login'
     *      And I check every '1' seconds for at least '5' seconds until an alert appears
     * }</pre>
     *
     * @see #iAcceptTheAlert()
     * @see #iDismissTheAlert()
     * @param poolingInterval Time between consecutive condition evaluations
     * @param poolMaxTime     Maximum time to wait for the condition to be true
     */
    @Then("^I check every '(\\d+)' seconds for at least '(\\d+)' seconds until an alert appears$")
    public void waitAlertWithPooling(int poolingInterval, int poolMaxTime) {
        Alert alert = commonspec.waitAlertWithPooling(poolingInterval, poolMaxTime);
        commonspec.setSeleniumAlert(alert);
    }

    /**
     * Accepts an alert message previously found.
     * <pre>{@code
     * Example:
     *
     * Scenario: Wait for an alert and dismiss it
     *      Given I go to 'http://mydummysite.com/login'
     *      And I check every '1' seconds for at least '5' seconds until an alert appears
     *      And I dismiss the alert
     * }</pre>
     *
     * @see #waitAlertWithPooling(int, int)
     * @see #iAcceptTheAlert()
     */
    @Then("^I dismiss the alert$")
    public void iAcceptTheAlert() {
        commonspec.dismissSeleniumAlert();
    }

    /**
     * Dismiss an alert message previously found.
     * <pre>{@code
     * Example:
     *
     * Scenario: Wait for an alert and accept it
     *      Given I go to 'http://mydummysite.com/login'
     *      And I check every '1' seconds for at least '5' seconds until an alert appears
     *      And I accept the alert
     * }
     * </pre>
     * @see #waitAlertWithPooling(int, int)
     * @see #iDismissTheAlert()
     */
    @Then("^I accept the alert$")
    public void iDismissTheAlert() {
        commonspec.acceptSeleniumAlert();
    }

    /**
     * Assigns the given file (relative to schemas/) to the web elements in the given index.
     * <p>
     * This step is suitable for file selectors/file pickers (an input type=file), where the user must specify a
     * file in the local computer as an input in a form. This step requires a previous operation for finding elements
     * to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     * <pre>{@code
     * Example:
     *
     * Scenario: Uploading a new profile picture
     *      Given I go to 'http://mydummysite.com/myprofile'
     *      When '1' elements exists with 'id:profile_pic_10'
     *      Then I assign the file in 'schemas/empty.json' to the element on index '0'
     * }</pre>
     *
     * @param fileName Name of the file relative to schemas folder (schemas/myFile.txt)
     * @param index    Index of the web element (file input)
     */
    @Then("^I assign the file in '(.*)' to the element on index '(\\d+)'$")
    public void iSetTheFileInSchemasEmptyJsonToTheElementOnIndex(String fileName, int index) {

        //Get file absolute path
        String filePath = getClass().getClassLoader().getResource(fileName).getPath();

        //Assign the file absolute path to the file picker element previously set
        File f = new File(filePath);
        Assertions.assertThat(f.exists()).as("The file located in " + filePath + " does not exists or is not accessible").isEqualTo(true);
        commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(filePath);
    }


    /**
     * Maximizes current browser window. Mind the current resolution could break a test.
     * <pre>{@code
     * Example:
     *
     * Scenario: maximize the browser
     *      Then I maximize the browser
     * }</pre>
     */
    @Given("^I maximize the browser$")
    public void seleniumMaximize() {
        commonspec.getDriver().manage().window().maximize();
    }


    /**
     * Switches to a frame/iframe.
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     * <pre>{@code
     * Example:
     *
     * Scenario: switching to the iframe
     *      Given I go to 'http://mydummysite.com/
     *      When '1' elements exists with 'id:iframeResult'
     *      Then I switch to the iframe on index '0'
     * }</pre>
     *
     * @see #seleniumIdFrame(String, String)
     * @param index the index
     */
    @Given("^I switch to the iframe on index '(\\d+)'$")
    public void seleniumSwitchFrame(Integer index) {

        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().size()).as("Could not get webelement with index %s. Less elements were found. Allowed index: 0 to %s", index, commonspec.getPreviousWebElements().getPreviousWebElements().size() - 1)
                .isGreaterThanOrEqualTo(index + 1);

        WebElement elem = commonspec.getPreviousWebElements().getPreviousWebElements().get(index);
        commonspec.getDriver().switchTo().frame(elem);
    }


    /**
     * Switch to the frame/iframe with the given locator.
     * <pre>{@code
     * Example:
     *
     * Scenario: Switch to iframe by locator
     *      Given I go to 'http://mydummysite.com/
     *      Then I switch to iframe with 'id:iframeResult'
     * }</pre>
     *
     * @see #seleniumSwitchFrame(Integer)
     * @param method  the method (id, class, name, xpath)
     * @param idframe locator
     */
    @Given("^I switch to iframe with '(" + LOCATORS + "):(.*)'$")
    public void seleniumIdFrame(String method, String idframe) {
        assertThat(commonspec.locateElement(method, idframe, 1));

        if (!method.toLowerCase().matches("id") && !method.toLowerCase().matches("name")) {
            Assertions.fail("Can not use '%s' to switch iframe. Use 'id' or 'name'", method);
        }

        commonspec.getDriver().switchTo().frame(idframe);

    }


    /**
     * Switches to a parent frame/iframe.
     * <pre>{@code
     * Example:
     *
     * Scenario: switch to a parent frame
     *      Then I switch to a parent frame
     * }
     * </pre>
     *
     * @see #seleniumIdFrame(String, String)
     * @see #seleniumSwitchFrame(Integer)
     * @see #seleniumSwitchParentFrame()
     *
     */
    @Given("^I switch to a parent frame$")
    public void seleniumSwitchAParentFrame() {
        commonspec.getDriver().switchTo().parentFrame();
    }


    /**
     * Switches to the frames main container.
     * <pre>{@code
     * Example:
     *
     * Scenario: switch to the main frame container
     *      Then I switch to the main frame container
     * }</pre>
     *
     * @see #seleniumSwitchAParentFrame()
     * @see #seleniumSwitchFrame(Integer)
     * @see #seleniumIdFrame(String, String)
     */
    @Given("^I switch to the main frame container$")
    public void seleniumSwitchParentFrame() {
        commonspec.getDriver().switchTo().frame(commonspec.getParentWindow());
    }


    /**
     * Assert that a new window is open
     * <p>
     * This step verifies that at least one new window is open. You can switch focus to this new window using
     * the step {@link #seleniumChangeWindow()}
     * <pre>{@code
     * Example:
     *
     * Scenario: Check that a new window opened
     *      Given I go to 'http:mydummysite/index.html'
     *      Then I click on the element with 'name:pie_submit'
     *      Then a new window is opened
     * }</pre>
     *
     * @see #seleniumChangeWindow()
     */
    @Given("^a new window is opened$")
    public void seleniumGetwindows() {
        Set<String> wel = commonspec.getDriver().getWindowHandles();
        assertThat(wel).as("No new windows opened. Driver only returned %s window handles", wel.size()).hasSize(2);
    }


    /**
     * Verifies that a webelement previously found has the given text
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     *
     * <pre>{@code
     * Example:
     *
     * Scenario: Verify text of element
     *      Given I go to 'http:mydummysite/index.html'
     *      When '1' elements exists with 'xpath://*[@id="pie_register"]/li[6]/div/label'
     *      And the element on index '0' has 'Phone Number' as text
     * }</pre>
     *
     * @param index the index of the webelement
     * @param text  the text to verify
     */
    @Then("^the element on index '(\\d+)' has '(.*)' as text$")
    public void assertSeleniumTextOnElementPresent(Integer index, String text) {
        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().size()).as("Could not get webelement with index %s. Less elements were found. Allowed index: 0 to %s", index, commonspec.getPreviousWebElements().getPreviousWebElements().size() - 1)
                .isGreaterThanOrEqualTo(index + 1);
        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().get(index).getText()).contains(text);
    }


    /**
     * Checks if a text exists in the source of an already loaded URL.
     * <pre>{@code
     * Example:
     *
     * Scenario: Verify text exists in page source
     *      Given I go to 'http:mydummysite/index.html'
     *      Then this text exists:
     *      """
     *      <h1 class="entry-title">Home</h1>
     *      """
     * }</pre>
     *
     * @param text the text to verify
     */
    @Then("^this text exists:$")
    public void assertSeleniumTextInSource(String text) {
        Assertions.assertThat(commonspec.getDriver().getPageSource()).as("Expected text not found at page").contains(text);
    }


    /**
     * Checks that the expected count of webelements are present in the page.
     * <p>
     * Elements found are internally stored to be used in subsequent steps in the same scenario
     * <pre>{@code
     * Examples:
     *
     * Scenario: Locating element by id
     *      Given I go to 'http:mydummysite/index.html'
     *      When '1' elements exists with 'id:profile_pic_10'
     *
     * Scenario: Locating element by class
     *      Given I go to 'http:mydummysite/index.html'
     *      When '7' elements exists with 'class:legend_txt'
     *
     * Scenario: verify that the amount of elements is bigger or equal
     *      Given I go to 'http:mydummysite/index.html'
     *      Then at least '1' elements exists with 'class:detail-entry'
     *
     * Scenario: Elements are stored to be used in subsequent steps:
     *      Given I go to 'http:mydummysite/index.html'
     *      Given '1' elements exists with 'xpath://*[@id="myBtn"]'
     *      Then I click on the element on index '0'
     * }
     * </pre>
     *
     * @see #waitWebElementWithPooling(int, int, int, String, String, String)
     * @see #seleniumClick(Integer)
     * @param atLeast       asserts that the amount of elements if greater or equal to expectedCount. If null, asserts the amount of element is equal to expectedCount
     * @param expectedCount the expected count of elements to find
     * @param method        method to locate the elements (id, name, class, css, xpath, linkText, partialLinkText and tagName)
     * @param element       the relative reference to the element
     */
    @Then("^(at least )?'(\\d+)' elements? exists? with '(" + LOCATORS + "):(.*)'$")
    public void assertSeleniumNElementExists(String atLeast, Integer expectedCount, String method, String element) {

        List<WebElement> wel;

        if (atLeast != null) {
            wel = commonspec.locateElement(method, element, -1);
            PreviousWebElements pwel = new PreviousWebElements(wel);
            Assertions.assertThat(pwel.getPreviousWebElements().size()).as("Couldn't find the expected amount of elements (at least %s) with the given %s", expectedCount, method).isGreaterThanOrEqualTo(expectedCount);
        } else {
            wel = commonspec.locateElement(method, element, expectedCount);
        }

        PreviousWebElements pwel = new PreviousWebElements(wel);
        commonspec.setPreviousWebElements(pwel);
    }


    /**
     * Checks that the expected count of webelements are present in the page, within a timeout and with a location.
     * <p>
     * Each negative lookup is followed by a wait of wait seconds. Selenium times are not accounted for the mentioned timeout.
     * This method is similar to {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     * but uses static wait instead of {@link org.openqa.selenium.support.ui.FluentWait} and does not assert the expected
     * condition of the elements (if elements are visible|hidden|present|clickable)
     * <pre>{@code
     * Example:
     *
     * Scenario: Wait for element to be present
     *      Given I go to 'http:mydummysite/index.html'
     *      Then in less than '20' seconds, checking each '2' seconds, '1' elements exists with 'id:name_3_firstname'
     *      And I click on the element on index '0'
     * }</pre>
     *
     * @deprecated use {@link #waitWebElementWithPooling(int, int, int, String, String, String)} instead.
     * @see #waitWebElementWithPooling(int, int, int, String, String, String)
     * @see #assertSeleniumNElementExists(String, Integer, String, String)
     * @see #seleniumClick(Integer)
     * @param timeout                   The max time to wait for the condition to be true
     * @param wait                      Interval between verification
     * @param expectedCount             The expected count of elements
     * @param method                    The method
     * @param element                   The web element element
     * @throws InterruptedException     The interrupted exception
     */
    @Deprecated
    @Then("^in less than '(\\d+)' seconds, checking each '(\\d+)' seconds, '(\\d+)' elements exists with '(" + LOCATORS + "):(.*)'$")
    public void assertSeleniumNElementExistsOnTimeOut(Integer timeout, Integer wait, Integer expectedCount,
                                                      String method, String element) throws InterruptedException {
        List<WebElement> wel = null;
        for (int i = 0; i < timeout; i += wait) {
            wel = commonspec.locateElement(method, element, -1);
            if (wel.size() == expectedCount) {
                break;
            } else {
                Thread.sleep(wait * 1000);
            }
        }

        PreviousWebElements pwel = new PreviousWebElements(wel);
        Assertions.assertThat(pwel.getPreviousWebElements().size()).as("Could not find the expected amount of element(s) (%s), with the given %s. Checked for %s secs, %s secs interval,", expectedCount, method, timeout, wait).isEqualTo(expectedCount);
        commonspec.setPreviousWebElements(pwel);

    }


    /**
     * Verifies if a webelement previously found is displayed or not
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     *
     * <pre>{@code
     * Example:
     *
     * Scenario: Checking if the element previously found is displayed
     *      Given I go to 'http:mydummysite/index.html'
     *      When '1' elements exists with 'id:myDIV'
     *      And the element on index '0' IS displayed
     *
     * Scenario: Checking if the element previously found is not displayed
     *      Given I go to 'http:mydummysite/index.html'
     *      When '1' elements exists with 'id:myDIV'
     *      And the element on index '0' IS NOT displayed
     * }</pre>
     *
     * @see #assertSeleniumNElementExists(String, Integer, String, String)
     * @see #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)
     * @see #waitWebElementWithPooling(int, int, int, String, String, String)
     * @param index  the index of the element
     * @param option the option (is selected or not)
     */
    @Then("^the element on index '(\\d+?)' (IS|IS NOT) displayed$")
    public void assertSeleniumIsDisplayed(Integer index, String option) {
        Boolean isDisplayed = false;

        if (option.matches("IS")) {
            isDisplayed = true;
        }

        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().size()).as("Could not get webelement with index %s. Less elements were found. Allowed index: 0 to %s", index, commonspec.getPreviousWebElements().getPreviousWebElements().size() - 1)
                .isGreaterThanOrEqualTo(index + 1);
        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().get(index).isDisplayed()).as(
                "Unexpected element display property").isEqualTo(isDisplayed);
    }


    /**
     * Verifies if a webelement previously found is enabled or not
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     *
     * <pre>{@code
     * Example:
     *
     * Scenario: Checking if the element previously found is enabled
     *      Given I go to 'http:mydummysite/index.html'
     *      When '1' elements exists with 'xpath://*[@id="myBtn"]'
     *      And the element on index '0' IS enabled
     *
     * Scenario: Checking if the element previously found is not enabled
     *      Given I go to 'http:mydummysite/index.html'
     *      When '1' elements exists with 'xpath://*[@id="myBtn"]'
     *      And the element on index '0' IS NOT enabled
     * }</pre>
     *
     * @param index  the index of the web element in the list
     * @param option the option (is enabled or not)
     */
    @Then("^the element on index '(\\d+?)' (IS|IS NOT) enabled$")
    public void assertSeleniumIsEnabled(Integer index, String option) {
        Boolean isEnabled = false;

        if (option.matches("IS")) {
            isEnabled = true;
        }

        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().size()).as("Could not get webelement with index %s. Less elements were found. Allowed index: 0 to %s", index, commonspec.getPreviousWebElements().getPreviousWebElements().size() - 1)
                .isGreaterThanOrEqualTo(index + 1);
        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().get(index).isEnabled())
                .as("Unexpected element enabled property").isEqualTo(isEnabled);
    }


    /**
     * Verifies if a webelement previously found is selected or not
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     * <pre>{@code
     * Example:
     *
     * Scenario: Checking if the element previously found is not selected
     *      Given I go to 'http:mydummysite/index.html'
     *      When '3' elements exists with 'name:radio_4[]'
     *      And the element on index '0' IS NOT selected
     *
     * Scenario: Checking if the element previously found is selected
     *      Given I go to 'http:mydummysite/index.html'
     *      When '3' elements exists with 'name:radio_4[]'
     *      And the element on index '1' IS selected
     * }</pre>
     *
     * @param index  the index of the web element in the list
     * @param option the option (if it is enabled or not)
     */
    @Then("^the element on index '(\\d+?)' (IS|IS NOT) selected$")
    public void assertSeleniumIsSelected(Integer index, String option) {
        Boolean isSelected = false;

        if (option.matches("IS")) {
            isSelected = true;
        }

        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().size()).as("Could not get webelement with index %s. Less elements were found. Allowed index: 0 to %s", index, commonspec.getPreviousWebElements().getPreviousWebElements().size() - 1)
                .isGreaterThanOrEqualTo(index + 1);
        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().get(index).isSelected()).as(
                "Unexpected element selected property").isEqualTo(isSelected);
    }


    /**
     * Verifies that a webelement previously found has attribute with value (as a regexp)
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     *
     * <pre>{@code
     * Example:
     *
     * Scenario: Checking the value of attribute
     *      Given I go to 'http:mydummysite/index.html'
     *      When '1' elements exists with 'id:loginButton'
     *      Then the element on index '0' has 'type' as 'submit'
     * }</pre>
     *
     * @param index     the index of the web element
     * @param attribute the attribute to verify
     * @param value     the value of the attribute
     */
    @Then("^the element on index '(\\d)' has '(.*?)' as '(.*)'$")
    public void assertSeleniumHasAttributeValue(Integer index, String attribute, String value) {
        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().size()).as("Could not get webelement with index %s. Less elements were found. Allowed index: 0 to %s", index, commonspec.getPreviousWebElements().getPreviousWebElements().size() - 1)
                .isGreaterThanOrEqualTo(index + 1);
        String val = commonspec.getPreviousWebElements().getPreviousWebElements().get(index).getAttribute(attribute);
        Assertions.assertThat(val).as("Attribute not found").isNotNull();
        Assertions.assertThat(val).as("Unexpected value for specified attribute").matches(value);
    }


    /**
     * Takes a snapshot/screenshot/screen capture of the current page.
     * <p>
     * Snapshots are stored under target/executions
     * <pre>{@code
     * Example:
     *
     * Scenario:
     *      Given I go to 'http:mydummysite/index.html'
     *      Then I take a snapshot
     * }</pre>
     */
    @Then("^I take a snapshot$")
    public void seleniumSnapshot() {
        commonspec.captureEvidence(commonspec.getDriver(), "screenCapture");
    }


    /**
     * Checks that we are in the URL passed
     * <pre>{@code
     * Example:
     *
     * Scenario: checking the current url
     *      Given I go to 'https://demoqa.com/'
     *      Then we are in page 'https://demoqa.com/'
     * }</pre>
     *
     * @see #checkURLContains(String)
     * @param url the url to verify
     */
    @Then("^we are in page '(.*)'$")
    public void checkURL(String url) {
        Assertions.assertThat(commonspec.getDriver().getCurrentUrl()).as("We are not in the expected url").isEqualTo(url);
    }


    /**
     * Checks if the current URL contains the specified text.
     * <pre>{@code
     * Example:
     *
     * Scenario: check if current url contains the word 'autocomplete'
     *      Given My app is running in 'demoqa.com:80'
     *      And I browse to '/autocomplete'
     *      Then the current url contains the text 'autocomplete'
     * }</pre>
     *
     * @see #checkURL(String)
     * @param text  Text to look for in the current url
     */
    @Then("^the current url contains the text '(.*)'$")
    public void checkURLContains(String text) {
        Assertions.assertThat(commonspec.getDriver().getCurrentUrl()).as("We are not in the expected url").contains(text);
    }


    /**
     * Save cookie in context for future references
     */
    @Then("^I save selenium cookies in context$")
    public void saveSeleniumCookies() {
        commonspec.setSeleniumCookies(commonspec.getDriver().manage().getCookies());
    }


    /**
     * Takes the content of a webElement and stores it in the thread environment variable passed as parameter
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     *
     * <pre>{@code
     * Example:
     *
     * Scenario: Saving the content of element
     *      Given I go to 'http:mydummysite/index.html'
     *      When '1' elements exists with 'id:my_text_field'
     *      I save content of element in index '0' in environment variable 'mytext'
     * }</pre>
     *
     * @see #assertSeleniumNElementExists(String, Integer, String, String)
     * @see #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)
     * @see #waitWebElementWithPooling(int, int, int, String, String, String)
     * @param index  position of the element in the array of webElements found
     * @param envVar name of the thread environment variable where to store the text
     */
    @Then("^I save content of element in index '(\\d+)' in environment variable '(.*)'$")
    public void saveContentWebElementInEnvVar(Integer index, String envVar) {
        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().size()).as("Could not get webelement with index %s. Less elements were found. Allowed index: 0 to %s", index, commonspec.getPreviousWebElements().getPreviousWebElements().size() - 1)
                .isGreaterThanOrEqualTo(index + 1);
        String text = commonspec.getPreviousWebElements().getPreviousWebElements().get(index).getText();
        ThreadProperty.set(envVar, text);
    }


    /**
     * Verifies if the value of a property of the webelement referenced by index matches the given value
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     *
     * <pre>{@code
     * Example:
     *
     * Scenario: Verify property of element
     *      Given I go to 'http:mydummysite/index.html'
     *      And '3' elements exists with 'css:input[name='radio_4[]']'
     *      Then the element in index '1' has 'radio_4[]' in property 'name'
     * }</pre>
     *
     * @see #assertSeleniumNElementExists(String, Integer, String, String)
     * @see #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)
     * @see #waitWebElementWithPooling(int, int, int, String, String, String)
     * @param index          Position of the element in the array of webElements
     * @param textValue      Value expected in the property
     * @param customProperty Property of webElement to verify
     */
    @Then("^the element in index '(.*)' has '(.*)' in property '(.*)'$")
    public void theElementOnIndexHasTextInCustomPropertyName(int index, String textValue, String customProperty) {

        List<WebElement> wel = commonspec.getPreviousWebElements().getPreviousWebElements();
        assertThat(wel.size()).as("The last step did not find elements").isNotZero();

        String value = wel.get(index).getAttribute(customProperty);
        assertThat(value).as("The element doesn't have the property '%s'", customProperty).isNotEmpty().isNotNull();
        assertThat(value).as("The property '%s' doesn't have the text '%s'", customProperty, textValue).isEqualToIgnoringCase(textValue);

    }


    /**
     * Search for two webelements dragging the first one to the second
     *
     * @param smethod     the smethod
     * @param source      initial web element
     * @param dmethod     the dmethod
     * @param destination destination web element
     */
    @When("^I drag '(.*):(.*)' and drop it to '(.*):(.*)'$")
    public void seleniumDrag(String smethod, String source, String dmethod, String destination) {
        Actions builder = new Actions(commonspec.getDriver());

        List<WebElement> sourceElement = commonspec.locateElement(smethod, source, 1);
        List<WebElement> destinationElement = commonspec.locateElement(dmethod, destination, 1);

        builder.dragAndDrop(sourceElement.get(0), destinationElement.get(0)).perform();
    }


    /**
     * Click on an numbered url previously found element.
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}<br>.
     * You can also perform click action on an element using {@link #seleniumClickByLocator(String, String, Integer)}
     *
     * <pre>{@code
     * Example:
     *
     * Scenario: Click on previously found element
     *      Given I go to 'http:mydummysite/index.html'
     *      When '1' elements exists with 'xpath://*[@id="name_3_lastname"]'
     *      And I click on the element on index '0'
     * }</pre>
     *
     * @deprecated This method is deprecated, use {@link #seleniumClickByLocator(String, String, Integer)} instead
     * @see #assertSeleniumNElementExists(String, Integer, String, String)
     * @see #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)
     * @see #waitWebElementWithPooling(int, int, int, String, String, String)
     * @see #seleniumClickByLocator(String, String, Integer)
     * @param index Index of the webelement in the list
     */
    @Deprecated
    @When("^I click on the element on index '(\\d+)'$")
    public void seleniumClick(Integer index) {

        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().size()).as("Could not get webelement with index %s. Less elements were found. Allowed index: 0 to %s", index, commonspec.getPreviousWebElements().getPreviousWebElements().size() - 1)
                .isGreaterThanOrEqualTo(index + 1);

        commonspec.getPreviousWebElements().getPreviousWebElements().get(index).click();

    }

    /**
     * Double clicks on an numbered url previously found element.
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}<br>.
     * <pre>{@code
     * Example:
     *
     * Scenario: Performing a double click on element previously found
     *      Given I go to 'http:mydummysite/index.html'
     *      When '1' elements exists with 'id:doubleClickBtn'
     *      And I double click on the element on index '0'
     * }</pre>
     *
     * @deprecated This method is deprecated, use {@link #seleniumDoubleClickByLocator(String, String, Integer)} instead
     * @see #seleniumDoubleClickByLocator(String, String, Integer)
     * @see #assertSeleniumNElementExists(String, Integer, String, String)
     * @see #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)
     * @see #waitWebElementWithPooling(int, int, int, String, String, String)
     * @param index Index of the webelement in the list
     */
    @Deprecated
    @When("^I double click on the element on index '(\\d+)'$")
    public void seleniumDoubleClick(Integer index) {

        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().size()).as("Could not get webelement with index %s. Less elements were found. Allowed index: 0 to %s", index, commonspec.getPreviousWebElements().getPreviousWebElements().size() - 1)
                .isGreaterThanOrEqualTo(index + 1);
        Actions actions = new Actions(this.commonspec.getDriver());
        actions.doubleClick(commonspec.getPreviousWebElements().getPreviousWebElements().get(index)).perform();

    }

    /**
     * Right clicks on an numbered url previously found element.
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}<br>.
     * <pre>{@code
     * Example:
     *
     * Scenario: Performing a right click on the element previously found
     *      Given I go to 'http:mydummysite/index.html'
     *      When '1' elements exists with 'id:rightClickBtn'
     *      And I right click on the element on index '0'
     * }</pre>
     *
     * @deprecated This method is deprecated, use {@link #seleniumRightClickByLocator(String, String, Integer)} instead
     * @see #seleniumRightClickByLocator(String, String, Integer)
     * @see #assertSeleniumNElementExists(String, Integer, String, String)
     * @see #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)
     * @see #waitWebElementWithPooling(int, int, int, String, String, String)
     * @param index Index of the webelement in the list
     */
    @Deprecated
    @When("^I right click on the element on index '(\\d+)'$")
    public void seleniumRightClick(Integer index) {

        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().size()).as("Could not get webelement with index %s. Less elements were found. Allowed index: 0 to %s", index, commonspec.getPreviousWebElements().getPreviousWebElements().size() - 1)
                .isGreaterThanOrEqualTo(index + 1);
        Actions actions = new Actions(this.commonspec.getDriver());
        actions.contextClick(commonspec.getPreviousWebElements().getPreviousWebElements().get(index)).perform();

    }


    /**
     * Clear the text on a numbered index previously found element.
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     *
     * <pre>{@code
     * Example:
     *
     * Scenario:
     *      Given I go to 'http:mydummysite/index.html'
     *      When '1' elements exists with 'id:phone_9'
     *      And I type '555-555' on the element on index '0'
     *      And I clear the content on text input at index '0'
     * }</pre>
     *
     * @see #assertSeleniumNElementExists(String, Integer, String, String)
     * @see #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)
     * @see #waitWebElementWithPooling(int, int, int, String, String, String)
     * @param index index of the web element
     */
    @When("^I clear the content on text input at index '(\\d+)'$")
    public void seleniumClear(Integer index) {
        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().size()).as("Could not get webelement with index %s. Less elements were found. Allowed index: 0 to %s", index, commonspec.getPreviousWebElements().getPreviousWebElements().size() - 1)
                .isGreaterThanOrEqualTo(index + 1);

        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().get(index)).is(commonspec.getTextFieldCondition());

        commonspec.getPreviousWebElements().getPreviousWebElements().get(index).clear();
    }


    /**
     * Type a text on an numbered index previously found element.
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}<br>
     * You can also use the step {@link #seleniumTypeByLocator(String, String, String, Integer)} to directly type the given
     * text in the element
     *
     * <pre>{@code
     * Example:
     *
     * Scenario:
     *      Given I go to 'http:mydummysite/index.html'
     *      When '1' elements exists with 'id:phone_9'
     *      Then I type '555-555' on the element on index '0'
     * }</pre>
     *
     * @deprecated This method is deprecated, use {@link #seleniumTypeByLocator(String, String, String, Integer)} instead
     * @see #seleniumTypeByLocator(String, String, String, Integer)
     * @see #seleniumTypeLongTextByLocator(String, String, Integer, DocString)
     * @see #assertSeleniumNElementExists(String, Integer, String, String)
     * @see #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)
     * @see #waitWebElementWithPooling(int, int, int, String, String, String)
     * @param input Text to write on the element
     * @param index Index of the webelement in the list
     */
    @Deprecated
    @When("^I type '(.*)' on the element on index '(\\d+)'$")
    public void seleniumType(String input, Integer index) {

        NullableStringConverter converter = new NullableStringConverter();
        String text = converter.transform(input);

        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().size()).as("Could not get webelement with index %s. Less elements were found. Allowed index: 0 to %s", index, commonspec.getPreviousWebElements().getPreviousWebElements().size() - 1)
                .isGreaterThanOrEqualTo(index + 1);
        while (text.length() > 0) {
            if (-1 == text.indexOf("\\n")) {
                commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(text);
                text = "";
            } else {
                commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(text.substring(0, text.indexOf("\\n")));
                commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(Keys.ENTER);
                text = text.substring(text.indexOf("\\n") + 2);
            }
        }
    }


    /**
     * Send a strokes list on an numbered url previously found element or to the driver.
     * <p>
     * Strokes examples are "HOME, END" or "END, SHIFT + HOME, DELETE". Each element in the stroke list has to be an element from
     * {@link org.openqa.selenium.Keys} (NULL, CANCEL, HELP, BACK_SPACE, TAB, CLEAR, RETURN, ENTER, SHIFT, LEFT_SHIFT,
     * CONTROL, LEFT_CONTROL, ALT, LEFT_ALT, PAUSE, ESCAPE, SPACE, PAGE_UP, PAGE_DOWN, END, HOME, LEFT, ARROW_LEFT, UP,
     * ARROW_UP, RIGHT, ARROW_RIGHT, DOWN, ARROW_DOWN, INSERT, DELETE, SEMICOLON, EQUALS, NUMPAD0, NUMPAD1, NUMPAD2,
     * NUMPAD3, NUMPAD4, NUMPAD5, NUMPAD6, NUMPAD7, NUMPAD8, NUMPAD9, MULTIPLY, ADD, SEPARATOR, SUBTRACT, DECIMAL,
     * DIVIDE, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, META, COMMAND, ZENKAKU_HANKAKU) , a plus sign (+), a
     * comma (,) or spaces ( ) <br>
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     *
     * <pre>{@code
     * Example:
     *
     * Scenario: Press enter on the given element
     *      Given I go to 'http:mydummysite/index.html'
     *      When '1' elements exists with 'id:name_3_firstname'
     *      Then I type 'testUser' on the element on index '0'
     *      Then I send 'ENTER' on the element on index '0'
     * }</pre>
     *
     * @see #seleniumType(String, Integer)
     * @see #assertSeleniumNElementExists(String, Integer, String, String)
     * @see #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)
     * @see #waitWebElementWithPooling(int, int, int, String, String, String)
     * @param text  key stroke to send
     * @param index index of the web element in the list
     */
    @When("^I send '(.*)' on the element on index '(\\d+)'$")
    public void seleniumKeys(String text, Integer index) {

        ArrayListConverter converter = new ArrayListConverter();
        List<String> strokes = converter.transform(text);

        if (index != null) {
            Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().size()).as("Could not get webelement with index %s. Less elements were found. Allowed index: 0 to %s", index, commonspec.getPreviousWebElements().getPreviousWebElements().size() - 1)
                    .isGreaterThanOrEqualTo(index + 1);
        }
        assertThat(strokes).isNotEmpty();

        for (String stroke : strokes) {
            if (stroke.contains("+")) {
                List<Keys> csl = new ArrayList<Keys>();
                for (String strokeInChord : stroke.split("\\+")) {
                    csl.add(Keys.valueOf(strokeInChord.trim()));
                }
                Keys[] csa = csl.toArray(new Keys[csl.size()]);
                if (index == null) {
                    new Actions(commonspec.getDriver()).sendKeys(commonspec.getDriver().findElement(By.tagName("body")), csa).perform();
                } else {
                    commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(csa);
                }
            } else {
                if (index == null) {
                    new Actions(commonspec.getDriver()).sendKeys(commonspec.getDriver().findElement(By.tagName("body")), Keys.valueOf(stroke)).perform();
                } else {
                    commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(Keys.valueOf(stroke));
                }
            }
        }
    }


    /**
     * Choose an option from a select webelement found previously
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     *
     * @see #assertSeleniumNElementExists(String, Integer, String, String)
     * @see #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)
     * @see #waitWebElementWithPooling(int, int, int, String, String, String)
     * @param option option in the select element
     * @param index  index of the web element in the list
     */
    @When("^I select '(.*)' on the element on index '(\\d+)'$")
    public void elementSelect(String option, Integer index) {
        Select sel = null;
        sel = new Select(commonspec.getPreviousWebElements().getPreviousWebElements().get(index));

        sel.selectByVisibleText(option);
    }


    /**
     * Choose no option from a select webelement found previously
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     *
     * @param index index of the web element in the list
     */
    @When("^I de-select every item on the element on index '(\\d+)'$")
    public void elementDeSelect(Integer index) {
        Select sel = null;
        sel = new Select(commonspec.getPreviousWebElements().getPreviousWebElements().get(index));

        if (sel.isMultiple()) {
            sel.deselectAll();
        }
    }


    /**
     * Change current focus to another opened window.
     * <p>
     * All further selenium actions will be executed in this new window. You can use the step
     * {@link #seleniumGetwindows()} to assert that a new window is indeed open
     * <pre>{@code
     * Example:
     *
     * Scenario:
     *      Given I go to 'http:mydummysite/index.html'
     *      Then I click on the element with 'name:pie_submit'
     *      Given a new window is opened
     *      Then I change active window
     * }</pre>
     *
     * @see #seleniumGetwindows()
     */
    @When("^I change active window$")
    public void seleniumChangeWindow() {
        String originalWindowHandle = commonspec.getDriver().getWindowHandle();
        Set<String> windowHandles = commonspec.getDriver().getWindowHandles();

        for (String window : windowHandles) {
            if (!window.equals(originalWindowHandle)) {
                commonspec.getDriver().switchTo().window(window);
            }
        }

    }

    /**
     * Saves the given property of the specified webelement (referenced by its index) in the specified variable.
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     *
     * <pre>{@code
     * Example:
     *
     * Scenario: Saving the value of class in local variable for future use
     *      Given I go to 'http:mydummysite/index.html'
     *      Then '1' elements exists with 'id:menu-item-146'
     *      Then I save the value of the property 'class' of the element in index '0' in variable 'VAR2'
     * }</pre>
     *
     * @see #assertSeleniumNElementExists(String, Integer, String, String)
     * @see #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)
     * @see #waitWebElementWithPooling(int, int, int, String, String, String)
     * @param propertyName Name of the property
     * @param index        Index of the webelement in the list
     * @param variable     Variable where to save the result
     */
    @Then("^I save the value of the property '(.*)' of the element in index '(.*)' in variable '(.*)'$")
    public void iSaveTheValueOfThePropertyHrefOfTheElementInIndexInVariableVAR(String propertyName, int index, String variable) {
        List<WebElement> wel = commonspec.getPreviousWebElements().getPreviousWebElements();
        String value = wel.get(index).getAttribute(propertyName);
        assertThat(value).as("The web element doesn't have the property '" + propertyName + "'").isNotNull();
        ThreadProperty.set(variable, value);
    }


    /**
     * Executes a JavaScript function in the current driver. This could be useful for getting specific information on the
     * web page or forcing specific actions, like clicking on an element that is being blocked by a popup
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     *
     * <pre>{@code
     * Example:
     *
     * Scenario: Executes a JavaScript function
     *      Given I go to 'http:mydummysite/index.html'
     *      And I execute 'alert("This is an alert!")' as javascript
     *
     * Scenario: Javascript functions can be executed on previous found elements
     *      Given I go to 'http:mydummysite/index.html'
     *      And '1' elements exists with 'xpath://*[@id="menu-item-158"]/a'
     *      And I execute 'arguments[0].click();' as javascript on the element on index '0'
     * }</pre>
     *
     * @see #assertSeleniumNElementExists(String, Integer, String, String)
     * @see #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)
     * @see #waitWebElementWithPooling(int, int, int, String, String, String)
     * @param script Script to execute (i.e alert("This is an alert message"))
     * @param index  If used, the index of the previously found web element on which to execute the function
     * @param enVar  If used, variable where to store the result of the execution of the script
     */
    @Then("^I execute '(.*)' as javascript( on the element on index '(.*)')?( and save the result in the environment variable '(.*)')?$")
    public void iExecuteTheScriptScriptOnTheElmentOnIndex(String script, String index, String enVar) {

        JavascriptExecutor executor = (JavascriptExecutor) this.commonspec.getDriver();
        Object output;

        if (index != null) {
            List<WebElement> wel = commonspec.getPreviousWebElements().getPreviousWebElements();
            output = executor.executeScript(script, wel.get(Integer.parseInt(index)));
        } else {
            output = executor.executeScript(script);
        }

        if (enVar != null) {
            assertThat(output).as("The script did not return any value!").isNotNull();
            ThreadProperty.set(enVar, output.toString());
        }
    }

    /**
     * Directly navigate to the specified url
     * <p>
     * This step is a similar way of navigating to a web page by specifying the
     * full url directly, instead of first setting the base path with {@link #setupApp(String)}
     * and later navigate with {@link #seleniumBrowse(String, String)}
     *
     * <pre>{@code
     * Examples:
     *
     * Scenario: Directly navigate to the given page
     *      Given I go to 'http://www.demoqa.com/autocomplete'
     *
     * Scenario: Setting a base path first
     *      Given My app is running in 'demoqa.com:80'
     *      And I browse to '/autocomplete'
     * }</pre>
     *
     * @see #setupApp(String)
     * @see #seleniumBrowse(String, String)
     * @param url   Url were to navigate
     */
    @Given("^I go to '(.*)'$")
    public void iGoToUrl(String url) {

        if (!url.toLowerCase().contains("http://") && !url.toLowerCase().contains("https://")) {
            Assertions.fail("Could not infer hypertext transfer protocol. Include 'http://' or 'https://'");
        }

        commonspec.getDriver().get(url);
        commonspec.setParentWindow(commonspec.getDriver().getWindowHandle());
    }

    /**
     * Directly clicks the given element referenced by locator
     * <p>
     * This step performs a click action on the element. This step is similar to {@link #seleniumClick(Integer)}
     * but, it does not require a previous operation for finding elements, such as {@link #assertSeleniumNElementExists(String, Integer, String, String)}
     * or {@link #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)}. If the page contains
     * several web elements with the given locator, the click action will be performed to the first element found by default,
     * unless an index is specified (first element has index 0)
     * <pre>{@code
     * Examples:
     *
     * Scenario: Perform click on the element with id 'username'
     *      Given I go to 'http:mydummysite/index.html'
     *      When I click on the element with 'id:username'
     *
     * Scenario: Using index in case more than one element is found (first element has index 0)
     *      Given I go to 'http:mydummysite/index.html'
     *      When I click on the element with 'tagName:button' index 1
     * }</pre>
     *
     * @see #seleniumClick(Integer)
     * @see #assertSeleniumNElementExists(String, Integer, String, String)
     * @see #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)
     * @param method        method to locate the elements (id, name, class, css, xpath, linkText, partialLinkText and tagName)
     * @param element       the relative reference to the element
     * @param index         Index of the element, in case one or more elements with the given locator are found (first element starts with index 0)
     */
    @Then("^I click on the element with '(" + LOCATORS + "):(.*?)'( index '(\\d+)')?$")
    public void seleniumClickByLocator(String method, String element, Integer index) {
        this.assertSeleniumNElementExists("at least", 1, method, element);
        if (index == null) {
            index = 0;
        }
        this.seleniumClick(index);
    }

    /**
     * Directly types the given text in the element referenced by locator.
     * <p>
     * This steps types the given text on the given web element. This step is similar to {@link #seleniumType(String, Integer)}
     * but, it does not require a previous operation for finding elements, such as {@link #assertSeleniumNElementExists(String, Integer, String, String)}
     * or {@link #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)}. If the page contains
     * several web elements with the given locator, the click action will be performed to the first element found by default,
     * unless an index is specified (first element has index 0)
     * <pre>{@code
     * Examples:
     *
     * Scenario: Type 555-555 in the input field with id:phone_number
     *      Given I go to 'http:mydummysite/index.html'
     *      And I type '555-555' on the element with 'id:phone_number'
     *
     * Scenario: Using index in case more than one element is found (first element has index 0)
     *      Given I go to 'http:mydummysite/index.html'
     *      And I type '555-555' on the element with 'class:text-field' index 1
     * }
     * </pre>
     *
     * @see #seleniumType(String, Integer)
     * @see #seleniumTypeLongTextByLocator(String, String, Integer, DocString)
     * @param input     text to type in the element
     * @param method    method to locate the elements (id, name, class, css, xpath, linkText, partialLinkText and tagName)
     * @param element   the relative reference to the element
     * @param index     Index of the element, in case one or more elements with the given locator are found (first element starts with index 0)
     */
    @When("^I type '(.*)' on the element with '(" + LOCATORS + "):(.*?)'( index '(\\d+)')?$")
    public void seleniumTypeByLocator(String input, String method, String element, Integer index) {
        this.assertSeleniumNElementExists("at least", 1, method, element);
        if (index == null) {
            index = 0;
        }
        this.seleniumType(input, index);
    }

    /**
     * Directly types the given large text in the element referenced by locator.
     * <p>
     * This step types the given large string of text in the given element. It's very similar to {@link #seleniumTypeByLocator(String, String, String, Integer)}
     * but uses a DocString, which allows using large strings of text, suitable for typing a long address or a long comment. If the page contains
     * several web elements with the given locator, the click action will be performed to the first element found by default,
     * unless an index is specified (first element has index 0)
     * <pre>{@code
     * Examples:
     *
     * Scenario: Typing a large piece of text
     *      Given I go to 'http:mydummysite/index.html'
     *      And I type on the element with 'id:message' the text:
     *       """
     *        Good morning George!
     *        ===============
     *        The package of your order with number 1234886 should be about to arrive!
     *       """
     *
     * Scenario: Using index in case more than one element is found (first element has index 0).
     *      Given I go to 'http:mydummysite/index.html'
     *      And I type on the element with 'id:message' index '0' the text:
     *       """
     *        Good morning ${USER_NAME}!
     *        ===============
     *        The package of your order with number ${ORDER_NUMBER} should be about to arrive!
     *       """
     * }</pre>
     *
     * @see #seleniumType(String, Integer)
     * @see #seleniumTypeByLocator(String, String, String, Integer)
     * @param input     text to type in the element
     * @param method    method to locate the elements (id, name, class, css, xpath, linkText, partialLinkText and tagName)
     * @param element   the relative reference to the element
     * @param index     Index of the element, in case one or more elements with the given locator are found (first element starts with index 0)
     */
    @When("^I type on the element with '(" + LOCATORS + "):(.*?)'( index '(\\d+)')? the text:$")
    public void seleniumTypeLongTextByLocator(String method, String element, Integer index, DocString input) {
        this.assertSeleniumNElementExists("at least", 1, method, element);
        if (index == null) {
            index = 0;
        }
        this.seleniumType(input.getContent(), index);
    }

    @And("^I go back (\\d+) (?:page|pages)?$")
    public void goBackBrowserHistory(Integer numberOfPages) {
        for (int i = 0; i < numberOfPages; i++) {
            this.commonspec.getDriver().navigate().back();
        }
    }

    @And("^I go forward (\\d+) (?:page|pages)?$")
    public void goForwardBrowserHistory(Integer numberOfPages) {
        for (int i = 0; i < numberOfPages; i++) {
            this.commonspec.getDriver().navigate().forward();
        }
    }

    /**
     * Scrolls the page up or down until the element referenced by index is visible
     * <p>
     * This step executes a javascript function to automatically scroll the element into view. The web element is referenced
     * by its index, to  it does require a previous operation for finding elements, such as {@link #assertSeleniumNElementExists(String, Integer, String, String)}
     * or {@link #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} to be executed first.
     * <pre>{@code
     * Example:
     *
     * Scenario: Scroll until the submit button is visible, then, click on it
     *      Given I go to 'http:mydummysite/index.html'
     *      And at least '1' elements exists with 'id:submit'
     *      Then I scroll down until the element on index '0' is visible
     *      Then I click on the element on index '0'
     * }</pre>
     *
     * @see #scrollUntilElementVisibleByLocator(String, String, String, Integer)
     * @see #assertSeleniumNElementExists(String, Integer, String, String)
     * @see #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)
     * @param direction             Indicates if the scroll is upwards or downwards
     * @param index                 Index of the web element
     * @throws InterruptedException InterruptedException
     */
    @Then("^I scroll (up|down) until the element on index '(\\d+)' is visible$")
    public void scrollUntilElementVisible(String direction, int index) throws InterruptedException {
        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().size()).as("Could not get webelement with index %s. Less elements were found. Allowed index: 0 to %s", index, commonspec.getPreviousWebElements().getPreviousWebElements().size() - 1)
                .isGreaterThanOrEqualTo(index + 1);

        String script;

        if (direction.matches("up")) {
            script = "arguments[0].scrollIntoView(false);";
        } else {
            script = "arguments[0].scrollIntoView(true);";
        }

        ((JavascriptExecutor) this.commonspec.getDriver()).executeScript(script, this.commonspec.getPreviousWebElements().getPreviousWebElements().get(index));
        Thread.sleep(500);
    }

    /**
     * Directly scrolls the page up or down until the element is visible
     * <p>
     * This step executes a javascript function to automatically scroll the element into view. This step is similar to {@link #scrollUntilElementVisible(String, int)}
     * but it does not require a previous operation for finding elements to be executed, since the element is directly referenced as a locator
     * <pre>{@code
     * Example:
     *
     * Scenario: Scroll up and down the page until the referenced element is visible
     *      Given I go to 'http:mydummysite/index.html'
     *      Then I scroll up until the element with 'id:userName' is visible
     *      Then I scroll down until the element with 'id:submit' is visible
     *      Then I click on the element with 'id:submit'
     * }</pre>
     *
     * @see #scrollUntilElementVisible(String, int)
     * @param direction                 Indicates if the scroll is upwards or downwards
     * @param method                    method to locate the elements (id, name, class, css, xpath, linkText, partialLinkText and tagName)
     * @param element                   The relative reference to the element
     * @param index                     Index of the element, in case one or more elements with the given locator are found (first element starts with index 0)
     * @throws InterruptedException     InterruptedException
     */
    @Then("^I scroll (up|down) until the element with '(" + LOCATORS +  "):(.*?)'( index '(\\d+)')? is visible$")
    public void scrollUntilElementVisibleByLocator(String direction, String method, String element, Integer index) throws InterruptedException {
        this.assertSeleniumNElementExists("at least", 1, method, element);
        if (index == null) {
            index = 0;
        }
        this.scrollUntilElementVisible(direction, index);
    }

    /**
     * Directly double clicks the given element referenced by locator
     * <p>
     * This step performs a double click action on the element. This step is similar to {@link #seleniumDoubleClick(Integer)}
     * but, it does not require a previous operation for finding elements, such as {@link #assertSeleniumNElementExists(String, Integer, String, String)}
     * or {@link #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)}. If the page contains
     * several web elements with the given locator, the double click action will be performed to the first element found by default,
     * unless an index is specified (first element has index 0)
     * <pre>{@code
     * Example:
     *
     * Scenario: Perform double click on the element with id 'doubleClickBtn'
     *      Given I go to 'http:mydummysite/index.html'
     *      When I double click on the element with 'id:doubleClickBtn'
     *
     * Scenario: Using index in case more than one element is found (first element has index 0)
     *      Given I go to 'http:mydummysite/index.html'
     *      When I double click on the element with 'tagName:button' index 1
     * }</pre>
     *
     * @see #seleniumDoubleClick(Integer)
     * @see #assertSeleniumNElementExists(String, Integer, String, String)
     * @see #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)
     * @param method        method to locate the elements (id, name, class, css, xpath, linkText, partialLinkText and tagName)
     * @param element       the relative reference to the element
     * @param index         Index of the element, in case one or more elements with the given locator are found (first element starts with index 0)
     */
    @Then("^I double click on the element with '(" + LOCATORS + "):(.*?)'( index '(\\d+)')?$")
    public void seleniumDoubleClickByLocator(String method, String element, Integer index) {
        this.assertSeleniumNElementExists("at least", 1, method, element);
        if (index == null) {
            index = 0;
        }
        this.seleniumDoubleClick(index);
    }


    /**
     * Directly right clicks the given element referenced by locator
     * <p>
     * This step performs a right click action on the element. This step is similar to {@link #seleniumRightClick(Integer)}
     * but, it does not require a previous operation for finding elements, such as {@link #assertSeleniumNElementExists(String, Integer, String, String)}
     * or {@link #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)}. If the page contains
     * several web elements with the given locator, the right click action will be performed to the first element found by default,
     * unless an index is specified (first element has index 0)
     * <pre>{@code
     * Example:
     *
     * Scenario: Perform right click on the element with id 'rightClickBtn'
     *      Given I go to 'http:mydummysite/index.html'
     *      When I right click on the element with 'id:rightClickBtn'
     *
     * Scenario: Using index in case more than one element is found (first element has index 0)
     *      Given I go to 'http:mydummysite/index.html'
     *      When I right click on the element with 'tagName:button' index '1'
     * }
     * </pre>
     * @see #seleniumRightClick(Integer)
     * @see #assertSeleniumNElementExists(String, Integer, String, String)
     * @see #assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)
     * @param method        method to locate the elements (id, name, class, css, xpath, linkText, partialLinkText and tagName)
     * @param element       the relative reference to the element
     * @param index         Index of the element, in case one or more elements with the given locator are found (first element starts with index 0)
     */
    @Then("^I right click on the element with '(" + LOCATORS + "):(.*?)'( index '(\\d+)')?$")
    public void seleniumRightClickByLocator(String method, String element, Integer index) {
        this.assertSeleniumNElementExists("at least", 1, method, element);
        if (index == null) {
            index = 0;
        }
        this.seleniumRightClick(index);
    }


    /**
     * Directly hovers on the given element referenced by locator
     * <p>
     * This step performs the function of hovering, or, directly placing the cursor (mouse pointer)
     * on top of the specified element. This is particularly useful since in some situations, DOM elements
     * are only revealed after the mouse is directly placed on top of another element (like tooltips)
     * <pre>{@code
     * Example:
     *
     * Scenario: Hover on element
     *      Given I go to 'http:mydummysite/index.html'
     *      Given I hover on the element with 'id:revelPopUpButton'
     *
     * Scenario: Using index in case more than one element is found (first element has index 0)
     *      Given I go to 'http:mydummysite/index.html'
     *      Given I hover on the element with 'id:revelPopUpButton' index '1'
     * }</pre>
     *
     * @param method        method to locate the elements (id, name, class, css, xpath, linkText, partialLinkText and tagName)
     * @param element       the relative reference to the element
     * @param index         Index of the element, in case one or more elements with the given locator are found (first element starts with index 0)
     */
    @Then("^I hover on the element with '(" + LOCATORS + "):(.*?)'( index '(\\d+)')?$")
    public void seleniumHoverByLocator(String method, String element, Integer index) {
        this.assertSeleniumNElementExists("at least", 1, method, element);
        if (index == null) {
            index = 0;
        }
        Actions action = new Actions(this.commonspec.getDriver());
        action.moveToElement(this.commonspec.getPreviousWebElements().getPreviousWebElements().get(index)).perform();
    }


    /**
     * Temporally stop the execution of the feature
     * <p>
     * This step shows a dialog in the center of the screen and interrupts the execution of the rest
     * of the steps in the feature until the "Ok" button in the dialog is pressed. This comes handy when
     * debugging and the user needs to execute some manual actions before continuing
     * <pre>{@code
     * Example:
     *
     * Scenario: Step the execution
     *     Given I go to 'http://demoqa.com/automation-practice-form'
     *     Then I pause
     *     Then I type 'Jose' on the element with 'id:firstName'
     * }</pre>
     *
     * @see UtilsGSpec#idleWait(Integer)
     * @see #waitWebElementWithPooling(int, int, int, String, String, String)
     * @see #waitAlertWithPooling(int, int)
     */
    @Then("^I pause$")
    public void SeleniumPause() {
        this.commonspec.getLogger().info("Pausing feature execution until button in dialog is pressed...");

        JFrame jf = new JFrame();
        JOptionPane.showMessageDialog(jf, "Feature execution is paused and will resume when you click OK.",
                    "Cucumber paused", JOptionPane.INFORMATION_MESSAGE);
        jf.toFront();
        jf.repaint();
    }

}
