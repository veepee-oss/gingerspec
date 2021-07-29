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

import com.privalia.qa.utils.ThreadProperty;
import io.cucumber.datatable.DataTable;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Created by carlosgarcia on 29/07/16.
 */
public class WhenGTest {

    @Test
    public void testSortJSONElementsAscending() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());

        String baseData = "indicesJSON.conf";
        String ascendingFile = "indicesJSONAscending.conf";
        String envVar = "envVar";

        String jsonString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(baseData).getFile())));
        String jsonStringAscending = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(ascendingFile).getFile())));

        CommonG commong = new CommonG();
        UtilsGSpec wheng = new UtilsGSpec(commong);

        ThreadProperty.set(envVar, jsonString);

        try {
            wheng.sortElements(envVar, "alphabetical", "ascending");
        } catch (Exception e) {
            fail("Error parsing JSON String");
        }

        String value = ThreadProperty.get(envVar);

        assertThat(value).as("Not correctly ordered").isEqualTo(jsonStringAscending);
    }

    @Test
    public void testSortJSONElementsDescending() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());

        String baseData = "indicesJSON.conf";
        String descendingFile = "indicesJSONDescending.conf";
        String envVar = "envVar";

        String jsonString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(baseData).getFile())));
        String jsonStringDescending = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(descendingFile).getFile())));

        CommonG commong = new CommonG();
        UtilsGSpec wheng = new UtilsGSpec(commong);

        ThreadProperty.set(envVar, jsonString);

        try {
            wheng.sortElements(envVar, "alphabetical", "descending");
        } catch (Exception e) {
            fail("Error parsing JSON String");
        }

        String value = ThreadProperty.get(envVar);

        assertThat(value).as("Not correctly ordered").isEqualTo(jsonStringDescending);
    }

    @Test
    public void testReadFileToVariableJSON() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());

        String baseData = "schemas/testCreateFile.json";
        String type = "json";
        String envVar = "myjson";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "UPDATE", "new_value", "n/a"), Arrays.asList("key2", "ADDTO", "[\"new_value\"]", "array"));
        DataTable modifications = DataTable.create(rawData);

        CommonG commong = new CommonG();
        UtilsGSpec wheng = new UtilsGSpec(commong);

        wheng.readFileToVariable(baseData, type, envVar, modifications);

        String envVarResult = ThreadProperty.get(envVar);
        String expectedResult = "{\"key1\":\"new_value\",\"key2\":[[\"new_value\"]],\"key3\":{\"key3_2\":\"value3_2\",\"key3_1\":\"value3_1\"}}";

        assertThat(envVarResult).as("Not as expected").isEqualTo(expectedResult);
    }

    @Test
    public void testReadFileToVariableString() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());

        String baseData = "schemas/krb5.conf";
        String type = "string";
        String envVar = "mystring";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("foo", "REPLACE", "bar", "n/a"));
        DataTable modifications = DataTable.create(rawData);

        CommonG commong = new CommonG();
        UtilsGSpec wheng = new UtilsGSpec(commong);

        wheng.readFileToVariable(baseData, type, envVar, modifications);

        String envVarResult = ThreadProperty.get(envVar);
        String expectedResult = "bar = bar";

        assertThat(envVarResult).as("Not as expected").isEqualTo(expectedResult);
    }

}
