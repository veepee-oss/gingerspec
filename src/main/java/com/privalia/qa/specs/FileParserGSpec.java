/*
 * Copyright (C) 2018 Privalia (http://privalia.com)
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

import com.sonalake.utah.config.Config;
import com.sonalake.utah.config.ConfigLoader;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.datatable.DataTable;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps definitions for handling and parsing text files.
 * This class makes use of Utah-parser, a Java library for parsing semi-structured text files
 * @see <a href="https://github.com/sonalake/utah-parser">https://github.com/sonalake/utah-parser</a>
 * @author Jose Fernandez
 */
public class FileParserGSpec extends BaseGSpec {

    public FileParserGSpec(CommonG spec) {
        this.commonspec = spec;
    }

    /**
     * Parse the given file according to the rules described in the XML configuration file
     * The operation returns a list of 'records' (List(Map(String, String))) that is stored internally
     * for further operations
     *
     * @param fileToParse           Location of the file to parse
     * @param XMLDefinitionFile     Location of the XML configuration file
     * @throws IOException          IOException
     * @throws JAXBException        JAXBException
     * @throws URISyntaxException   URISyntaxException
     */
    @Given("^I parse the file located at '(.+?)' using the template defined in '(.+?)'$")
    public void parseTemplateFile(String fileToParse, String XMLDefinitionFile) throws IOException, JAXBException, URISyntaxException {

        InputStream stream = getClass().getClassLoader().getResourceAsStream(fileToParse);
        Config config = new ConfigLoader().loadConfig(getClass().getClassLoader().getResource(XMLDefinitionFile).toURI().toURL());

        Reader in = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        List<Map<String, String>> records = this.commonspec.getFileParserUtil().parseFile(config, in);
        this.commonspec.setLastFileParseResult(records);

    }

    /**
     * Verifies that the stored result from the last operation contains the specified amount of records, or if the amount of records is greater that or equal
     * than the expected
     * @param atLeast   Verifies a greater than or equal condition
     * @param quantity  Expected quantity
     */
    @Then("^the result contains( at least)? '(\\d+)' records$")
    public void verifyLastResult(String atLeast, int quantity) {

        if (atLeast != null) {
            assertThat(this.commonspec.getLastFileParseResult().size()).as("The result is not of the size expècted").isGreaterThanOrEqualTo(quantity);
        } else {
            assertThat(this.commonspec.getLastFileParseResult().size()).as("The result is not of the size expected").isEqualTo(quantity);
        }

    }

    /**
     * Calculates the sum of the given column. The column must contain numeric values
     * @param columnName        Column name
     * @param expectedTotal     Expected total
     * @throws ParseException   Exception if the column does not contain numeric values
     */
    @And("^the total of the column '(.+?)' is '(.+?)'$")
    public void sumColumn(String columnName, float expectedTotal) throws ParseException {

        String result = this.getCommonSpec().getFileParserUtil().sumColumn(this.commonspec.getLastFileParseResult(), columnName);
        assertThat(Float.parseFloat(result)).as("The total of the column " + expectedTotal + " is not the value expected").isEqualTo(expectedTotal);
    }

    /**
     * Calculates the amount of records in the last operation where the column matches the expected value
     * @param quantity      Expected amount of columns to find that match the condition
     * @param columnName    Column name to consider
     * @param expectedValue Expected value for the command
     */
    @And("^there are '(\\d+)' records with column '(.+?)' equal to '(.+?)'$")
    public void assertNumberOfRecordsWithColumn(int quantity, String columnName, String expectedValue) {

        assertThat(this.commonspec.getFileParserUtil().elementsWhereEqual(this.commonspec.getLastFileParseResult(), columnName, expectedValue)).as("The amount of " +
                "records with column " + columnName + "equal to " + expectedValue + " did not match the expected value").isEqualTo(quantity);

    }

    /**
     * Verifies that for the record located at the given position, the column contains the expected value
     * @param position          position
     * @param columnName        columnName
     * @param valueExpected     valueExpected
     */
    @And("^the record at position '(\\d+)' at column '(.+?)' has the value '(.+?)'$")
    public void assertRecordValue(int position, String columnName, String valueExpected) {

        assertThat(this.getCommonSpec().getFileParserUtil().getValueofColumnAtPosition(this.commonspec.getLastFileParseResult(), columnName, position)).as("The record " +
                "at position " + position + " did not have the expected value: " + valueExpected + " for column " + columnName).isEqualTo(valueExpected);
    }

