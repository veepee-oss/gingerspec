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

import com.csvreader.CsvReader;
import com.privalia.qa.utils.SlackConnector;
import com.privalia.qa.utils.ThreadProperty;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import io.cucumber.datatable.DataTable;
import io.cucumber.docstring.DocString;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.hjson.JsonArray;
import org.hjson.JsonValue;
import org.testng.Assert;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps definitions for other useful functions/operations
 *
 * @author Jose Fernandez
 */
public class UtilsGSpec extends BaseGSpec {

    public static final int DEFAULT_TIMEOUT = 1000;

    /**
     * Default constructor.
     *
     * @param spec CommonG object
     */
    public UtilsGSpec(CommonG spec) {
        this.commonspec = spec;
    }


    /**
     * Wait seconds.
     * <p>
     * Static wait used to halt the execution of the feature for a given amount of seconds. After the time completes,
     * the remaining steps in the feature are executed. This step is commonly used to wait for a background operation to complete,
     * so next steps wont return false negatives. This step completely halts the operation of the feature for the given time and
     * could reduce performance (increase the feature execution time) if used too frequently or if the time used is unnecessarily
     * large. Try to use a reasonable time in your tests, or in the case of cucumber related scenarios, there is a much better
     * alternative to the static wait: {@link SeleniumGSpec#waitWebElementWithPooling(int, int, int, String, String, String)}
     *
     * <pre>
     * {@code
     *      When I wait '10' seconds
     * }
     * </pre>
     *
     * @param seconds                   Seconds to wait
     * @throws InterruptedException     InterruptedException
     */
    @When("^I wait '(.*)' seconds?$")
    public void idleWait(Integer seconds) throws InterruptedException {
        Thread.sleep(seconds * DEFAULT_TIMEOUT);
    }


    /**
     * Check value stored in environment variable "is|matches|is higher than|is lower than|contains|is different from" to value provided
     * <pre>
     * Examples:
     * {@code
     *      Then '${content-type}' matches 'application/json; charset=utf-8'   //checks if the value of the variable matches the string 'application/json; charset=utf-8'
     *      Then '${DEFEXSTAT}' contains 'total'                               //checks if the value of the variable contains the string 'total'
     * }
     * </pre>
     * @param envVar        The env var to verify
     * @param operation     Operation
     * @param value         The value to match against the condition
     * @throws Exception    Exception
     */
    @Then("^'(.*)' (is|matches|is higher than|is lower than|contains|is different from) '(.*)'$")
    public void checkValue(String envVar, String operation, String value) throws Exception {
        switch (operation.toLowerCase()) {
            case "is":
                Assertions.assertThat(envVar).as("%s is not equal to %s", envVar, value).isEqualTo(value);
                break;
            case "matches":
                Assertions.assertThat(envVar).as("%s does not match %s", envVar, value).matches(value);
                break;
            case "is higher than":
                if (envVar.matches("^-?\\d+$") && value.matches("^-?\\d+$")) {
                    Assertions.assertThat(Integer.parseInt(envVar)).as("%s is not higher than %s", envVar, value).isGreaterThan(Integer.parseInt(value));
                } else {
                    Assertions.fail("A number should be provided in order to perform a valid comparison.");
                }
                break;
            case "is lower than":
                if (envVar.matches("^-?\\d+$") && value.matches("^-?\\d+$")) {
                    Assertions.assertThat(Integer.parseInt(envVar)).as("%s is not lower than %s", envVar, value).isLessThan(Integer.parseInt(value));
                } else {
                    Assertions.fail("A number should be provided in order to perform a valid comparison.");
                }
                break;
            case "contains":
                Assertions.assertThat(envVar).as("%s does not contain %s", envVar, value).contains(value);
                break;
            case "is different from":
                Assertions.assertThat(envVar).as("%s is not different than %s", envVar, value).isNotEqualTo(value);
                break;
            default:
                Assertions.fail("Not a valid comparison. Valid ones are: is | matches | is higher than | is lower than | contains | is different from");
        }
    }


    /**
     * Save value for future use.
     *
     * @param value  value to be saved
     * @param envVar thread environment variable where to store the value
     */
    @Given("^I save '(.*)' in variable '(.*)'$")
    public void saveInEnvironment(String value, String envVar) {
        ThreadProperty.set(envVar, value);
    }


