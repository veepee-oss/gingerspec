package com.privalia.qa.specs;

import com.privalia.qa.cucumber.converter.ArrayListConverter;
import com.privalia.qa.cucumber.converter.NullableStringConverter;
import com.privalia.qa.utils.PreviousWebElements;
import com.privalia.qa.utils.ThreadProperty;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.privalia.qa.assertions.Assertions.assertThat;

/**
 * Steps definitions for selenium (web application automation)
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
     * Set app host and port.
     * <p>
     * This is an initialization step. This is used as the first step in Selenium features to configure
     * the basepath url
     * <pre>
     * Example:
     * {@code
     *      Given My app is running in 'demoqa.com:80'
     * }
     * </pre>
     *
     * @param host host where app is running (i.e "localhost" or "localhost:443")
     */
    @Given("^My app is running in '(.+?)'$")
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
     * Browse to {@code url} using the current browser.
     * <p>
     * The {@code url} is relative to the basepath configured with {@link SeleniumGSpec#setupApp(String)} method
     * <pre>
     * Example:
     * {@code
     *      Given My app is running in 'demoqa.com:80'
     *      Then I browse to '/'                        //will load http://demoqa.com:80/
     * }
     * Or if the site uses https
     * {@code
     *      Given My app is running in 'mysecuresite.com:443'
     *      Then I securely browse to '/'               //will load https://mysecuresite.com:443/
     * }
     * </pre>
     *
     * @param isSecured If the connection should be secured
     * @param path      path of running app
     * @throws Exception exception
     */
    @Given("^I( securely)? browse to '(.+?)'$")
    public void seleniumBrowse(String isSecured, String path) throws Exception {
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
     * This method is similar to {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * but implements a pooling mechanism with a maximum pooling time instead of a static wait
     *
     * <pre>
     * Example:
     * {@code
     *      Then in less than '10' seconds, checking each '1' seconds, '1' elements exists with 'id:name_3_firstname'
     * }
     * </pre>
     *
     * @param poolingInterval Time between consecutive condition evaluations
     * @param poolMaxTime     Maximum time to wait for the condition to be true
     * @param elementsCount   integer. Expected number of elements.
     * @param method          class of element to be searched
     * @param element         webElement searched in selenium context
     * @param type            The expected style of the element: visible, clickable, present, hidden
     * @throws Throwable Throwable
     */
    @Then("^I check every '(\\d+)' seconds for at least '(\\d+)' seconds until '(\\d+)' elements exists with '([^:]*?):(.+?)' and is '(visible|clickable|present|hidden)'$")
    public void waitWebElementWithPooling(int poolingInterval, int poolMaxTime, int elementsCount, String method, String element, String type) throws Throwable {
        List<WebElement> wel = commonspec.locateElementWithPooling(poolingInterval, poolMaxTime, method, element, elementsCount, type);
        PreviousWebElements pwel = new PreviousWebElements(wel);
        commonspec.setPreviousWebElements(pwel);
    }

    /**
     * Checks if an alert message is open in the current page. The function implements a pooling interval to check if the condition is true
     * <p>
     * This step stores the reference to the alert to be used in other steps such as {@link #iAcceptTheAlert()} or
     * by {@link #iDismissTheAlert()}
     * <pre>
     * Example:
     * {@code
     *      And I check every '1' seconds for at least '5' seconds until an alert appears
     * }
     * </pre>
     * @param poolingInterval Time between consecutive condition evaluations
     * @param poolMaxTime     Maximum time to wait for the condition to be true
     */
    @Then("^I check every '(\\d+)' seconds for at least '(\\d+)' seconds until an alert appears$")
    public void waitAlertWithPooling(int poolingInterval, int poolMaxTime) {
        Alert alert = commonspec.waitAlertWithPooling(poolingInterval, poolMaxTime);
        commonspec.setSeleniumAlert(alert);
    }

    /**
     * Accepts an alert message previously found
     * <pre>
     * Example:
     * {@code
     *      And I check every '1' seconds for at least '5' seconds until an alert appears
     *      And I dismiss the alert
     * }
     * </pre>
     */
    @Then("^I dismiss the alert$")
    public void iAcceptTheAlert() {
        commonspec.dismissSeleniumAlert();
    }

    /**
     * Dismiss an alert message previously found
     * <pre>
     * Example:
     * {@code
     *      And I check every '1' seconds for at least '5' seconds until an alert appears
     *      And I accept the alert
     * }
     * </pre>
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
     * <pre>
     * Example:
     * {@code
     *      When '1' elements exists with 'id:profile_pic_10'
     *      Then I assign the file in 'schemas/empty.json' to the element on index '0'
     * }
     * </pre>
     *
     * @param fileName Name of the file relative to schemas folder (schemas/myFile.txt)
     * @param index    Index of the web element (file input)
     */
    @Then("^I assign the file in '(.+?)' to the element on index '(\\d+)'$")
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
     * <pre>
     * Example:
     * {@code
     *      Then I maximize the browser
     * }
     * </pre>
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
     * <pre>
     * Example:
     * {@code
     *      When '1' elements exists with 'id:iframeResult'
     *      Then I switch to the iframe on index '0'
     * }
     * </pre>
     * @param index the index
     */
    @Given("^I switch to the iframe on index '(\\d+?)'$")
    public void seleniumSwitchFrame(Integer index) {

        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().size()).as("Could not get webelement with index %s. Less elements were found. Allowed index: 0 to %s", index, commonspec.getPreviousWebElements().getPreviousWebElements().size() - 1)
                .isGreaterThanOrEqualTo(index + 1);

        WebElement elem = commonspec.getPreviousWebElements().getPreviousWebElements().get(index);
        commonspec.getDriver().switchTo().frame(elem);
    }


    /**
     * Switch to the frame/iframe with the given locator
     * <pre>
     * Example:
     * {@code
     *      Then I switch to iframe with 'id:iframeResult'
     * }
     * </pre>
     *
     * @param method  the method (id, class, name, xpath)
     * @param idframe locator
     * @throws IllegalAccessException exception
     * @throws NoSuchFieldException   exception
     * @throws ClassNotFoundException exception
     */
    @Given("^I switch to iframe with '([^:]*?):(.+?)'$")
    public void seleniumIdFrame(String method, String idframe) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        assertThat(commonspec.locateElement(method, idframe, 1));

        Assertions.assertThat(method).as("Can not use '%s' to switch iframe. Use 'id' or 'name'", method).isEqualTo("id").isEqualTo("name");
        commonspec.getDriver().switchTo().frame(idframe);

    }


    /**
     * Switches to a parent frame/iframe.
     * <pre>
     * Example:
     * {@code
     *      Then I switch to a parent frame
     * }
     * </pre>
     */
    @Given("^I switch to a parent frame$")
    public void seleniumSwitchAParentFrame() {
        commonspec.getDriver().switchTo().parentFrame();
    }


    /**
     * Switches to the frames main container.
     * <pre>
     * Example:
     * {@code
     *      Then I switch to the main frame container
     * }
     * </pre>
     */
    @Given("^I switch to the main frame container$")
    public void seleniumSwitchParentFrame() {
        commonspec.getDriver().switchTo().frame(commonspec.getParentWindow());
    }


    /**
     * Get all opened windows and store it.
     * <pre>
     * Example:
     * {@code
     *      Then a new window is opened
     * }
     * </pre>
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
     * <pre>
     * Example:
     * {@code
     *      When '1' elements exists with 'xpath://*[@id="pie_register"]/li[6]/div/label'
     *      And the element on index '0' has 'Phone Number' as text
     * }
     * </pre>
     *
     * @param index the index of the webelement
     * @param text  the text to verify
     */
    @Then("^the element on index '(\\d+?)' has '(.+?)' as text$")
    public void assertSeleniumTextOnElementPresent(Integer index, String text) {
        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().size()).as("Could not get webelement with index %s. Less elements were found. Allowed index: 0 to %s", index, commonspec.getPreviousWebElements().getPreviousWebElements().size() - 1)
                .isGreaterThanOrEqualTo(index + 1);
        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().get(index).getText()).contains(text);
    }


    /**
     * Checks if a text exists in the source of an already loaded URL.
     * <pre>
     * Example:
     * {@code
     *      Then this text exists:
     *      """
     *      <h1 class="entry-title">Home</h1>
     *      """
     * }
     * </pre>
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
     * <pre>
     * Example:
     * {@code
     *      When '1' elements exists with 'id:profile_pic_10'
     * }
     * Elements can be also located using class for example:
     * {@code
     *      When '7' elements exists with 'class:legend_txt'
     * }
     * To verify that the amount of elements is bigger or equal:
     * {@code
     *      Then at least '1' elements exists with 'class:detail-entry'
     * }
     * Elements are stored to be used in subsequent steps:
     * {@code
     *      Given '1' elements exists with 'xpath://*[@id="myBtn"]'
     *      Then I click on the element on index '0'
     * }
     * </pre>
     *
     *
     * @param atLeast       asserts that the amount of elements if greater or equal to expectedCount. If null, asserts the amount of element is equal to expectedCount
     * @param expectedCount the expected count of elements to find
     * @param method        method to locate the elements (id, name, class, css, xpath for regular html elements, and additionally, linkText, partialLinkText and tagName for mobile elements)
     * @param element       the relative reference to the element
     * @throws ClassNotFoundException   the class not found exception
     * @throws NoSuchFieldException     the no such field exception
     * @throws SecurityException        the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException   the illegal access exception
     */
    @Then("^(at least )?'(\\d+?)' elements? exists? with '([^:]*?):(.+?)'$")
    public void assertSeleniumNElementExists(String atLeast, Integer expectedCount, String method, String element) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

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
     * Each negative lookup is followed by a wait of {@code wait} seconds. Selenium times are not accounted for the mentioned timeout.
     * This method is similar to {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     * but uses static wait instead of {@link org.openqa.selenium.support.ui.FluentWait} and does not assert the expected
     * condition of the elements (if elements are visible|hidden|present|clickable)
     * <pre>
     * Example:
     * {@code
     *      Then in less than '20' seconds, checking each '2' seconds, '1' elements exists with 'id:name_3_firstname'
     *      And I click on the element on index '0'
     * }
     * </pre>
     * @param timeout       the max time to wait for the condition to be true
     * @param wait          interval between verification
     * @param expectedCount the expected count of elements
     * @param method        the method
     * @param element       the web element element
     * @throws InterruptedException     the interrupted exception
     * @throws ClassNotFoundException   the class not found exception
     * @throws NoSuchFieldException     the no such field exception
     * @throws SecurityException        the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException   the illegal access exception
     */
    @Then("^in less than '(\\d+?)' seconds, checking each '(\\d+?)' seconds, '(\\d+?)' elements exists with '([^:]*?):(.+?)'$")
    public void assertSeleniumNElementExistsOnTimeOut(Integer timeout, Integer wait, Integer expectedCount,
                                                      String method, String element) throws InterruptedException, ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
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
     * <pre>
     * Example:
     * {@code
     *      When '1' elements exists with 'id:myDIV'
     *      And the element on index '0' IS displayed
     * }
     * Or:
     * {@code
     *      When '1' elements exists with 'id:myDIV'
     *      And the element on index '0' IS NOT displayed
     * }
     * </pre>
     *
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
     * <pre>
     * Example:
     * {@code
     *      When '1' elements exists with 'xpath://*[@id="myBtn"]'
     *      And the element on index '0' IS enabled
     * }
     * Or:
     * {@code
     *      When '1' elements exists with 'xpath://*[@id="myBtn"]'
     *      And the element on index '0' IS NOT enabled
     * }
     * </pre>
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
     * <pre>
     * Example:
     * {@code
     *      When '3' elements exists with 'name:radio_4[]'
     *      And the element on index '0' IS NOT selected
     * }
     * Or:
     * {@code
     *      When '3' elements exists with 'name:radio_4[]'
     *      And the element on index '1' IS selected
     * }
     * </pre>
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
     * Verifies that a webelement previously found has {@code attribute} with {@code value} (as a regexp)
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     *
     * <pre>
     * Example:
     * {@code
     *      When '1' elements exists with 'id:phone_9'
     *      Then the element on index '0' has 'id' as 'phone_9'
     * }
     * </pre>
     * @param index     the index of the web element
     * @param attribute the attribute to verify
     * @param value     the value of the attribute
     */
    @Then("^the element on index '(\\d+?)' has '(.+?)' as '(.+?)'$")
    public void assertSeleniumHasAttributeValue(Integer index, String attribute, String value) {
        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().size()).as("Could not get webelement with index %s. Less elements were found. Allowed index: 0 to %s", index, commonspec.getPreviousWebElements().getPreviousWebElements().size() - 1)
                .isGreaterThanOrEqualTo(index + 1);
        String val = commonspec.getPreviousWebElements().getPreviousWebElements().get(index).getAttribute(attribute);
        Assertions.assertThat(val).as("Attribute not found").isNotNull();
        Assertions.assertThat(val).as("Unexpected value for specified attribute").matches(value);
    }


    /**
     * Takes an snapshot of the current page.
     * <p>
     * Snapshots are stored under target/executions
     * <pre>
     * Example:
     * {@code
     *      Then I take a snapshot
     * }
     * </pre>
     */
    @Then("^I take a snapshot$")
    public void seleniumSnapshot() {
        commonspec.captureEvidence(commonspec.getDriver(), "screenCapture");
    }


    /**
     * Checks that we are in the URL passed
     * <pre>
     * Example:
     * {@code
     *      Given I go to 'https://demoqa.com/'
     *      Then we are in page 'https://demoqa.com/'
     * }
     * </pre>
     *
     * @param url the url to verify
     */
    @Then("^we are in page '(.+?)'$")
    public void checkURL(String url) {
        Assertions.assertThat(commonspec.getDriver().getCurrentUrl()).as("We are not in the expected url").isEqualTo(url);
    }


    /**
     * Checks if the current URL contains the specified text
     * <pre>
     * Example:
     * {@code
     *      Given My app is running in 'demoqa.com:80'
     *      And I browse to '/autocomplete'
     *      Then the current url contains the text 'autocomplete'
     * }
     * </pre>
     * @param text  Text to look for in the current url
     */
    @Then("^the current url contains the text '(.+?)'$")
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
     * <pre>
     * Example:
     * {@code
     *      When '1' elements exists with 'id:my_text_field'
     *      I save content of element in index '0' in environment variable 'mytext'
     * }
     * </pre>
     *
     * @param index  position of the element in the array of webElements found
     * @param envVar name of the thread environment variable where to store the text
     */
    @Then("^I save content of element in index '(\\d+?)' in environment variable '(.+?)'$")
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
     * <pre>
     * Example:
     * {@code
     *      And '3' elements exists with 'css:input[name='radio_4[]']'
     *      Then the element in index '1' has 'radio_4[]' in property 'name'
     * }
     * </pre>
     *
     * @param index          Position of the element in the array of webElements
     * @param textValue      Value expected in the property
     * @param customProperty Property of webElement to verify
     */
    @Then("^the element in index '(.+?)' has '(.+?)' in property '(.+?)'$")
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
     * @throws ClassNotFoundException   ClassNotFoundException
     * @throws NoSuchFieldException     NoSuchFieldException
     * @throws SecurityException        SecurityException
     * @throws IllegalArgumentException IllegalArgumentException
     * @throws IllegalAccessException   IllegalAccessException
     */
    @When("^I drag '([^:]*?):(.+?)' and drop it to '([^:]*?):(.+?)'$")
    public void seleniumDrag(String smethod, String source, String dmethod, String destination) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Actions builder = new Actions(commonspec.getDriver());

        List<WebElement> sourceElement = commonspec.locateElement(smethod, source, 1);
        List<WebElement> destinationElement = commonspec.locateElement(dmethod, destination, 1);

        builder.dragAndDrop(sourceElement.get(0), destinationElement.get(0)).perform();
    }


    /**
     * Click on an numbered {@code url} previously found element.
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     *
     * <pre>
     * Example:
     * {@code
     *      When '1' elements exists with 'xpath://*[@id="name_3_lastname"]'
     *      And I click on the element on index '0'
     * }
     * </pre>
     *
     * @param index Index of the webelement in the list
     */
    @When("^I click on the element on index '(\\d+?)'$")
    public void seleniumClick(Integer index) {

        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().size()).as("Could not get webelement with index %s. Less elements were found. Allowed index: 0 to %s", index, commonspec.getPreviousWebElements().getPreviousWebElements().size() - 1)
                .isGreaterThanOrEqualTo(index + 1);
        commonspec.getPreviousWebElements().getPreviousWebElements().get(index).click();
    }


    /**
     * Clear the text on a numbered {@code index} previously found element.
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     *
     * <pre>
     * Example:
     * {@code
     *      Then the element on index '0' has 'id' as 'phone_9'
     *      And I type '555-555' on the element on index '0'
     *      And I clear the content on text input at index '0'
     * }
     * </pre>
     *
     * @param index index of the web element
     */
    @When("^I clear the content on text input at index '(\\d+?)'$")
    public void seleniumClear(Integer index) {
        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().size()).as("Could not get webelement with index %s. Less elements were found. Allowed index: 0 to %s", index, commonspec.getPreviousWebElements().getPreviousWebElements().size() - 1)
                .isGreaterThanOrEqualTo(index + 1);

        Assertions.assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().get(index)).is(commonspec.getTextFieldCondition());

        commonspec.getPreviousWebElements().getPreviousWebElements().get(index).clear();
    }


    /**
     * Type a {@code text} on an numbered {@code index} previously found element.
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     *
     * <pre>
     * Example:
     * {@code
     *      Then the element on index '0' has 'id' as 'phone_9'
     *      And I type '555-555' on the element on index '0'
     *      And I clear the content on text input at index '0'
     * }
     * </pre>
     *
     * @param input Text to write on the element
     * @param index Index of the webelement in the list
     */
    @When("^I type '(.+?)' on the element on index '(\\d+?)'$")
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
     * Send a {@code strokes} list on an numbered {@code url} previously found element or to the driver.
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
     * <pre>
     * Example:
     * {@code
     *      When '1' elements exists with 'id:name_3_firstname'
     *      Then I type 'testUser' on the element on index '0'
     *      Then I send 'ENTER' on the element on index '0'
     * }
     * </pre>
     *
     * @param text  key stroke to send
     * @param index index of the web element in the list
     */
    @When("^I send '(.+?)'( on the element on index '(\\d+?)')?$")
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
     * Choose an @{code option} from a select webelement found previously
     * <p>
     * This step requires a previous operation for finding elements to have been executed, such as: <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExists(String, Integer, String, String)} <br>
     * {@link SeleniumGSpec#assertSeleniumNElementExistsOnTimeOut(Integer, Integer, Integer, String, String)} <br>
     * {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     *
     * @param option option in the select element
     * @param index  index of the web element in the list
     */
    @When("^I select '(.+?)' on the element on index '(\\d+?)'$")
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
    @When("^I de-select every item on the element on index '(\\d+?)'$")
    public void elementDeSelect(Integer index) {
        Select sel = null;
        sel = new Select(commonspec.getPreviousWebElements().getPreviousWebElements().get(index));

        if (sel.isMultiple()) {
            sel.deselectAll();
        }
    }


    /**
     * Change current window to another opened window.
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
     * <pre>
     * Example:
     * {@code
     *      Then '1' elements exists with 'id:menu-item-146'
     *      Then I save the value of the property 'class' of the element in index '0' in variable 'VAR2'
     * }
     * </pre>
     * @param propertyName Name of the property
     * @param index        Index of the webelement in the list
     * @param variable     Variable where to save the result
     */
    @Then("^I save the value of the property '(.+?)' of the element in index '(.+?)' in variable '(.+?)'$")
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
     * <pre>
     * Example:
     * {@code
     *      And I execute 'alert("This is an alert!")' as javascript
     * }
     * Javascript functions can be executed on previous found elements:
     * {@code
     *      And '1' elements exists with 'xpath://*[@id="menu-item-158"]/a'
     *      And I execute 'arguments[0].click();' as javascript on the element on index '0'
     * }
     * </pre>
     *
     * @param script Script to execute (i.e alert("This is an alert message"))
     * @param index  If used, the index of the previously found web element on which to execute the function
     * @param enVar  if used, variable where to store the result of the execution of the script
     */
    @Then("^I execute '(.+?)' as javascript( on the element on index '(.+?)')?( and save the result in the environment variable '(.+?)')?$")
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
     * Directly navigate go to the specified url
     * <p>
     * This step is a similar way of navigating to a web page by specifying the
     * full url directly, instead of first setting the base path with {@link #setupApp(String)}
     * and later navigate with {@link #seleniumBrowse(String, String)}
     *
     * <pre>
     * Example:
     * {@code
     *      Given I go to 'http://www.demoqa.com/autocomplete'
     * }
     * You can also do it like this:
     * {@code
     *      Given My app is running in 'demoqa.com:80'
     *      And I browse to '/autocomplete'
     * }
     * </pre>
     *
     * @param url   Url were to navigate
     */
    @Given("I go to '(.+?)'")
    public void iGoToUrl(String url) {
        commonspec.getDriver().get(url);
        commonspec.setParentWindow(commonspec.getDriver().getWindowHandle());
    }
}