    /**
     * Returns the first records in the set in which the column has the given value
     * @param columnName    Column name to consider
     * @param value         Expected value
     */
    @And("^I get the first record with column '(.+?)' equal to '(.+?)'$")
    public void getFirstRecordWithColumn(String columnName, String value) {

        Map<String, String> record = this.commonspec.getFileParserUtil().getFirstRecordThatMatches(this.commonspec.getLastFileParseResult(), columnName, value);
        assertThat(record).as("No record found with columhn: " + columnName + " and value: " + value + " found in the record set").isNotNull();
        this.commonspec.setLastFileParseRecord(record);
    }

    /**
     * Returns the record at the specified position in the set
     * @param rowNUmber     Index (starting by 0)
     * @throws Throwable    Throwable
     */
    @Then("^I get the record at position '(\\d+)'$")
    public void getRecord(int rowNUmber) throws Throwable {

        Map<String, String> record = this.commonspec.getFileParserUtil().getRecordAtPosition(this.commonspec.getLastFileParseResult(), rowNUmber);
        assertThat(record).as("No record found at position: " + rowNUmber).isNotNull();
        this.commonspec.setLastFileParseRecord(record);
    }

    /**
     * Verifies if the selected record matches the given cases by the datatable
     * @param dataTable DataTable containing the set of columns to be
     *                      verified against the condition. Syntax will be:
     *                      {@code
     *                      | <column name> | <condition> | <expected value>
     *                      }
     *                      where:
     *                      header name: header name
     *                      condition: Condition that is going to be evaluated (available: equal,
     *                      not equal, contains, does not contain, length)
     *                      expected value: Value used to verify the condition
     *                      for example:
     *                      If we want to verify that the column "uptime" is equal
     *                      to "10:37:12" we would do
     *                      | uptime | equal | 10:37:12 |
     */
    @And("^the selected record matches the following cases:$")
    public void assertRecordMatchesProperties(DataTable dataTable) {

        assertThat(this.commonspec.getLastFileParseRecord()).as("No record was selected in a previous step").isNotNull();

        for (List<String> row : dataTable.asLists()) {
            String columnName = row.get(0);
            String condition = row.get(1);
            String expectedValue = row.get(2);

            //I use the the function to filter records to also perfom a bsic matching of expected conditions.
            //The record is inserted on a list and the list is passed throught the filter. If the output still contains
            //the record, means that the record match the condition
            List<Map<String, String>> recordList = new ArrayList<>();
            recordList.add(this.commonspec.getLastFileParseRecord());
            int size = this.commonspec.getFileParserUtil().filterRecordThatMatches(recordList, columnName, expectedValue, condition).size();
            assertThat(size).as("The value of the column " + columnName + " did not match the expected condition (" +  condition + " " + expectedValue).isNotZero();

        }

    }

    /**
     * From the previous operation, filter all the records from the set that match the conditions
     * given in the datatable. The resulting set will be stored internally and will be avilable to
     * perform further filter operations
     * @param dataTable DataTable containing the set of columns to be
     *                      verified against the condition. Syntax will be:
     *                      {@code
     *                      | <column name> | <condition> | <expected value>
     *                      }
     *                      where:
     *                      header name: header name
     *                      condition: Condition that is going to be evaluated (available: equal,
     *                      not equal, contains, does not contain, length)
     *                      expected value: Value used to verify the condition
     *                      for example:
     *                      If we want to filter all the records in the set where
     *                      'status' does not contains '0'
     *                      | status    | does not contain  | 0 |
     */
    @And("^I select all records that match the following cases:$")
    public void selectRecordsWithProperties(DataTable dataTable) {

        List<Map<String, String>> result = this.commonspec.getLastFileParseResult();

        for (List<String> row : dataTable.asLists()) {

            String columnName = row.get(0);
            String condition = row.get(1);
            String expectedValue = row.get(2);

            result = this.commonspec.getFileParserUtil().filterRecordThatMatches(result, columnName, expectedValue, condition);

        }

        this.commonspec.setLastFileParseResult(result);
    }
}

