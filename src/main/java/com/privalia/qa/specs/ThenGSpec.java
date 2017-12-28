/*
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.privalia.qa.specs;

import com.privalia.qa.utils.PreviousWebElements;
import com.privalia.qa.utils.ThreadProperty;
import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import gherkin.formatter.model.DataTableRow;
import org.assertj.core.api.Fail;
import org.assertj.core.api.WritableAssertionInfo;
import org.json.JSONArray;
import org.junit.Assert;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static com.privalia.qa.assertions.Assertions.assertThat;


/**
 * Generic Then Specs.
 * @see <a href="ThenGSpec-annotations.html">Then Steps &amp; Matching Regex</a>
 */
public class ThenGSpec extends BaseGSpec {


    /**
     * Class constructor.
     *
     * @param spec
     */
    public ThenGSpec(CommonG spec) {
        this.commonspec = spec;
    }

    /**
     * Checks if an exception has been thrown.
     *
     * @param exception    : "IS NOT" | "IS"
     * @param foo
     * @param clazz
     * @param bar
     * @param exceptionMsg
     */
    @Then("^an exception '(.+?)' thrown( with class '(.+?)'( and message like '(.+?)')?)?")
    public void assertExceptionNotThrown(String exception, String foo, String clazz, String bar, String exceptionMsg)
            throws ClassNotFoundException {
        List<Exception> exceptions = commonspec.getExceptions();
        if ("IS NOT".equals(exception)) {
            assertThat(exceptions).as("Captured exception list is not empty").isEmpty();
        } else {
            assertThat(exceptions).as("Captured exception list is empty").isNotEmpty();
            Exception ex = exceptions.get(exceptions.size() - 1);
            if ((clazz != null) && (exceptionMsg != null)) {
                assertThat(ex.toString()).as("Unexpected last exception class").contains(clazz);
                assertThat(ex.toString()).as("Unexpected last exception message").contains(exceptionMsg);

            } else if (clazz != null) {
                assertThat(exceptions.get(exceptions.size() - 1).getClass().getSimpleName()).as("Unexpected last exception class").isEqualTo(clazz);
            }

            commonspec.getExceptions().clear();
        }
    }

    /**
     * Verifies that a webelement previously found has {@code text} as text
     *
     * @param index
     * @param text
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
     * @param text
     */
    @Then("^this text exists:$")
    public void assertSeleniumTextInSource(String text) {
        assertThat(this.commonspec, commonspec.getDriver()).as("Expected text not found at page").contains(text);
    }

    /**
     * Checks if {@code expectedCount} webelements are found, with a location {@code method}.
     *
     * @param expectedCount
     * @param method
     * @param element
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException
     */
    @Then("^'(\\d+?)' elements? exists? with '([^:]*?):([^:]*?)'$")
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
     * @param timeout
     * @param wait
     * @param expectedCount
     * @param method
     * @param element
     * @throws InterruptedException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException
     */
    @Then("^in less than '(\\d+?)' seconds, checking each '(\\d+?)' seconds, '(\\d+?)' elements exists with '([^:]*?):([^:]*?)'$")
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
     * Checks if {@code expectedCount} element is found, whithin a {@code timeout} and with a location
     * {@code method}. Each negative lookup is followed by a wait of {@code wait} seconds. Selenium times are not
     * accounted for the mentioned timeout.
     *
     * @param timeout
     * @param wait
     * @param command
     * @param search
     * @throws InterruptedException
     */
    @Then("^in less than '(\\d+?)' seconds, checking each '(\\d+?)' seconds, the command output '(.+?)' contains '(.+?)'$")
    public void assertCommandExistsOnTimeOut(Integer timeout, Integer wait, String command, String search) throws Exception {
        Boolean found = false;
        AssertionError ex = null;

        for (int i = 0; (i <= timeout); i += wait) {
            if (found) {
                break;
            }
            commonspec.getLogger().debug("Checking output value");
            commonspec.getRemoteSSHConnection().runCommand(command);
            commonspec.setCommandResult(commonspec.getRemoteSSHConnection().getResult());
            try {
                assertThat(commonspec.getCommandResult()).as("Contains " + search + ".").contains(search);
                found = true;
                timeout = i;
            } catch (AssertionError e) {
                commonspec.getLogger().info("Command output don't found yet after " + i + " seconds");
                Thread.sleep(wait * 1000);
                ex = e;
            }
        }
        if (!found) {
            throw (ex);
        }
        commonspec.getLogger().info("Command output found after " + timeout + " seconds");
    }

    /**
     * Verifies that a webelement previously found {@code isDisplayed}
     *
     * @param index
     * @param isDisplayed
     */
    @Then("^the element on index '(\\d+?)' (IS|IS NOT) displayed$")
    public void assertSeleniumIsDisplayed(Integer index, Boolean isDisplayed) {
        assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                .hasAtLeast(index);
        assertThat(this.commonspec, commonspec.getPreviousWebElements().getPreviousWebElements().get(index).isDisplayed()).as(
                "Unexpected element display property").isEqualTo(isDisplayed);
    }