    /**
     * Read csv file and store result in list of maps
     *
     * @param csvFile       the csv file
     * @throws Exception    the exception
     */
    @When("^I read info from csv file '(.*)'$")
    public void readFromCSV(String csvFile) throws Exception {
        CsvReader rows = new CsvReader(csvFile);

        String[] columns = null;
        if (rows.readRecord()) {
            columns = rows.getValues();
            rows.setHeaders(columns);
        }

        List<Map<String, String>> results = new ArrayList<Map<String, String>>();
        while (rows.readRecord()) {
            Map<String, String> row = new HashMap<String, String>();
            for (String column : columns) {
                row.put(column, rows.get(rows.getIndex(column)));
            }
            results.add(row);
        }

        rows.close();

        commonspec.setResultsType("csv");
        commonspec.setCSVResults(results);
    }



    /**
     * Sort elements in envVar by a criteria and order.
     *
     * @param envVar   Environment variable to be sorted
     * @param criteria alphabetical,...
     * @param order    ascending or descending
     */
    @When("^I sort elements in '(.+?)' by '(.+?)' criteria in '(.+?)' order$")
    public void sortElements(String envVar, String criteria, String order) {

        String value = ThreadProperty.get(envVar);
        JsonArray jsonArr = JsonValue.readHjson(value).asArray();

        List<JsonValue> jsonValues = new ArrayList<JsonValue>();
        for (int i = 0; i < jsonArr.size(); i++) {
            jsonValues.add(jsonArr.get(i));
        }

        Comparator<JsonValue> comparator;
        switch (criteria) {
            case "alphabetical":
                commonspec.getLogger().debug("Alphabetical criteria selected.");
                comparator = new Comparator<JsonValue>() {
                    public int compare(JsonValue json1, JsonValue json2) {
                        int res = String.CASE_INSENSITIVE_ORDER.compare(json1.toString(), json2.toString());
                        if (res == 0) {
                            res = json1.toString().compareTo(json2.toString());
                        }
                        return res;
                    }
                };
                break;
            default:
                commonspec.getLogger().debug("No criteria selected.");
                comparator = null;
        }

        if ("ascending".equals(order)) {
            Collections.sort(jsonValues, comparator);
        } else {
            Collections.sort(jsonValues, comparator.reversed());
        }

        ThreadProperty.set(envVar, jsonValues.toString());
    }


