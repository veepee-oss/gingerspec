package com.privalia.qa.utils;

import com.jayway.jsonpath.JsonPath;
import cucumber.api.DataTable;
import org.hjson.JsonObject;
import org.hjson.JsonType;
import org.hjson.JsonValue;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Generic utilities for operations with Json and text formats.
 */
public class JsonUtils {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(CassandraUtils.class);

    /**
     * Returns the information modified
     *
     * @param data          string containing the information
     * @param type          type of information, it can be: json
     * @param modifications modifications to apply with a format:
     *                      WHERE,ACTION,VALUE
     * @return String
     * @throws Exception
     */
    public String modifyDataJson(String data, String type, DataTable modifications) throws Exception {
        String modifiedData = data;
        String typeJsonObject = "";
        String nullValue = "";

        JSONArray jArray;
        JSONObject jObject;
        Double jNumber;
        Boolean jBoolean;
        LinkedHashMap jsonAsMap = new LinkedHashMap();


        for (int i = 0; i < modifications.raw().size(); i++) {
            String composeKey = modifications.raw().get(i).get(0);
            String operation = modifications.raw().get(i).get(1);
            String newValue = modifications.raw().get(i).get(2);

            if (modifications.raw().get(0).size() == 4) {
                typeJsonObject = modifications.raw().get(i).get(3);
            }

            JsonObject object = new JsonObject(JsonValue.readHjson(modifiedData).asObject());
            removeNulls(object);
            modifiedData = JsonValue.readHjson(object.toString()).toString();

            switch (operation.toUpperCase()) {
                case "DELETE":
                    jsonAsMap = JsonPath.parse(modifiedData).delete(composeKey).json();
                    break;
                case "ADD":
                    // Get the last key
                    String newKey;
                    String newComposeKey;
                    if (composeKey.contains(".")) {
                        newKey = composeKey.substring(composeKey.lastIndexOf('.') + 1);
                        newComposeKey = composeKey.substring(0, composeKey.lastIndexOf('.'));
                    } else {
                        newKey = composeKey;
                        newComposeKey = "$";
                    }
                    jsonAsMap = JsonPath.parse(modifiedData).put(newComposeKey, newKey, newValue).json();
                    break;
                case "UPDATE":
                    jsonAsMap = JsonPath.parse(modifiedData).set(composeKey, newValue).json();
                    break;
                case "APPEND":
                    String appendValue = JsonPath.parse(modifiedData).read(composeKey);
                    jsonAsMap = JsonPath.parse(modifiedData).set(composeKey, appendValue + newValue).json();
                    break;
                case "PREPEND":
                    String prependValue = JsonPath.parse(modifiedData).read(composeKey);
                    jsonAsMap = JsonPath.parse(modifiedData).set(composeKey, newValue + prependValue).json();
                    break;
                case "REPLACE":
                    if ("array".equals(typeJsonObject)) {
                        jArray = new JSONArray();
                        if (!"[]".equals(newValue)) {
                            jArray = new JSONArray(newValue);
                        }
                        jsonAsMap = JsonPath.parse(modifiedData).set(composeKey, jArray).json();
                        break;
                    } else if ("object".equals(typeJsonObject)) {
                        jObject = new JSONObject();
                        if (!"{}".equals(newValue)) {
                            jObject = new JSONObject(newValue);
                        }
                        jsonAsMap = JsonPath.parse(modifiedData).set(composeKey, jObject).json();
                        break;
                    } else if ("string".equals(typeJsonObject)) {
                        jsonAsMap = JsonPath.parse(modifiedData).set(composeKey, newValue).json();
                        break;

                    } else if ("number".equals(typeJsonObject)) {
                        jNumber = new Double(newValue);
                        jsonAsMap = JsonPath.parse(modifiedData).set(composeKey, jNumber).json();
                        break;

                    } else if ("boolean".equals(typeJsonObject)) {
                        jBoolean = new Boolean(newValue);
                        jsonAsMap = JsonPath.parse(modifiedData).set(composeKey, jBoolean).json();
                        break;

                    } else if ("null".equals(typeJsonObject)) {
                        nullValue = JsonPath.parse(modifiedData).set(composeKey, null).jsonString();
                        break;

                    } else {
                        String replaceValue = JsonPath.parse(modifiedData).read(composeKey);
                        String toBeReplaced = newValue.split("->")[0];
                        String replacement = newValue.split("->")[1];
                        newValue = replaceValue.replace(toBeReplaced, replacement);
                        jsonAsMap = JsonPath.parse(modifiedData).set(composeKey, newValue).json();
                        break;
                    }
                case "ADDTO":
                    if ("array".equals(typeJsonObject)) {
                        jArray = new JSONArray();
                        if (!"[]".equals(newValue)) {
                            jArray = new JSONArray(newValue);
                        }
                        jsonAsMap = JsonPath.parse(modifiedData).add(composeKey, jArray).json();
                        break;
                    } else if ("object".equals(typeJsonObject)) {
                        jObject = new JSONObject();
                        if (!"{}".equals(newValue)) {
                            jObject = new JSONObject(newValue);
                        }
                        jsonAsMap = JsonPath.parse(modifiedData).add(composeKey, jObject).json();
                        break;
                    } else if ("string".equals(typeJsonObject)) {
                        jsonAsMap = JsonPath.parse(modifiedData).add(composeKey, newValue).json();
                        break;
                    } else if ("number".equals(typeJsonObject)) {
                        jNumber = new Double(newValue);
                        jsonAsMap = JsonPath.parse(modifiedData).add(composeKey, jNumber).json();
                        break;
                    } else if ("boolean".equals(typeJsonObject)) {
                        jBoolean = new Boolean(newValue);
                        jsonAsMap = JsonPath.parse(modifiedData).add(composeKey, jBoolean).json();
                        break;
                    } else if ("null".equals(typeJsonObject)) {
                        nullValue = JsonPath.parse(modifiedData).add(composeKey, null).jsonString();
                        break;
                    } else {
                        // TO-DO: understand  newValue.split("->")[0];  and  newValue.split("->")[1];
                        break;
                    }
                default:
                    throw new Exception("Modification type does not exist: " + operation);
            }

            modifiedData = new JSONObject(jsonAsMap).toString();
            if (!"".equals(nullValue)) {
                modifiedData = nullValue;
            }
            modifiedData = modifiedData.replaceAll("\"TO_BE_NULL\"", "null");
        }
        return modifiedData;
    }

