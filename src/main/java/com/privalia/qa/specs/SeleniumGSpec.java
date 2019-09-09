package com.privalia.qa.specs;

import com.privalia.qa.cucumber.converter.ArrayListConverter;
import com.privalia.qa.cucumber.converter.NullableStringConverter;
import com.privalia.qa.utils.PreviousWebElements;
import com.privalia.qa.utils.ThreadProperty;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
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
     * Set app host and port
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
     *
     * @param isSecured If the connection should be secured
     * @param path      path of running app
     * @throws Exception exception
     */
    @Given("^I( securely)? browse to '(.+?)'$")
    public void seleniumBrowse(String isSecured, String path) throws Exception {
        assertThat(path).isNotEmpty();

        if (commonspec.getWebHost() == null) {
            throw new Exception("Web host has not been set");
        }

        if (commonspec.getWebPort() == null) {
            throw new Exception("Web port has not been set");
        }
        String protocol = "http://";
        if (isSecured != null) {
            protocol = "https://";
        }

        String webURL = protocol + commonspec.getWebHost() + commonspec.getWebPort();

        commonspec.getDriver().get(webURL + path);
        commonspec.setParentWindow(commonspec.getDriver().getWindowHandle());
    }


    /**
     * Checks that a web elements exists in the page and is of the type specified. This method is similar to {@link SeleniumGSpec#assertSeleniumNElementExists(Integer, String, String)}
     * but implements a pooling mechanism with a maximum pooling time instead of a static wait
     * @param poolingInterval   Time between consecutive condition evaluations
     * @param poolMaxTime       Maximum time to wait for the condition to be true
     * @param elementsCount     integer. Expected number of elements.
     * @param method            class of element to be searched
     * @param element           webElement searched in selenium context
     * @param type              The expected style of the element: visible, clickable, present, hidden
     * @throws Throwable        Throwable
     */
    @Then("^I check every '(\\d+)' seconds for at least '(\\d+)' seconds until '(\\d+)' elements exists with '([^:]*?):(.+?)' and is '(visible|clickable|present|hidden)'$")
    public void waitWebElementWithPooling(int poolingInterval, int poolMaxTime, int elementsCount, String method, String element, String type) throws Throwable {
        List<WebElement> wel = commonspec.locateElementWithPooling(poolingInterval, poolMaxTime, method, element, elementsCount, type);
        PreviousWebElements pwel = new PreviousWebElements(wel);
        commonspec.setPreviousWebElements(pwel);
    }

    /**
     * Checks if an alert message is open in the current page. The function implements a pooling interval to check if the condition is true
     * @param poolingInterval   Time between consecutive condition evaluations
     * @param poolMaxTime       Maximum time to wait for the condition to be true
     * @throws Throwable        Throwable
     */
    @Then("^I check every '(\\d+)' seconds for at least '(\\d+)' seconds until an alert appears$")
    public void waitAlertWithPooling(int poolingInterval, int poolMaxTime) throws Throwable {
        Alert alert = commonspec.waitAlertWithPooling(poolingInterval, poolMaxTime);
        commonspec.setSeleniumAlert(alert);
    }

    /**
     * Accepts an alert message previously found
     * @throws Throwable    Throwable
     */
    @Then("^I dismiss the alert$")
    public void iAcceptTheAlert() throws Throwable {
        commonspec.dismissSeleniumAlert();
    }

    /**
     * Dismiss an alert message previously found
     * @throws Throwable    Throwable
     */
    @Then("^I accept the alert$")
    public void iDismissTheAlert() throws Throwable {
        commonspec.acceptSeleniumAlert();
    }

    /**
     * Assigns the given file (relative to schemas/) to the web elements in the given index. This step
     * is suitable for file selectors/file pickers (an input type=file), where the user must specify a
     * file in the local computer as an input in a form
     * @param fileName      Name of the file relative to schemas folder (schemas/myFile.txt)
     * @param index         Index of the web element (file input)
     * @throws Throwable    Throwable
     */
    @Then("^I assign the file in '(.+?)' to the element on index '(\\d+)'$")
    public void iSetTheFileInSchemasEmptyJsonToTheElementOnIndex(String fileName, int index) throws Throwable {

        //Get file absolute path
        String filePath = getClass().getClassLoader().getResource(fileName).getPath();

        //Assign the file absolute path to the file picker element previously set
        File f = new File(filePath);
        assertThat(this.getCommonSpec(), f.exists()).as("The file located in " + filePath + " does not exists or is not accesible").isEqualTo(true);
        commonspec.getPreviousWebElements().getPreviousWebElements().get(index).sendKeys(filePath);
    }


    /**
     * Maximizes current browser window. Mind the current resolution could break a test.
     */
    @Given("^I maximize the browser$")
    public void seleniumMaximize() {
        commonspec.getDriver().manage().window().maximize();
    }


    /**
     * Switches to a frame/ iframe.
     *
     * @param index the index
     */
    @Given("^I switch to the iframe on index '(\\d+?)'$")
    public void seleniumSwitchFrame(Integer index) {

        assertThat(commonspec.getPreviousWebElements()).as("There are less found elements than required")
                .hasAtLeast(index);

        WebElement elem = commonspec.getPreviousWebElements().getPreviousWebElements().get(index);
        commonspec.getDriver().switchTo().frame(elem);
    }


    /**
     * Swith to the iFrame where id matches idframe
     *
     * @param method  the method
     * @param idframe iframe to swith to
     * @throws IllegalAccessException exception
     * @throws NoSuchFieldException   exception
     * @throws ClassNotFoundException exception
     */
    @Given("^I switch to iframe with '([^:]*?):(.+?)'$")
    public void seleniumIdFrame(String method, String idframe) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        assertThat(commonspec.locateElement(method, idframe, 1));

        if (method.equals("id") || method.equals("name")) {
            commonspec.getDriver().switchTo().frame(idframe);
        } else {
            throw new ClassNotFoundException("Can not use this method to switch iframe");
        }
    }


    /**
     * Switches to a parent frame/ iframe.
     */
    @Given("^I switch to a parent frame$")
    public void seleniumSwitchAParentFrame() {
        commonspec.getDriver().switchTo().parentFrame();
    }


    /**
     * Switches to the frames main container.
     */
    @Given("^I switch to the main frame container$")
    public void seleniumSwitchParentFrame() {
        commonspec.getDriver().switchTo().frame(commonspec.getParentWindow());
    }


    /**
     * Get all opened windows and store it.
     */
    @Given("^a new window is opened$")
    public void seleniumGetwindows() {
        Set<String> wel = commonspec.getDriver().getWindowHandles();

        assertThat(wel).as("Element count doesnt match").hasSize(2);
    }


    /**
     * Verifies that a webelement previously found has {@code text} as text
     *
     * @param index the index of the webelement
     * @param text  the text to verify
     */
    @Then("^the element on index '(\\d+?)' has '(.+?)' as text$")
    public void assertSeleniumTextOnElementPresent(Integer index, String text) {
        assertThat(commonspec.getPreviousWebElements()).as("There are less found elements than required")
                .hasAtLeast(index);
        assertThat(commonspec.getPreviousWebElements().getPreviousWebElements().get(index)).contains(text);
    }


    /**
     * Checks if a text exists in the source of an already loaded URL.
     *
     * @param text  the text to verify
     */
    @Then("^this text exists:$")
    public void assertSeleniumTextInSource(String text) {
        assertThat(this.commonspec, commonspec.getDriver()).as("Expected text not found at page").contains(text);
    }


    /**
     * Checks if {@code expectedCount} webelements are found, with a location {@code method}.
     *
     * @param expectedCount             the expected count
     * @param method                    the method
     * @param element                   the element
     * @throws ClassNotFoundException   the class not found exception
     * @throws NoSuchFieldException     the no such field exception
     * @throws SecurityException        the security exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalAccessException   the illegal access exception
     */
    @Then("^'(\\d+?)' elements? exists? with '([^:]*?):(.+?)'$")
    public void assertSeleniumNElementExists(Integer expectedCount, String method, String element) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        List<WebElement> wel = commonspec.locateElement(method, element, expectedCount);
        PreviousWebElements pwel = new PreviousWebElements(wel);
        commonspec.setPreviousWebElements(pwel);
    }


    /**
     * Checks if {@code expectedCount} webelements are found, whithin a {@code timeout} and with a location
     * {@code method}. Each negative lookup is followed by a wait of {@code wait} seconds. Selenium times are not
     * accounted for the mentioned timeout.
     *
     * @param timeout                   the max time to wait for the condition to be true
     * @param wait                      interval between verification
     * @param expectedCount             the expected count of elements
     * @param method                    the method
     * @param element                   the web element element
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
        assertThat(this.commonspec, pwel).as("Element count doesnt match").hasSize(expectedCount);
        commonspec.setPreviousWebElements(pwel);

    }


    /**
     * Verifies that a webelement previously found {@code isDisplayed}
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

        assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                .hasAtLeast(index);
        assertThat(this.commonspec, commonspec.getPreviousWebElements().getPreviousWebElements().get(index).isDisplayed()).as(
                "Unexpected element display property").isEqualTo(isDisplayed);
    }


    /**
     * Verifies that a webelement previously found {@code isEnabled}
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

        assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                .hasAtLeast(index);
        assertThat(this.commonspec, commonspec.getPreviousWebElements().getPreviousWebElements().get(index).isEnabled())
                .as("Unexpected element enabled property").isEqualTo(isEnabled);
    }


    /**
     * Verifies that a webelement previously found {@code isSelected}
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

        assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                .hasAtLeast(index);
        assertThat(this.commonspec, commonspec.getPreviousWebElements().getPreviousWebElements().get(index).isSelected()).as(
                "Unexpected element selected property").isEqualTo(isSelected);
    }


    /**
     * Verifies that a webelement previously found has {@code attribute} with {@code value} (as a regexp)
     *
     * @param index     the index of the web element
     * @param attribute the attribute to verify
     * @param value     the value of the attribute
     */
    @Then("^the element on index '(\\d+?)' has '(.+?)' as '(.+?)'$")
    public void assertSeleniumHasAttributeValue(Integer index, String attribute, String value) {
        assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                .hasAtLeast(index);
        String val = commonspec.getPreviousWebElements().getPreviousWebElements().get(index).getAttribute(attribute);
        assertThat(this.commonspec, val).as("Attribute not found").isNotNull();
        assertThat(this.commonspec, val).as("Unexpected value for specified attribute").matches(value);
    }


    /**
     * Takes an snapshot of the current page
     *
     * @throws Exception    Exception
     */
    @Then("^I take a snapshot$")
    public void seleniumSnapshot() throws Exception {
        commonspec.captureEvidence(commonspec.getDriver(), "screenCapture");
    }


    /**
     * Checks that we are in the URL passed
     *
     * @param url           the url to verify
     * @throws Exception    Exception
     */
    @Then("^we are in page '(.+?)'$")
    public void checkURL(String url) throws Exception {

        if (commonspec.getWebHost() == null) {
            throw new Exception("Web host has not been set");
        }

        if (commonspec.getWebPort() == null) {
            throw new Exception("Web port has not been set");
        }

        String webURL = commonspec.getWebHost();

        assertThat(commonspec.getDriver().getCurrentUrl()).as("We are not in the expected url: " + webURL.toLowerCase() + url)
                .endsWith(webURL.toLowerCase() + url);
    }


    /**
     * Save cookie in context for future references
     *
     * @throws Exception Exception
     */
    @Then("^I save selenium cookies in context$")
    public void saveSeleniumCookies() throws Exception {
        commonspec.setSeleniumCookies(commonspec.getDriver().manage().getCookies());
    }


    /**
     * Takes the content of a webElement and stores it in the thread environment variable passed as parameter
     *
     * @param index  position of the element in the array of webElements found
     * @param envVar name of the thread environment variable where to store the text
     */
    @Then("^I save content of element in index '(\\d+?)' in environment variable '(.+?)'$")
    public void saveContentWebElementInEnvVar(Integer index, String envVar) {
        assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                .hasAtLeast(index);
        String text = commonspec.getPreviousWebElements().getPreviousWebElements().get(index).getText();
        ThreadProperty.set(envVar, text);
    }


    /**
     * Verifies if the value of a property of the webelement referenced by index matches the given value
     *
     * @param index                     Position of the element in the array of webElements
     * @param textValue                 Value expected in the property
     * @param customProperty            Property of webElement to verify
     * @throws IllegalAccessException   IllegalAccessException
     * @throws NoSuchFieldException     NoSuchFieldException
     * @throws ClassNotFoundException   ClassNotFoundException
     */
    @Then("^the element in index '(.+?)' has '(.+?)' in property '(.+?)'$")
    public void theElementOnIndexHasTextInCustomPropertyName(int index, String textValue, String customProperty) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {

        List<WebElement> wel = commonspec.getPreviousWebElements().getPreviousWebElements();
        assertThat(wel.size()).as("The last step did not find elements").isNotZero();

        String value = wel.get(index) .getAttribute(customProperty);
        assertThat(value).as("The element doesn't have the property '%s'", customProperty).isNotEmpty().isNotNull();

        assertThat(value).as("The property '%s' doesn't have the text '%s'", customProperty, textValue).isEqualToIgnoringCase(textValue);

    }


    /**
     * Searchs for two webelements dragging the first one to the second
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
     *
     * @param index                     Index of the webelement in the list
     * @throws InterruptedException     InterruptedException
     */
    @When("^I click on the element on index '(\\d+?)'$")
    public void seleniumClick(Integer index) throws InterruptedException {

        try {
            assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                    .hasAtLeast(index);
            commonspec.getPreviousWebElements().getPreviousWebElements().get(index).click();
        } catch (AssertionError e) {
            Thread.sleep(1000);
            assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                    .hasAtLeast(index);
            commonspec.getPreviousWebElements().getPreviousWebElements().get(index).click();
        }
    }


    /**
     * Clear the text on a numbered {@code index} previously found element.
     *
     * @param index     index of the web element
     */
    @When("^I clear the content on text input at index '(\\d+?)'$")
    public void seleniumClear(Integer index) {
        assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                .hasAtLeast(index);

        assertThat(this.commonspec, commonspec.getPreviousWebElements().getPreviousWebElements().get(index)).isTextField(commonspec.getTextFieldCondition());

        commonspec.getPreviousWebElements().getPreviousWebElements().get(index).clear();
    }


    /**
     * Type a {@code text} on an numbered {@code index} previously found element.
     *
     * @param input      Text to write on the element
     * @param index     Index of the webelement in the list
     */
    @When("^I type '(.+?)' on the element on index '(\\d+?)'$")
    public void seleniumType(String input, Integer index) {

        NullableStringConverter converter = new NullableStringConverter();
        String text = converter.transform(input);

        assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                .hasAtLeast(index);
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
     * Send a {@code strokes} list on an numbered {@code url} previously found element or to the driver. strokes examples are "HOME, END"
     * or "END, SHIFT + HOME, DELETE". Each element in the stroke list has to be an element from
     * {@link org.openqa.selenium.Keys} (NULL, CANCEL, HELP, BACK_SPACE, TAB, CLEAR, RETURN, ENTER, SHIFT, LEFT_SHIFT,
     * CONTROL, LEFT_CONTROL, ALT, LEFT_ALT, PAUSE, ESCAPE, SPACE, PAGE_UP, PAGE_DOWN, END, HOME, LEFT, ARROW_LEFT, UP,
     * ARROW_UP, RIGHT, ARROW_RIGHT, DOWN, ARROW_DOWN, INSERT, DELETE, SEMICOLON, EQUALS, NUMPAD0, NUMPAD1, NUMPAD2,
     * NUMPAD3, NUMPAD4, NUMPAD5, NUMPAD6, NUMPAD7, NUMPAD8, NUMPAD9, MULTIPLY, ADD, SEPARATOR, SUBTRACT, DECIMAL,
     * DIVIDE, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, META, COMMAND, ZENKAKU_HANKAKU) , a plus sign (+), a
     * comma (,) or spaces ( )
     *
     * @param text       key stroke to send
     * @param index         index of the web element in the list
     */
    @When("^I send '(.+?)'( on the element on index '(\\d+?)')?$")
    public void seleniumKeys(String text, Integer index) {

        ArrayListConverter converter = new ArrayListConverter();
        List<String> strokes = converter.transform(text);

        if (index != null) {
            assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                    .hasAtLeast(index);
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
     *
     * @param option    option in the select element
     * @param index     index of the web element in the list
     */
    @When("^I select '(.+?)' on the element on index '(\\d+?)'$")
    public void elementSelect(String option, Integer index) {
        Select sel = null;
        sel = new Select(commonspec.getPreviousWebElements().getPreviousWebElements().get(index));

        sel.selectByVisibleText(option);
    }


    /**
     * Choose no option from a select webelement found previously
     *
     * @param index     index of the web element in the list
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
}