    /**
     * Create a file from seed.
     * <p>
     * Creates a JSON file in the /target/test-classes directory of the project with the specified name.
     * This file can later be referenced using $(pwd)/target/test-classes/fileName. This steps receives a
     * datatable with a list of all modifications to be performed in the seed file (ADD, REPLACE, APPEND,
     * ADDTO, etc)
     * <p>
     * You can specify a seed file that uses the contents of another file to create a single merged file with ADDTO. For example
     * <pre>
     * {@code
     *      Given I create file 'testCreateFilePlain.json' based on 'schemas/testCreateFile.json' as 'json' with:
     *          | $.key2 | ADDTO | ${file:UTF-8:src/test/resources/schemas/testCreateFileReplacePlainText.json} | string |
     * }
     * </pre>
     * Will create a file with name testCreateFilePlain under /target/test-classes, using'schemas/testCreateFile.json' as template,
     * changing the value $.key2 with the contents of schemas/testCreateFileReplacePlainText.json as string
     *
     *
     * @param fileName      name of the JSON file to be created
     * @param baseData      path to file containing the schema to be used
     * @param type          element to read from file (element should contain a json)
     * @param modifications DataTable containing the modifications to be done to the base schema element
     *                      <p>
     *                      - Syntax will be:
     *                      {@code
     *                      | <key path> | <type of modification> | <new value> |
     *                      }
     *                      for DELETE/ADD/UPDATE/APPEND/PREPEND
     *                      where:
     *                      key path: path to the key to be modified
     *                      type of modification: DELETE/ADD/UPDATE/APPEND/PREPEND
     *                      new value: new value to be used
     *                      <p>
     *                      - Or:
     *                      {@code
     *                      | <key path> | <type of modification> | <new value> | <new value type> |
     *                      }
     *                      for REPLACE
     *                      where:
     *                      key path: path to the key to be modified
     *                      type of modification: REPLACE
     *                      new value: new value to be used
     *                      json value type: type of the json property (array|object|number|boolean|null|n/a (for string))
     *                      <br>
     *                      <br>
     *                      For example:
     *                      <br>
     *                      (1)
     *                      If the element read is {"key1": "value1", "key2": {"key3": "value3"}}
     *                      and we want to modify the value in "key3" with "new value3"
     *                      the modification will be:
     *                      | key2.key3 | UPDATE | "new value3" |
     *                      being the result of the modification: {"key1": "value1", "key2": {"key3": "new value3"}}
     *                      <br>
     *                      (2)
     *                      If the element read is {"key1": "value1", "key2": {"key3": "value3"}}
     *                      and we want to replace the value in "key2" with {"key4": "value4"}
     *                      the modification will be:
     *                      | key2 | REPLACE | {"key4": "value4"} | object |
     *                      being the result of the modification: {"key1": "value1", "key2": {"key4": "value4"}}
     * @throws Exception    Exception
     */
    @When("^I create file '(.+?)' based on '(.+?)' as '(.+?)' with:$")
    public void createFile(String fileName, String baseData, String type, DataTable modifications) throws Exception {
        // Retrieve data
        String retrievedData = commonspec.retrieveData(baseData, type);

        // Modify data
        commonspec.getLogger().debug("Modifying data {} as {}", retrievedData, type);
        String modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();

        // Create file (temporary) and set path to be accessible within test
        File tempDirectory = new File(System.getProperty("user.dir") + "/target/test-classes/");
        String absolutePathFile = tempDirectory.getAbsolutePath() + "/" + fileName;
        commonspec.getLogger().debug("Creating file {} in 'target/test-classes'", absolutePathFile);
        // Note that this Writer will delete the file if it exists
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(absolutePathFile), StandardCharsets.UTF_8));
        try {
            out.write(modifiedData);
        } catch (Exception e) {
            Assertions.fail("Custom file could not be created: " +  e.getMessage());
        } finally {
            out.close();
        }

        assertThat(new File(absolutePathFile).isFile());
    }

    /**
     * Saves file in variable with modifications.
     * <p>
     * Read the file passed as parameter, perform the modifications specified and save the result in the environment
     * variable passed as parameter.
     * <p>
     * Using a json file and updating its contents
     * <pre>
     * {@code
     *      Given I read file 'schemas/testCreateFile.json' as 'json' and save it in environment variable 'myjson' with:
     *          | $.key1 | UPDATE | new_value     | n/a   |
     *          | $.key2 | ADDTO  | ["new_value"] | array |
     * }
     * </pre>
     * Reading a plain text file and editin its contents
     * <pre>
     * {@code
     *       Given I read file 'schemas/krb5.conf' as 'string' and save it in environment variable 'mystring' with:
     *          | foo | REPLACE | bar | n/a |
     * }
     * </pre>
     *
     * @param baseData      file to read
     * @param type          whether the info in the file is a 'json' or a simple 'string'
     * @param envVar        name of the variable where to store the result
     * @param modifications modifications to perform in the content of the file
     * @throws Exception    Exception
     */
    @When("^I read file '(.+?)' as '(.+?)' and save it in environment variable '(.+?)' with:$")
    public void readFileToVariable(String baseData, String type, String envVar, DataTable modifications) throws Exception {
        // Retrieve data
        String retrievedData = commonspec.retrieveData(baseData, type);

        // Modify data
        commonspec.getLogger().debug("Modifying data {} as {}", retrievedData, type);
        String modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();

        // Save in environment variable
        ThreadProperty.set(envVar, modifiedData);
    }

    /**
     * Saves file in variable.
     * <p>
     * Read the file passed as parameter and save the result in the environment variable passed as parameter. Unlike the previous
     * example, if no modifications are necessary in the file, there is no need to specify a datatable with modifications.
     * <p>
     * Example
     * <pre>
     * {@code
     *      Given I read file 'schemas/testCreateFile.json' as 'json' and save it in environment variable 'myjson'
     * }
     * </pre>
     *
     * @param baseData      file to read
     * @param type          whether the info in the file is a 'json' or a simple 'string'
     * @param envVar        name of the variable where to store the result
     * @throws Exception    Exception
     */
    @When("^I read file '(.+?)' as '(.+?)' and save it in environment variable '(.+?)'$")
    public void readFileToVariableNoDataTable(String baseData, String type, String envVar) throws Exception {
        // Retrieve data
        String retrievedData = commonspec.retrieveData(baseData, type);

        // Save in environment variable
        ThreadProperty.set(envVar, retrievedData);
    }


    /**
     * Checks if an exception has been thrown.
     * <p>
     * Checks if an exception is/is not thrown during the execution of the previous step. It can also check the exception
     * class and the message of the exception
     * <p>
     * Example: Checking if any exception is thrown
     * <pre>
     * {@code
     *      When I execute a jdbc select 'SELECT count(*) FROM crossdataTables''
     *      Then an exception 'IS NOT' thrown
     * }
     * </pre>
     * Example: Checking if not exception is thrown
     * <pre>
     * {@code
     *      Given I execute 'CREATE TABLE newCatalog.newTable'
     *      Then an exception 'IS' thrown
     * }
     * </pre>
     * Example: Checking if a particular exception is thrown
     * <pre>
     * {@code
     *      When I delete the stream 'testStreamACK0'
     *      Then an exception 'IS' thrown with class 'PrivaliaEngineConnectionException' and message like 'Acknowledge timeout expired'
     * }
     * </pre>
     *
     * @param exception                 : "IS NOT" | "IS"
     * @param foo                       parameter generated by cucumber because of the optional expression
     * @param clazz                     the clazz
     * @param bar                       parameter generated by cucumber because of the optional expression
     * @param exceptionMsg              the exception msg
     * @throws ClassNotFoundException   the class not found exception
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
     * Checks the different results of a previous query
     *
     * @param expectedResults A DataTable Object with all data needed for check the results. The DataTable must contains at least 2 columns:
     *                        a) A field column from the result
     *                        b) Occurrences column (Integer type)
     *                        <p>
     *                        Example:
     *                        |latitude| longitude|place     |occurrences|
     *                        |12.5    |12.7      |Valencia  |1           |
     *                        |2.5     | 2.6      |Madrid   |0           |
     *                        |12.5    |13.7      |Sevilla   |1           |
     *                        IMPORTANT: There no should be no existing columns
     * @throws Exception      Exception
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
     * Creates a random integer within range
     * <p>
     * Creates a random number within the range provided and saves it in a variable
     * for later use
     * <br>
     * <pre>
     * Example:
     * {@code
     *      Given I create a random number between '0' and '10' and save it in the variable 'RANDOM'
     *      And I wait '${RANDOM}' seconds
     * }
     * </pre>
     *
     * @see #generateRandomStrings(String, Integer, String)
     * @param lowerLimit    lower limit in range
     * @param upperLimit    upper limit in range
     * @param varName       name of the variable to save the result
     */
    @Given("^I generate a random number between '(.*)' and '(.*)' and save it in the variable '(.*)'$")
    public void generateRandomNumberInRange(Integer lowerLimit, Integer upperLimit, String varName) {
        Assertions.assertThat(lowerLimit).as("").isLessThan(upperLimit);
        Random random = new Random();
        Integer rndint = random.nextInt(upperLimit - lowerLimit) + lowerLimit;
        ThreadProperty.set(varName, rndint.toString());
    }

    /**
     * Generate numeric or alphanumeric strings
     * <p>
     * Generates a string that can be numeric ([0-9]) or alphanumeric ([0-9][A-Z][a-z]) of a given length
     * and saves it in a variable for furute use
     * <br>
     * <pre>
     * Example: Generating a random numeric string of length 20
     * {@code
     *      Given I generate a random 'numeric' string of length '20' and save it in the variable 'NUMERIC'
     * }
     * Example: Generating a random alphanumeric string of length 20
     * {@code
     *      Given I generate a random 'alphanumeric' string of length '20' and save it in the variable 'ALPHANUMERIC'
     * }
     * </pre>
     *
     * @see #generateRandomNumberInRange(Integer, Integer, String)
     * @param type      string type: numeric|alphanumeric
     * @param length    string final length
     * @param varName   name of the variable to save the result
     */
    @Given("^I generate a random '(numeric|alphanumeric)' string of length '(.*)' and save it in the variable '(.*)'$")
    public void generateRandomStrings(String type, Integer length, String varName) {

        Assertions.assertThat(length).as("Length must be greater than zero!").isGreaterThan(0);
        SecureRandom random = new SecureRandom();
        String out = "";

        if (type.matches("numeric")) {
            for (int i = 0; i <= length - 1; i++) {
                Integer rndint = random.nextInt(9 - 0) + 0;
                out = out.concat(rndint.toString());
            }
        } else {
            String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(AB.charAt(random.nextInt(AB.length())));
            }
            out = sb.toString();
        }
        ThreadProperty.set(varName, out);
    }

    /**
     * Creates a conditional block of execution
     * <p>
     * This allows the conditional execution of steps during runtime. All steps enclosed
     * between this step and {@link #ifStamenetEndBlock()} will be executed only if the given
     * statement returns true, otherwise, the steps will be skipped.
     * The statement can be any javascript expression that can return a true|false output. You can even
     * use variables created during the scenario execution.
     * <p>
     * <b>Warning: use this functionality sparingly, or only in very concrete automation cases. We discourage
     * the creation of tests that could return different results on different runs. Also, this functionality
     * has not been tested on multi-threaded mode, so it may break when running tests in parallel</b>
     * <br>
     * <pre>
     * {@code
     * Examples
     *
     * Scenario: Using if block to control execution
     *     * I save 'GingerSpec' in variable 'NAME'
     *     * if ('${NAME}'.contains('Ginger')) {
     *     * I run 'echo "This should be executed"' locally
     *     * }
     *     * if ('${NAME}'.contains('foo')) {
     *     * I run 'echo "This should NOT be executed"' locally
     *     * I run 'exit 1' locally
     *     * }
     * }
     * </pre>
     * @see #ifStamenetEndBlock()
     * @param statement Any javascript expression that could be resolved to true or false
     */
    @Given("^if \\((.*)\\) \\{$")
    public void ifStamenetBeginBlock(String statement) { }

    /**
     * If statement end block
     * <p>
     * Indicates the end of the if block
     *
     * @see #ifStamenetBeginBlock(String)
     */
    @Given("^\\}$")
    public void ifStamenetEndBlock() { }


    /**
     * Send a message to the given slack channel.
     * <p>
     * Sends a message to the given channel (or group of channels separated by comma). The message is a {@link DocString} object
     * and it can also contain other variables created during the execution of the scenario. you will need to provide the slack token
     * either via src/test/resources/slack.properties file or via maven variables as Dslack.token=abcdefg123456. Check the attached link
     * for more information.
     * <br>
     * <pre>
     * {@code
     * Examples
     *
     *     Scenario: Sending message to a slack channel
     *          Given I save 'GingerSpec' in variable 'FRAMEWORK'
     *          Given I send a message to the slack channel '#qms-notifications' with text
     *          """
     *          :wave: Hello! You can send any type of text to a given slack channel.
     *          You can even send variables created during your steps
     *
     *          Regards, ${FRAMEWORK} :slightly_smiling_face:
     *          """
     * }
     * </pre>
     * @see <a href="https://github.com/veepee-oss/gingerspec/wiki/Gherkin-tags#slack-tag">@slack tag</a>
     * @param slackChannel          Name of the channel to send the notification to. It can also be a list of channels separated by comma (i.e #channel1,#channel2,#channel3)
     * @param message               Message to send. It can contain variables created during the scenario
     * @throws SlackApiException    SlackApiException
     * @throws IOException          IOException
     */
    @Given("I send a message to the slack channel(s) {string} with text")
    public void sendMessageToSlackChannel(String slackChannel, DocString message) throws SlackApiException, IOException {
        SlackConnector sc = new SlackConnector();
        String[] channels = slackChannel.split(",");
        List<ChatPostMessageResponse> responses = sc.sendMessageToChannels(Arrays.asList(channels), message.getContent());

        for (ChatPostMessageResponse response: responses) {
            Assert.assertTrue(response.isOk(), "Could not send the notification to channel " + response.getChannel() + " in slack: " + response.getError());
        }
    }

    /**
     * This step is for testing purposes of GingerSpec. Should NOT be used
     * @param body  Example DocString argument
     */
    @Given("This is a DocString")
    public void thisIsADocString(DocString body) {
        System.out.println(body.getContent());
    }

    /**
     * This step is for testing purposes of GingerSpec. Should NOT be used
     * @param table Example DataTable argument
     */
    @Given("this is a datatable:")
    public void thisIsADatatable(DataTable table) {
        System.out.println(table.toString());
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
     * Scenario: Temporally stop the execution of the feature
     *     Given I go to 'http://demoqa.com/automation-practice-form'
     *     Then I pause
     *     Then I type 'Jose' on the element with 'id:firstName'
     * }</pre>
     *
     * @see UtilsGSpec#idleWait(Integer)
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