    /**
     * Returns the information modified
     *
     * @param data          string containing the information
     * @param type          type of information, it can be: string
     * @param modifications modifications to apply with a format:
     *                      WHERE,ACTION,VALUE
     * @return String
     * @throws Exception
     */
    public String modifyDataString(String data, String type, DataTable modifications) throws Exception {

        String modifiedData = data;

        for (int i = 0; i < modifications.raw().size(); i++) {
            String value = modifications.raw().get(i).get(0);
            String operation = modifications.raw().get(i).get(1);
            String newValue = modifications.raw().get(i).get(2);

            switch (operation.toUpperCase()) {
                case "DELETE":
                    modifiedData = modifiedData.replace(value, "");
                    break;
                case "ADD":
                case "APPEND":
                    modifiedData = modifiedData + newValue;
                    break;
                case "UPDATE":
                case "REPLACE":
                    modifiedData = modifiedData.replace(value, newValue);
                    break;
                case "PREPEND":
                    modifiedData = newValue + modifiedData;
                    break;
                default:
                    throw new Exception("Modification type does not exist: " + operation);
            }
        }
        return modifiedData;
    }

    /**
     * Eliminates null occurrences, replacing them with "TO_BE_NULL"
     *
     * @param object JsonObject containing json where to replace null ocurrences
     * @return JsonObject
     */
    public JsonObject removeNulls(JsonObject object) {
        for (int j = 0; j < object.names().size(); j++) {
            if (JsonType.OBJECT.equals(object.get(object.names().get(j)).getType())) {
                removeNulls(object.get(object.names().get(j)).asObject());
            } else {
                if (object.get(object.names().get(j)).isNull()) {
                    object.set(object.names().get(j), "TO_BE_NULL");
                }
            }
        }
        return object;
    }

}
