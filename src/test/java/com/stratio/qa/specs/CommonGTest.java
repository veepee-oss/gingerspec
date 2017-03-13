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
package com.stratio.qa.specs;

import com.stratio.qa.utils.ThreadProperty;
import cucumber.api.DataTable;
import org.hjson.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CommonGTest {

    private JSONObject jsonObject1_1, jsonObject1;
    private JSONObject jsonObject2, jsonObject3;
    private JSONObject jsonObject4_1, jsonObject4;
    private JSONObject jsonObject5, jsonObject6, jsonObject6_1;
    private JSONObject jsonObject7, jsonObject7_1;
    private JSONObject jsonObject8, jsonObject8_1;
    private JSONObject jsonObject9, jsonObject9_1;
    private JSONObject jsonObject10, jsonObject10_1;
    private JSONObject jsonObject11, jsonObject11_1;
    private JSONObject jsonObject12, jsonObject12_1;
    private JSONObject jsonObject13, jsonObject13_1, jsonObject13_2;
    private JSONObject jsonObject14, jsonObject14_1;
    private JSONObject jsonObject15, jsonObject15_1;
    private JSONObject jsonObject16, jsonObject16_1;
    private JSONObject jsonObject17, jsonObject17_2;
    private JSONObject jsonObject18, jsonObject18_3;
    private JSONObject jsonObject19, jsonObject19_2, jsonObject19_3;
    private JSONArray jsonObject3_1, jsonObject17_1, jsonObject18_1;


    @BeforeClass
    public void prepareJson() {
        jsonObject1_1 = new JSONObject();
        jsonObject1_1.put("key3", "value3");
        jsonObject1 = new JSONObject();
        jsonObject1.put("key1", "value1").put("key2", jsonObject1_1);

        jsonObject2 = new JSONObject();
        jsonObject2.put("type", "record");

        String[] array1 = {"a", "b", "c"};
        jsonObject3_1 = new JSONArray(Arrays.asList(array1));
        jsonObject3 = new JSONObject();
        jsonObject3.put("type", jsonObject3_1);

        jsonObject4_1 = new JSONObject();
        jsonObject4_1.put("key3", JSONObject.NULL);
        jsonObject4 = new JSONObject();
        jsonObject4.put("key1", "value1").put("key2", jsonObject4_1);

        jsonObject5 = new JSONObject();
        jsonObject5.put("key2", jsonObject1_1);

        jsonObject6_1 = new JSONObject();
        jsonObject6_1 = new JSONObject();
        jsonObject6_1.put("key4", "value4").put("key3", "value3");
        jsonObject6 = new JSONObject();
        jsonObject6.put("key1", "value1").put("key2", jsonObject6_1);

        jsonObject7_1 = new JSONObject();
        jsonObject7_1.put("key3", "NEWvalue3");
        jsonObject7 = new JSONObject();
        jsonObject7.put("key2", jsonObject7_1).put("key1", "value1");

        jsonObject8_1 = new JSONObject();
        jsonObject8_1.put("key3", "value3Append");
        jsonObject8 = new JSONObject();
        jsonObject8.put("key2", jsonObject8_1).put("key1", "value1");

        jsonObject9_1 = new JSONObject();
        jsonObject9_1.put("key3", "Prependvalue3");
        jsonObject9 = new JSONObject();
        jsonObject9.put("key2", jsonObject9_1).put("key1", "value1");

        jsonObject10_1 = new JSONObject();
        jsonObject10_1.put("key3", "vaREPLACEe3");
        jsonObject10 = new JSONObject();
        jsonObject10.put("key2", jsonObject10_1).put("key1", "value1");

        jsonObject11_1 = new JSONObject();
        jsonObject11_1.put("key3", true);
        jsonObject11 = new JSONObject();
        jsonObject11.put("key2", jsonObject11_1).put("key1", "value1");

        jsonObject12_1 = new JSONObject();
        jsonObject12_1.put("key3", false);
        jsonObject12 = new JSONObject();
        jsonObject12.put("key2", jsonObject12_1).put("key1", "value1");

        jsonObject13_1 = new JSONObject();
        jsonObject13_2 = new JSONObject();
        jsonObject13_1.put("key3", jsonObject13_2);
        jsonObject13 = new JSONObject();
        jsonObject13.put("key2", jsonObject13_1).put("key1", "value1");

        jsonObject14_1 = new JSONObject();
        jsonObject14_1.put("key3", 5);
        jsonObject14 = new JSONObject();
        jsonObject14.put("key2", jsonObject14_1).put("key1", "value1");

        jsonObject15_1 = new JSONObject();
        jsonObject15_1.put("key3", 5.0);
        jsonObject15 = new JSONObject();
        jsonObject15.put("key2", jsonObject15_1).put("key1", "value1");

        jsonObject16_1 = new JSONObject();
        jsonObject16_1.put("key3", 0);
        jsonObject16 = new JSONObject();
        jsonObject16.put("key2", jsonObject16_1).put("key1", "value1");

        String[] array2 = {"a", "b", "c"};
        jsonObject17_1 = new JSONArray(Arrays.asList(array2));
        jsonObject17_2 = new JSONObject();
        jsonObject17_2.put("key3", jsonObject17_1);
        jsonObject17 = new JSONObject();
        jsonObject17.put("key2", jsonObject17_2).put("key1", "value1");

        String[] array3_1 = {};
        Object[] array3 = {array3_1};
        jsonObject18_1 = new JSONArray(Arrays.asList(array3));
        jsonObject18_3 = new JSONObject();
        jsonObject18_3.put("key3", jsonObject18_1);
        jsonObject18 = new JSONObject();
        jsonObject18.put("key2", jsonObject18_3).put("key1", "value1");

        jsonObject19_3 = new JSONObject();
        jsonObject19_3.put("a", "1").put("b", "1").put("c", "1");

        jsonObject19_2 = new JSONObject();
        jsonObject19_2.put("key3", jsonObject19_3);
        jsonObject19 = new JSONObject();
        jsonObject19.put("key2", jsonObject19_2).put("key1", "value1");

    }

    @Test
    public void retrieveDataExceptionTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String baseData = "invalid.conf";
        String type = "string";

        assertThat(commong.retrieveData(baseData, type)).as("File not found exception").isEqualTo("ERROR: File does not exist: invalid.conf");
    }

    @Test
    public void retrieveDataStringTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String baseData = "retrieveDataStringTest.conf";
        String type = "string";

        String returnedData = commong.retrieveData(baseData, type);
        assertThat(returnedData).as("Invalid information read").isEqualTo("username=username&password=password");
    }

    @Test
    public void retrieveDataInvalidJsonTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String baseData = "retrieveDataInvalidJsonTest.conf";
        String type = "json";

        try {
            commong.retrieveData(baseData, type);
            org.testng.Assert.fail("Expected ParseException");
        } catch (Exception e) {
            assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(ParseException.class.toString());
        }
    }

    @Test
    public void retrieveDataValidJsonTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String baseData = "retrieveDataValidJsonTest.conf";
        String type = "json";

        String returnedData = commong.retrieveData(baseData, type);
        assertThat(returnedData).as("Invalid information read").isEqualTo(jsonObject1.toString());
    }

    @Test
    public void modifyDataNullValueJsonTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject4.toString();
        ;
        String expectedData = "{\"key2\":{\"key3\":null}}";
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "DELETE", "N/A"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataInvalidModificationTypeStringTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = "username=username&password=password";
        String type = "string";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("username=username", "REMOVE", "N/A"));
        DataTable modifications = DataTable.create(rawData);

        try {
            commong.modifyData(data, type, modifications);
            fail("Expected Exception");
        } catch (Exception e) {
            assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
            assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("Modification type does not exist: REMOVE");
        }
    }

    @Test
    public void modifyDataInvalidModificationTypeJsonTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject1.toString();
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("username=username", "REMOVE", "N/A"));
        DataTable modifications = DataTable.create(rawData);

        try {
            commong.modifyData(data, type, modifications);
            fail("Expected Exception");
        } catch (Exception e) {
            assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
            assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("Modification type does not exist: REMOVE");
        }
    }

    @Test
    public void modifyDataDeleteStringTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = "username=username&password=password";
        String expectedData = "password=password";
        String type = "string";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("username=username&", "DELETE", "N/A"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        assertThat(modifiedData).as("Unexpected modified data").isEqualTo(expectedData);
    }

    @Test
    public void modifyDataAddStringTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = "username=username&password=password";
        String expectedData = "username=username&password=password&config=config";
        String type = "string";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("N/A", "ADD", "&config=config"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        assertThat(modifiedData).as("Unexpected modified data").isEqualTo(expectedData);
    }

    @Test
    public void modifyDataUpdateStringTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = "username=username&password=password";
        String expectedData = "username=NEWusername&password=password";
        String type = "string";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("username=username", "UPDATE", "username=NEWusername"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        assertThat(modifiedData).as("Unexpected modified data").isEqualTo(expectedData);
    }

    @Test
    public void modifyDataPrependStringTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = "username=username&password=password";
        String expectedData = "key1=value1&username=username&password=password";
        String type = "string";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("username=username", "PREPEND", "key1=value1&"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        assertThat(modifiedData).as("Unexpected modified data").isEqualTo(expectedData);
    }

    @Test
    public void modifyDataDeleteJsonTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject1.toString();
        String expectedData = "{\"key2\":{\"key3\":\"value3\"}}";
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "DELETE", "N/A"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataAddJsonTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject1.toString();
        String expectedData = jsonObject6.toString();
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("$.key2.key4", "ADD", "value4"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataUpdateJsonTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject1.toString();
        String expectedData = jsonObject7.toString();
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key2.key3", "UPDATE", "NEWvalue3"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataAppendJsonTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject1.toString();
        String expectedData = jsonObject8.toString();
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key2.key3", "APPEND", "Append"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataPrependJsonTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject1.toString();
        String expectedData = jsonObject9.toString();
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key2.key3", "PREPEND", "Prepend"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataReplaceJsonTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject1.toString();
        String expectedData = jsonObject10.toString();
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key2.key3", "REPLACE", "lu->REPLACE"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataReplaceJsonArrayTest_1() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject2.toString();
        String expectedData = "{\"type\":[]}";
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("type", "REPLACE", "[]", "array"));
        DataTable modifications = DataTable.create(rawData);
        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataReplaceJsonArrayTest_2() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject2.toString();
        String expectedData = jsonObject3.toString();
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("type", "REPLACE", "[\"a\", \"b\", \"c\"]", "array"));
        DataTable modifications = DataTable.create(rawData);
        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataReplaceJsonArrayTest_3() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject1.toString();
        String expectedData = jsonObject17.toString();
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key2.key3", "REPLACE", "[\"a\", \"b\", \"c\"]", "array"));
        DataTable modifications = DataTable.create(rawData);
        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataReplaceJsonArrayTest_4() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject1.toString();
        String expectedData = jsonObject18.toString();
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key2.key3", "REPLACE", "[[]]", "array"));
        DataTable modifications = DataTable.create(rawData);
        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataReplaceJsonObjectTest_1() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject1.toString();
        String expectedData = jsonObject19.toString();
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key2.key3", "REPLACE", "{\"a\":\"1\", \"b\":\"1\", \"c\":\"1\"}", "object"));
        DataTable modifications = DataTable.create(rawData);
        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataReplaceJsonObjectTest_2() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject1.toString();
        String expectedData = jsonObject13.toString();
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key2.key3", "REPLACE", "{}", "object"));
        DataTable modifications = DataTable.create(rawData);
        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataReplaceJsonNumberTest_1() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject1.toString();
        String expectedData = jsonObject14.toString();
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key2.key3", "REPLACE", "5", "number"));
        DataTable modifications = DataTable.create(rawData);
        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataReplaceJsonNumberTest_2() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject1.toString();
        String expectedData = jsonObject15.toString();
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key2.key3", "REPLACE", "5.0", "number"));
        DataTable modifications = DataTable.create(rawData);
        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataReplaceJsonNumberTest_3() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject1.toString();
        String expectedData = jsonObject16.toString();
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key2.key3", "REPLACE", "0", "number"));
        DataTable modifications = DataTable.create(rawData);
        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataReplaceJsonBooleanTest_1() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject1.toString();
        String expectedData = jsonObject11.toString();
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key2.key3", "REPLACE", "true", "boolean"));
        DataTable modifications = DataTable.create(rawData);
        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataReplaceJsonBooleanTest_2() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject1.toString();
        String expectedData = jsonObject12.toString();
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key2.key3", "REPLACE", "false", "boolean"));
        DataTable modifications = DataTable.create(rawData);
        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataReplaceJsonNull() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String data = jsonObject1.toString();
        String expectedData = jsonObject4.toString();
        ;
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key2.key3", "REPLACE", "null", "null"));
        DataTable modifications = DataTable.create(rawData);
        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataAddToJsonArrayTest_1() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key1", new JSONArray(Arrays.asList("value1")));
        String data = jsonObject.toString();
        String expectedData = "{\"key1\":[\"value1\",[\"value2\"]]}";
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "ADDTO", "[\"value2\"]", "array"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataAddToJsonArrayTest_2() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key1", new JSONArray(Arrays.asList("value1")));
        String data = jsonObject.toString();
        String expectedData = "{\"key1\":[\"value1\",[]]}";
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "ADDTO", "[]", "array"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataAddToJsonArrayTest_3() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key1", new JSONArray(Arrays.asList("value1")));
        String data = jsonObject.toString();
        String expectedData = "{\"key1\":[\"value1\",[[]]]}";
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "ADDTO", "[[]]", "array"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataAddToJsonObjectTest_1() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key1", new JSONArray(Arrays.asList("value1")));
        String data = jsonObject.toString();
        String expectedData = "{\"key1\":[\"value1\",{\"key2\": \"value2\"}]}";
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "ADDTO", "{\"key2\": \"value2\"}", "object"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataAddToJsonObjectTest_2() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key1", new JSONArray(Arrays.asList("value1")));
        String data = jsonObject.toString();
        String expectedData = "{\"key1\":[\"value1\",{}]}";
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "ADDTO", "{}", "object"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataAddToJsonStringTest_1() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key1", new JSONArray(Arrays.asList("value1")));
        String data = jsonObject.toString();
        String expectedData = "{\"key1\":[\"value1\",\"value2\"]}";
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "ADDTO", "value2", "string"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataAddToJsonStringTest_2() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key1", new JSONArray(Arrays.asList("value1")));
        String data = jsonObject.toString();
        String expectedData = "{\"key1\":[\"value1\",\"\"]}";
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "ADDTO", "", "string"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataAddToJsonNumberTest_1() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key1", new JSONArray(Arrays.asList("value1")));
        String data = jsonObject.toString();
        String expectedData = "{\"key1\":[\"value1\",666]}";
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "ADDTO", "666", "number"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataAddToJsonNumberTest_2() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key1", new JSONArray(Arrays.asList("value1")));
        String data = jsonObject.toString();
        String expectedData = "{\"key1\":[\"value1\",66.6]}";
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "ADDTO", "66.6", "number"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataAddToJsonNumberTest_3() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key1", new JSONArray(Arrays.asList("value1")));
        String data = jsonObject.toString();
        String expectedData = "{\"key1\":[\"value1\",0]}";
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "ADDTO", "0", "number"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataAddToJsonBooleanTest_1() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key1", new JSONArray(Arrays.asList("value1")));
        String data = jsonObject.toString();
        String expectedData = "{\"key1\":[\"value1\",true]}";
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "ADDTO", "true", "boolean"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataAddToJsonBooleanTest_2() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key1", new JSONArray(Arrays.asList("value1")));
        String data = jsonObject.toString();
        String expectedData = "{\"key1\":[\"value1\",false]}";
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "ADDTO", "false", "boolean"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataAddToJsonBooleanTest_3() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key1", new JSONArray(Arrays.asList("value1")));
        String data = jsonObject.toString();
        String expectedData = "{\"key1\":[\"value1\",false]}";
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "ADDTO", "", "boolean"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataAddToJsonNullTest_1() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key1", new JSONArray(Arrays.asList("value1")));
        String data = jsonObject.toString();
        String expectedData = "{\"key1\":[\"value1\",null]}";
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "ADDTO", "null", "null"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void modifyDataAddToJsonNullTest_2() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key1", new JSONArray(Arrays.asList("value1")));
        String data = jsonObject.toString();
        String expectedData = "{\"key1\":[\"value1\",null]}";
        String type = "json";
        List<List<String>> rawData = Arrays.asList(Arrays.asList("key1", "ADDTO", "", "null"));
        DataTable modifications = DataTable.create(rawData);

        String modifiedData = commong.modifyData(data, type, modifications);
        JSONAssert.assertEquals(expectedData, modifiedData, false);
    }

    @Test
    public void generateRequestNoAppURLTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String requestType = "MYREQUEST";
        String endPoint = "endpoint";
        String data = "data";
        String type = "string";

        try {
            commong.generateRequest(requestType, false, null, null, endPoint, data, type, "");
            fail("Expected Exception");
        } catch (Exception e) {
            assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
            assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("Rest host has not been set");
        }
    }

    @Test
    public void generateRequestInvalidRequestTypeTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String requestType = "MYREQUEST";
        String endPoint = "endpoint";
        String data = "data";
        String type = "string";

        try {
            commong.setRestHost("localhost");
            commong.setRestPort("80");

            commong.generateRequest(requestType, false, null, null, endPoint, data, type, "");
            fail("Expected Exception");
        } catch (Exception e) {
            assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
            assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("Operation not valid: MYREQUEST");
        }
    }

    @Test
    public void generateRequestNotImplementedRequestTypeTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String requestType = "TRACE";
        String endPoint = "endpoint";
        String data = "data";
        String type = "string";

        try {
            commong.setRestHost("localhost");
            commong.setRestPort("80");
            commong.generateRequest(requestType, false, null, null, endPoint, data, type, "");
            fail("Expected Exception");
        } catch (Exception e) {
            assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
            assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("Operation not implemented: TRACE");
        }
    }

    @Test
    public void generateRequestDataNullPUTTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String requestType = "PUT";
        String endPoint = "endpoint";
        String type = "string";

        try {
            commong.setRestHost("localhost");
            commong.setRestPort("80");
            commong.generateRequest(requestType, false, null, null, endPoint, null, type, "");
            fail("Expected Exception");
        } catch (Exception e) {
            assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
            assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("Missing fields in request.");
        }
    }

    @Test
    public void generateRequestDataNullPOSTTest() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String requestType = "POST";
        String endPoint = "endpoint";
        String type = "string";

        try {
            commong.setRestHost("localhost");
            commong.setRestPort("80");
            commong.generateRequest(requestType, false, null, null, endPoint, null, type, "");
            fail("Expected Exception");
        } catch (Exception e) {
            assertThat(e.getClass().toString()).as("Unexpected exception").isEqualTo(Exception.class.toString());
            assertThat(e.getMessage()).as("Unexpected exception message").isEqualTo("Missing fields in request.");
        }
    }

    @Test
    public void testRunLocalCommand() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String command = "echo hey";
        commong.runLocalCommand(command);
        int exitstatus = commong.getCommandExitStatus();
        String response = commong.getCommandResult();

        assertThat(exitstatus).as("Running command echo locally").isEqualTo(0);
        assertThat(response).as("Running command echo locally").isEqualTo("hey");

    }

    @Test
    public void testNonexistentLocalCommandExitStatus() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        CommonG commong = new CommonG();
        String command = "nonexistscommand";
        commong.runLocalCommand(command);
        int exitstatus = commong.getCommandExitStatus();

        assertThat(exitstatus).as("Running nonexistent command 'shur' locally").isEqualTo(127);
    }

    @Test
    public void testParseJSONFragments() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        String baseData = "fragmentsJSON.conf";

        String jsonString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(baseData).getFile())));

        CommonG commong = new CommonG();

        String value1 = null;
        String value2 = null;

        try {
            value1 = commong.getJSONPathString(jsonString, "$.id", null);
            value2 = commong.getJSONPathString(jsonString, "$.element.name", null);
        } catch (Exception e) {
            fail("Error parsing JSON String");
        }

        assertThat(value1).as("Value for id key does not match").isEqualTo("id");
        assertThat(value2).as("Value for element.name key does not match").isEqualTo("elementName");
    }

    @Test
    public void testParseJSONElasticseach() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        String baseData = "elasticJSON.conf";

        String jsonString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(baseData).getFile())));

        CommonG commong = new CommonG();

        String value1 = null;

        try {
            value1 = commong.getJSONPathString(jsonString, "$.product", null);
        } catch (Exception e) {
            fail("Error parsing JSON String");
        }

        assertThat(value1).as("Value for product key does not match").isEqualTo("productValue");
    }


    @Test
    public void testParseJSONWhere() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        String baseData = "whereJSON.conf";

        String jsonString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(baseData).getFile())));

        CommonG commong = new CommonG();

        String value1 = null;

        try {
            value1 = commong.getJSONPathString(jsonString, "$", null);
        } catch (Exception e) {
            fail("Error parsing JSON String");
        }

        assertThat(value1).as("Value does not match").isEqualTo("column = 'value'");
    }

    @Test
    public void testParseJSONConsulMesos() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        String baseData = "consulMesosJSON.conf";

        String jsonString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(baseData).getFile())));

        CommonG commong = new CommonG();

        String value1 = null;
        String value2 = null;

        try {
            value1 = commong.getJSONPathString(jsonString, "$.[0].Node", null);
            value2 = commong.getJSONPathString(jsonString, "$.[1].Node", null);
        } catch (Exception e) {
            fail("Error parsing JSON String");
        }

        assertThat(value1).as("Value for [0].Node key does not match").isEqualTo("paaslab31.stratio.com");
        assertThat(value2).as("Value for [1].Node key does not match").isEqualTo("paaslab33.stratio.com");
    }


    @Test
    public void testParseJSONConsulServices() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        String baseData = "consulServicesJSON.conf";

        String jsonString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(baseData).getFile())));

        CommonG commong = new CommonG();

        String value1 = null;
        String value2 = null;

        try {
            value1 = commong.getJSONPathString(jsonString, "$.~[0]", null);
            value2 = commong.getJSONPathString(jsonString, "$.~[2]", null);
        } catch (Exception e) {
            fail("Error parsing JSON String: " + e.toString());
        }

        assertThat(value1).as("key in 0 does not match").isEqualTo("mesos");
        assertThat(value2).as("key in 2 does not match").isEqualTo("consul");

    }

    @Test
    public void testParseJSONConsulMesosFrameworks() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        String baseData = "mesosFrameworks.conf";

        String jsonString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(baseData).getFile())));

        CommonG commong = new CommonG();

        String value1 = null;

        try {
            value1 = commong.getJSONPathString(jsonString, "$.frameworks[?(@.name == \"arangodb3\")].hostname", "0");
        } catch (Exception e) {
            fail("Error parsing JSON String");
        }

        assertThat(value1).as("Value for search does not match").isEqualTo("paaslab34.stratio.com");
    }


}