    /**
     * Verifies that a webelement previously found {@code isEnabled}
     *
     * @param index
     * @param isEnabled
     */
    @Then("^the element on index '(\\d+?)' (IS|IS NOT) enabled$")
    public void assertSeleniumIsEnabled(Integer index, Boolean isEnabled) {
        assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                .hasAtLeast(index);
        assertThat(this.commonspec, commonspec.getPreviousWebElements().getPreviousWebElements().get(index).isEnabled())
                .as("Unexpected element enabled property").isEqualTo(isEnabled);
    }

    /**
     * Verifies that a webelement previously found {@code isSelected}
     *
     * @param index
     * @param isSelected
     */
    @Then("^the element on index '(\\d+?)' (IS|IS NOT) selected$")
    public void assertSeleniumIsSelected(Integer index, Boolean isSelected) {
        assertThat(this.commonspec, commonspec.getPreviousWebElements()).as("There are less found elements than required")
                .hasAtLeast(index);
        assertThat(this.commonspec, commonspec.getPreviousWebElements().getPreviousWebElements().get(index).isSelected()).as(
                "Unexpected element selected property").isEqualTo(isSelected);
    }

    /**
     * Verifies that a webelement previously found has {@code attribute} with {@code value} (as a regexp)
     *
     * @param index
     * @param attribute
     * @param value
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
     * @throws Exception
     */
    @Then("^I take a snapshot$")
    public void seleniumSnapshot() throws Exception {
        commonspec.captureEvidence(commonspec.getDriver(), "screenCapture");
    }

    /**
     * Checks that we are in the URL passed
     *
     * @param url
     * @throws Exception
     */
    @Then("^we are in page '(.+?)'$")
    public void checkURL(String url) throws Exception {

        if (commonspec.getWebHost() == null) {
            throw new Exception("Web host has not been set");
        }

        if (commonspec.getWebPort() == null) {
            throw new Exception("Web port has not been set");
        }

        String webURL = commonspec.getWebHost() + commonspec.getWebPort();

        assertThat(commonspec.getDriver().getCurrentUrl()).as("We are not in the expected url: " + webURL.toLowerCase() + url)
                .endsWith(webURL.toLowerCase() + url);
    }

