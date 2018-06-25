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

package com.privalia.qa.utils;

import com.privalia.qa.assertions.Assertions;
import com.sonalake.utah.Parser;
import com.sonalake.utah.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.Reader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base class for parsing text files based on XML definitions
 * This class makes use of Utah Parser: A Java library for parsing semi-structured text files
 * https://github.com/sonalake/utah-parser/tree/master/src/test/java/com/sonalake/utah
 * <p>
 * This is a low level class for most basic functions regarding records
 *
 * @author Jose Fernandez
 */
public class FileParserUtils {

    private static final Logger logger = LoggerFactory.getLogger(SqlUtils.class);

    /**
     * Generic constructor
     */
    public FileParserUtils() {
    }

    /**
     * Parses the file passed as a Reader object using the definition in the XMLDefinitionFile as
     * a {@link Config} object
     *
     * @param XMLDefinitionFile File containing the rules for decoding
     * @param fileToParse       File to extract the values from
     * @throws IOException
     * @throws JAXBException
     */
    public List<Map<String, String>> parseFile(Config XMLDefinitionFile, Reader fileToParse) throws IOException, JAXBException {

        // load a config, using a URL or a Reader
        Config config = XMLDefinitionFile;

        // load a file and iterate through the records
        List<Map<String, String>> observedValues = new ArrayList<Map<String, String>>();
        try (Reader in = fileToParse) {
            logger.debug("Trying to parse the file...");
            Parser parser = Parser.parse(config, in);
            while (true) {
                Map<String, String> record = parser.next();
                if (null == record) {
                    break;
                } else {
                    observedValues.add(record);
                }
            }
        }

        return observedValues;

    }

    /**
     * Sum al lthe values in the given colum. This functions will try to cast all values to longs
     * with dot (".") as decimal separator. The result will be the string representation of
     * the resulting float number
     *
     * @param records    Initial set of records to perform the operation
     * @param columnName Column name to sum
     * @return String representation of the sum
     */
    public String sumColumn(List<Map<String, String>> records, String columnName) throws ParseException {

        float total = 0;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("0.#");
        format.setDecimalFormatSymbols(symbols);
        logger.debug("Adding all values from column " + columnName);

        for (Map<String, String> element : records) {

            if (element.containsKey(columnName)) {
                float f = format.parse(element.get(columnName)).floatValue();
                total = total + f;
            }
        }

        logger.debug("result: " + total);
        return String.valueOf(total);

    }

    /**
     * Returns the amount of records in which the specified column has the given value
     *
     * @param records    Initial set of records in which to perform the operation
     * @param columnName Column name to look for
     * @param value      Value the column should have to be considered
     * @return Amount of records in which column matches the given value
     */
    public int elementsWhereEqual(List<Map<String, String>> records, String columnName, String value) {

        int recordsFound = 0;

        logger.debug("Searching for records where " + columnName + " matches " + value);
        for (Map<String, String> element : records) {

            if (element.containsKey(columnName)) {
                if (value.matches(element.get(columnName).trim())) {
                    recordsFound++;
                }
            }
        }

        logger.debug("result: " + recordsFound);
        return recordsFound;

    }

    /**
     * Returns tue amount of records where the value of the specified column is different to the given value
     *
     * @param records    Initial set of records in which to perform the operation
     * @param columnName Column name to look for
     * @param value      Value the column should not have to be considered
     * @return Amount of records in which column dont matche the given value
     */
    public int elementWhereNotEqual(List<Map<String, String>> records, String columnName, String value) {

        return records.size() - this.elementsWhereEqual(records, columnName, value);
    }


    /**
     * Get the value of the given column at the given index (starting at 0)
     *
     * @param records    Initial set of records in which to perform the operation
     * @param columnName Column name
     * @param rowNumber  Index (row)
     * @return Value as String
     */
    public String getValueofColumnAtPosition(List<Map<String, String>> records, String columnName, int rowNumber) {

        return records.get(rowNumber).get(columnName).trim();

    }

    /**
     * Returns a single record at the specified position
     *
     * @param records   Initial set of records in which to perform the operation
     * @param rowNumber Record number
     * @return A single Map object {@link Map<String, String>} representing a record
     */
    public Map<String, String> getRecordAtPosition(List<Map<String, String>> records, int rowNumber) {

        try {
            return records.get(rowNumber);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Validates the value of a given key in a record
     *
     * @param record        record {@link Map<String, String>}
     * @param key           Key to look for in the record
     * @param expectedValue Expected value for the key
     * @return True if the value is the expected, false otherwise
     */
    public boolean validateRecordValues(Map<String, String> record, String key, String expectedValue) {

        if (record.containsKey(key)) {
            if (record.get(key).trim().matches(expectedValue)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the first record that matches the given condition
     *
     * @param records Initial set of records in which to perform the operation
     * @param key     Key to llok for in the record
     * @param value   Value for the given key
     * @return Fisrt record that matches the expected condition
     */
    public Map<String, String> getFirstRecordThatMatches(List<Map<String, String>> records, String key, String value) {

        for (Map<String, String> element : records) {

            if (element.containsKey(key)) {
                if (value.matches(element.get(key).trim())) {
                    return element;
                }
            }
        }

        return null;
    }

    /**
     * Returns a list of records that matches the expected condition. The resulted list of records is stored
     * internally and can be used to perform new searches on it
     *
     * @param initialSet Records list to perform operation
     * @param key        key to look for in each record
     * @param value      Expected value to match the condition
     * @param condition  Condition to apply (equal, not equal, contains, does not contains, length)
     * @return List of records where the value for the key match the expected condition
     */
    public List<Map<String, String>> filterRecordThatMatches(List<Map<String, String>> initialSet, String key, String value, String condition) {

        List<Map<String, String>> finalSet = new ArrayList<>();

        for (Map<String, String> element : initialSet) {

            if (element.containsKey(key)) {

                switch (condition) {
                    case "equal":
                        if (element.get(key).trim().matches(value)) {
                            finalSet.add(element);
                        }
                        break;

                    case "not equal":
                        if (!element.get(key).trim().matches(value)) {
                            finalSet.add(element);
                        }
                        break;

                    case "contains":
                        if (element.get(key).trim().contains(value)) {
                            finalSet.add(element);
                        }
                        break;

                    case "does not contain":
                        if (!element.get(key).trim().contains(value)) {
                            finalSet.add(element);
                        }
                        break;

                    case "length":
                        if (element.get(key).trim().length() == Integer.parseInt(value)) {
                            finalSet.add(element);
                        }
                        break;

                    default:
                        Assertions.fail("Not implemented condition: " + condition);
                }
            }

        }

        return finalSet;

    }
}