    @Then("^the service response must contain the text '(.*?)'$")
    public void assertResponseMessage(String expectedText) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Pattern pattern = CommonG.matchesOrContains(expectedText);
        assertThat(commonspec.getResponse().getResponse()).containsPattern(pattern);
    }

    /**
     * Verifies the HTTP code of the service response. Additionally, the step can also verify if the returned response
     * has an specific length, contains a string or matches a json schema.
     * @param expectedStatus    Expected HTTP response code
     * @param foo               regex needed to match method
     * @param expectedLength    Expected lenght of the response
     * @param expectedText      Text to look for in the response
     * @param expectedSchema    Json schema to match the response (i.e. schemas/test-schema.json)
     */
    @Then("^the service response status must be '(.*?)'( and its response length must be '(.*?)'| and its response must contain the text '(.*?)'| and its response matches the schema in '(.*?)')?$")
    public void assertResponseStatusLength(Integer expectedStatus, String foo, Integer expectedLength, String expectedText, String expectedSchema) {
        if (foo != null) {
            if (foo.contains("length")) {
                assertThat(Optional.of(commonspec.getResponse())).hasValueSatisfying(r -> {
                    assertThat(r.getStatusCode()).isEqualTo(expectedStatus);
                    assertThat((r.getResponse()).length()).isEqualTo(expectedLength);
                });
            } else  if (foo.contains("text")) {
                WritableAssertionInfo assertionInfo = new WritableAssertionInfo();
                Pattern pattern = CommonG.matchesOrContains(expectedText);
                assertThat(Optional.of(commonspec.getResponse())).hasValueSatisfying(r -> {
                    assertThat(r.getStatusCode()).isEqualTo(expectedStatus);
                    assertThat(r.getResponse()).containsPattern(pattern);
                });
            } else if (foo.contains("schema")) {
                assertThat(commonspec.getResponse().getStatusCode()).isEqualTo(expectedStatus);
                String responseBody = commonspec.getResponse().getResponse();
                String schemaData = commonspec.retrieveData(expectedSchema, "json");

                /*The following assert uses the typical junit Assert (org.junit.Assert) instead of assertj (used abode)*/
                Assert.assertThat(responseBody, matchesJsonSchema(schemaData));

            }
        } else {
            assertThat(commonspec.getResponse().getStatusCode()).isEqualTo(expectedStatus);
        }
    }

    /**
     * Checks the different results of a previous query
     *
     * @param expectedResults A DataTable Object with all data needed for check the results. The DataTable must contains at least 2 columns:
     *                        a) A field column from the result
     *                        b) Occurrences column (Integer type)
     *                        <p>
     *                        Example:
     *                        |latitude| longitude|place     |occurrences|
     *                        |12.5    |12.7      |Valencia  |1           |
     *                        |2.5     | 2.6      |Stratio   |0           |
     *                        |12.5    |13.7      |Sevilla   |1           |
     *                        IMPORTANT: There no should be no existing columns
     * @throws Exception
     */
    @Then("^There are results found with:$")
    public void resultsMustBe(DataTable expectedResults) throws Exception {

        String type = commonspec.getResultsType();
        assertThat(type).isNotEqualTo("").overridingErrorMessage("It's necessary to define the result type");
        switch (type) {
            case "cassandra":
                commonspec.resultsMustBeCassandra(expectedResults);
                break;
            case "mongo":
                commonspec.resultsMustBeMongo(expectedResults);
                break;
            case "elasticsearch":
                commonspec.resultsMustBeElasticsearch(expectedResults);
                break;
            case "csv":
                commonspec.resultsMustBeCSV(expectedResults);
                break;
            default:
                commonspec.getLogger().warn("default switch branch on results check");
        }
    }

    /**
     * Check the existence of a text at a command output
     *
     * @param search
     **/
    @Then("^the command output contains '(.+?)'$")
    public void findShellOutput(String search) throws Exception {
        assertThat(commonspec.getCommandResult()).as("Contains " + search + ".").contains(search);
    }

    /**
     * Check the non existence of a text at a command output
     *
     * @param search
     **/
    @Then("^the command output does not contain '(.+?)'$")
    public void notFindShellOutput(String search) throws Exception {
        assertThat(commonspec.getCommandResult()).as("NotContains " + search + ".").doesNotContain(search);
    }

    /**
     * Check the exitStatus of previous command execution matches the expected one
     *
     * @param expectedExitStatus
     * @deprecated Success exit status is directly checked in the "execute remote command" method, so this is not
     * needed anymore.
     **/
    @Deprecated
    @Then("^the command exit status is '(.+?)'$")
    public void checkShellExitStatus(int expectedExitStatus) throws Exception {
        assertThat(commonspec.getCommandExitStatus()).as("Is equal to " + expectedExitStatus + ".").isEqualTo(expectedExitStatus);
    }

    /**
     * Save cookie in context for future references
     **/
    @Then("^I save selenium cookies in context$")
    public void saveSeleniumCookies() throws Exception {
        commonspec.setSeleniumCookies(commonspec.getDriver().manage().getCookies());
    }

    /**
     * Check if expression defined by JSOPath (http://goessner.net/articles/JsonPath/index.html)
     * match in JSON stored in a environment variable.
     *
     * @param envVar environment variable where JSON is stored
     * @param table  data table in which each row stores one expression
     */
    @Then("^'(.+?)' matches the following cases:$")
    public void matchWithExpresion(String envVar, DataTable table) throws Exception {
        String jsonString = ThreadProperty.get(envVar);

        for (DataTableRow row : table.getGherkinRows()) {
            String expression = row.getCells().get(0);
            String condition = row.getCells().get(1);
            String result = row.getCells().get(2);

            String value = commonspec.getJSONPathString(jsonString, expression, null);
            commonspec.evaluateJSONElementOperation(value, condition, result);
        }
    }

    /*
     * Check value stored in environment variable "is|matches|is higher than|is lower than|contains|is different from" to value provided
     *
     * @param envVar
     * @param value
     *
     */
    @Then("^'(?s)(.+?)' ((?!.*with).+?) '(.+?)'$")
    public void checkValue(String envVar, String operation, String value) throws Exception {
        switch (operation.toLowerCase()) {
            case "is":
                assertThat(envVar).isEqualTo(value);
                break;
            case "matches":
                assertThat(envVar).matches(value);
                break;
            case "is higher than":
                if (envVar.matches("^-?\\d+$") && value.matches("^-?\\d+$")) {
                    assertThat(Integer.parseInt(envVar)).isGreaterThan(Integer.parseInt(value));
                } else {
                    Fail.fail("A number should be provided in order to perform a valid comparison.");
                }
                break;
            case "is lower than":
                if (envVar.matches("^-?\\d+$") && value.matches("^-?\\d+$")) {
                    assertThat(Integer.parseInt(envVar)).isLessThan(Integer.parseInt(value));
                } else {
                    Fail.fail("A number should be provided in order to perform a valid comparison.");
                }
                break;
            case "contains":
                assertThat(envVar).contains(value);
                break;
            case "is different from":
                assertThat(envVar).isNotEqualTo(value);
                break;
            default:
                Fail.fail("Not a valid comparison. Valid ones are: is | matches | is higher than | is lower than | contains | is different from");
        }
    }

    /**
     * Takes the content of a webElement and stores it in the thread environment variable passed as parameter
     * @param index position of the element in the array of webElements found
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
     * Clears the headers set by any previous request. A request will reuse the headers/cookies
     * that were set in any previous call within the same scenario
     * @throws Throwable
     */
    @Then("^I clear headers from previous request$")
    public void clearHeaders() throws Throwable {
        commonspec.getHeaders().clear();
    }

}

