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

import com.jayway.jsonpath.PathNotFoundException;
import com.privalia.qa.exceptions.NonReplaceableException;
import com.privalia.qa.utils.ThreadProperty;
import io.cucumber.datatable.DataTable;
import io.cucumber.docstring.DocString;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.ResponseBody;
import io.restassured.specification.ProxySpecification;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.assertj.core.api.Assertions;
import org.hjson.JsonValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps definitions for testing REST services
 *
 * @see <a href="https://github.com/rest-assured/rest-assured">https://github.com/rest-assured/rest-assured</a>
 * @author Jose Fernandez
 */
public class RestSpec extends BaseGSpec {

    public RestSpec(CommonG spec) {
        this.commonspec = spec;
    }

    /**
     * Set app host and port for Rest requests.
     * <p>
     * This is an initialization step. This is used as the first step in rest features to configure the basepath url.
     * This parameters will be used for all future requests in the same scenario. The rest request is build within
     * the {@link HookGSpec} class, so, don't forget to use the {@literal @}rest annotation at the beginning of your
     * feature for a proper initialization.
     * <pre>
     * {@code
     * Examples
     *
     * Scenario: Setting up the host.
     *      Given I send requests to 'jsonplaceholder.typicode.com'
     *
     * Scenario: Setting up host and specific port
     *      Given I send requests to 'jsonplaceholder.typicode.com:8080'
     *
     * Scenario: using the keyword 'securely' to use https.
     *      Given I securely send requests to 'jsonplaceholder.typicode.com'
     * }
     * </pre>
     *
     * @param isSecured     Indicates if https:// should be used (if false, defaults to http://)
     * @param restHost      Base url of the API (without the http/https prefix, like mybaseurl.com). You can also specify a port number with baseURL:port
     */
    @Given("^I( securely)? send requests to '(.*)'$")
    public void setupApp(String isSecured, String restHost) {

        RequestSpecification spec = new RequestSpecBuilder().setContentType(ContentType.JSON).setRelaxedHTTPSValidation().build();
        commonspec.setRestRequest(given().header("Content-Type", "application/json").spec(spec));

        String restProtocol = "http://";
        String restPort = null;

        if (isSecured != null) {
            restProtocol = "https://";
            commonspec.getRestRequest().relaxedHTTPSValidation();
        }

        Assertions.assertThat(restHost).isNotNull();
        Assertions.assertThat(restHost).as("Malformed url. No need to use http(s):// prefix").doesNotContain("http://").doesNotContain("https://");

        String[] restAddress = restHost.split(":");

        if (restAddress.length == 2) {
            restHost = restAddress[0];
            restPort = restAddress[1];
            restPort = restPort.replace(":", "");
        }

        this.getCommonSpec().getLogger().debug("Setting base URL to {}", restProtocol + restHost);
        commonspec.getRestRequest().baseUri(restProtocol + restHost);

        if (restPort != null) {
            this.getCommonSpec().getLogger().debug("Setting port to {}", Integer.parseInt(restPort));
            commonspec.getRestRequest().port(Integer.parseInt(restPort));
        }

        commonspec.setRestHost(restHost);
        commonspec.setRestPort(restPort);
        commonspec.setRestProtocol(restProtocol);

    }

    /**
     * Verifies the structure of a json document against a set of test cases defined in a datatable
     * <p>
     * This step is typically used to verify the response body of a request. The json to verify
     * must have been previously saved in a variable using for example
     * {@link #saveElementEnvironment(String, String, String)}. In the datatable, the first column represents the
     * jsonpath of the element in the body to verify, the second column the operator operator (equal|not equal|contains|does not contain|length|exists|does not exists|size)
     * and the third column the value to match against
     * <pre>{@code
     * Example
     *
     * Scenario: Saving result in variable 'response' and evaluating
     *      Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *      When I send a 'POST' request to '/posts' based on 'schemas/mytestdata.json' as 'json'
     *      And I save element '$' in environment variable 'response'
     *      And 'response' matches the following cases:
     *       | $.title  | contains  | 2              |
     *       | $.body   | contains  | This is a test |
     *       | $.userId | not equal | 2              |
     * }</pre>
     * @see #saveElementEnvironment(String, String, String)
     * @param envVar        Environment variable where JSON is stored
     * @param table         Data table in which each row stores one expression
     */
    @Then("^'(.*)' matches the following cases:$")
    public void matchWithExpresion(String envVar, DataTable table) {
        String jsonString = ThreadProperty.get(envVar);

        Assertions.assertThat(jsonString).as("The variable '" + envVar + "' was not set correctly previously").isNotNull();

        for (List<String> row : table.asLists()) {
            String jsonPath = row.get(0);
            String condition = row.get(1);
            String result = row.get(2);

            String value = commonspec.getJSONPathString(jsonString, jsonPath, null);
            commonspec.evaluateJSONElementOperation(value, condition, result, jsonPath);
        }
    }

    /**
     * Generates a REST request of the type specified to the indicated endpoint
     * <p>
     * The endpoint must be relative to the base path previously defined with {@link #setupApp(String, String)}. If needed, you can also specify
     * the body of the request (for POST, PUT and DELETE requests) from a local file. If you need to alter the content of the json document before
     * sending you could use {@link #sendRequestDataTable(String, String, String, String, String, String, DataTable)}. As soon as the request is completed, the
     * request object is re-initialized with the same base URI and port as configured in {@link #setupApp(String, String)}. This is to avoid future requests
     * from re-using the same cookies/headers/url parameters that the user may have configured.
     * <pre>{@code
     * Examples:
     *
     * Scenario: Executing a simple GET request
     *      Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *      When I send a 'GET' request to '/posts'
     *
     * Scenario: Using basic authentication
     *      Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *      When I send a 'GET' request to '/posts' with user and password 'user:password'
     *
     * Scenario: Sending a POST request with content of file mytestdata.json as body
     *      Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *      When I send a 'POST' request to '/posts' based on 'schemas/mytestdata.json' as 'json'
     *
     * Scenario: Sending a POST request with content of file mytestdata.graphql as body
     *      Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *      When I send a 'POST' request to '/' based on 'schemas/mytestdata.graphql' as 'graphql'
     *
     * Scenario: Sending a POST request with content of file mytestdata.graphql with variables as body
     *      Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *      When I send a 'POST' request to '/' based on 'schemas/mytestdata.graphql' as 'graphql' with variables '{"perPage": 10}' and
     * }</pre>
     *
     * @see #setupApp(String, String)
     * @see #sendRequestDataTable(String, String, String, String, String, String, DataTable)
     * @see #sendRequestInlineBody(String, String, String, String, DocString)
     * @param requestType   HTTP verb (type of request): POST, GET, PUT, PATCH, DELETE
     * @param endPoint      Endpoint (i.e /user/1). The base path used is the one indicated in a previous step
     * @param loginInfo     User and password to use if the endpoints requires basic authentication (user:password)
     * @param baseData      If specified, the content of the file will be loaded in the body of the request (POST, PUT, PATCH operations)
     * @param type          If the content of the file should be read as string or json or graphql
     * @param variables     If the content as graphql
     * @throws Exception    Exception
     */
    @When("^I send a '(.+?)' request to '(.+?)'( with user and password '(.+:.+?)')?( based on '([^:]+?)')?( as '(json|string|graphql)')?( with variables '(.+?)')?$")
    public void sendRequestNoDataTable(String requestType, String endPoint, String loginInfo, String baseData, String type, String variables) throws Exception {

        String retrievedData;
        String user = null;
        String password = null;

        if (loginInfo != null) {
            user = loginInfo.substring(0, loginInfo.indexOf(':'));
            password = loginInfo.substring(loginInfo.indexOf(':') + 1, loginInfo.length());
            commonspec.getRestRequest().auth().preemptive().basic(user, password);
        }

        if (baseData != null) {
            retrievedData = commonspec.retrieveData(baseData, type);

            if (type.equals("graphql")) {
                retrievedData = this.commonspec.buildGraphql(retrievedData, variables);
            }

            commonspec.getRestRequest().given().body(retrievedData);
        }

        // Save response
        commonspec.generateRestRequest(requestType, endPoint);
        commonspec.getLogger().debug("Saving response");
        this.initializeRestClient();

    }

    /**
     * Generates a REST request of the type specified to the indicated endpoint
     * <p>
     * This function works in the same way as {@link #sendRequestNoDataTable(String, String, String, String, String, String)}
     * the difference is that this one accepts a datatable with a list of modification to be applied to the json
     * body before the request is executed. As soon as the request, is completed, the request object is re-initialized
     * with the same base URI and port as configured in {@link #setupApp(String, String)}. This is to avoid future requests
     * from re-using the same cookies/headers/url parameters that the user may have configured.
     *
     * <pre>{@code
     * Example
     *
     * Scenario: Send a POST request with the content of schemas/mytestdata.json as body data
     *      Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *      When I send a 'POST' request to '/posts' based on 'schemas/mytestdata.json' as 'json' with:
     *          | $.title | UPDATE | This is a test 2 |
     *
     *
     * About the modifications datatable: The datatable will typically have the following
     * structure:
     *
     *      | <key path> | <type of modification> | <new value> | <object type> (optional) |
     *
     * <key path>: jsonPath to the key to be modified.
     * <type of modification>: DELETE|ADD|UPDATE|APPEND|PREPEND|REPLACE|ADDTO
     * <new value>: In case of UPDATE or ADD, new value to be used.
     *
     *              If the element read is: {"key1": "value1", "key2": {"key3": "value3"}}
     *              And the modifications datatable is: | key2.key3 | UPDATE | "new value3" |
     *              The result will be: {"key1": "value1", "key2": {"key3": "new value3"}}
     *
     *              (The new value will always by added as a string, that is, will contain double
     *              quotes "". If you want to override this behaviour, use REPLACE and specify the <object type> column)
     *
     * <object type>: In case of REPLACE and ADDTO, specifies the object type the value should be transformed to.
     *                Accepted values are: array|object|string|number|array|boolean|null. Use null if you want that
     *                value in the json to be null.
     *
     * }</pre>
     * @see #setupApp(String, String)
     * @see #sendRequestNoDataTable(String, String, String, String, String, String)
     * @see #sendRequestInlineBody(String, String, String, String, DocString)
     * @param requestType   Type of request to be sent. Possible values: GET|DELETE|POST|PUT|PATCH
     * @param endPoint      End point to be used (relative to the base path previously defined with {@link #setupApp(String, String)})
     * @param baseData      Path to file containing the schema to be used
     * @param type          Element to read from file (element should contain a json)
     * @param variables     If the content as graphql
     * @param loginInfo     Credentials for basic auth (if required)
     * @param modifications DataTable containing the modifications to be done to the
     *                      base schema element.
     * @throws Exception    Exception
     */
    @When("^I send a '(.+?)' request to '(.+?)'( with user and password '(.+:.+?)')? based on '([^:]+?)'( as '(json|string|graphql)')? with( variables '(.+?)' and)?:$")
    public void sendRequestDataTable(String requestType, String endPoint, String loginInfo, String baseData, String type, String variables, DataTable modifications) throws Exception {

        String user;
        String password;

        // Retrieve data
        String retrievedData = commonspec.retrieveData(baseData, type);

        if (type.equals("graphql")) {
            retrievedData = this.commonspec.buildGraphql(retrievedData, variables);
        }

        // Modify data
        commonspec.getLogger().debug("Modifying data {} as {}", retrievedData, type);
        String modifiedData = commonspec.modifyData(retrievedData, type, modifications).toString();
        commonspec.getRestRequest().given().body(modifiedData);


        if (loginInfo != null) {
            user = loginInfo.substring(0, loginInfo.indexOf(':'));
            password = loginInfo.substring(loginInfo.indexOf(':') + 1, loginInfo.length());
            commonspec.getRestRequest().auth().preemptive().basic(user, password);
        }

        // Save response
        commonspec.generateRestRequest(requestType, endPoint);
        commonspec.getLogger().debug("Saving response");
        this.initializeRestClient();

    }

    /**
     * Verifies the status response (HTTP response code) of a rest request.
     * <p>
     * This step was deprecated, please, use {@link #assertResponseStatusCode(Integer)}, {@link #assertResponseMessage(String)},
     * {@link #assertResponseLength(Integer)} or {@link #assertResponseSchema(String)}
     *
     * @param expectedStatus Expected HTTP status code
     * @param responseAssert Expression to determine if assert length, text or schema
     * @throws NonReplaceableException the non replaceable exception
     * @throws ConfigurationException  the configuration exception
     * @throws FileNotFoundException   the file not found exception
     * @throws URISyntaxException      the uri syntax exception
     * @see #assertResponseStatusCode(Integer) #assertResponseStatusCode(Integer)
     * @see #assertResponseMessage(String) #assertResponseMessage(String)
     * @see #assertResponseLength(Integer) #assertResponseLength(Integer)
     * @see #assertResponseSchema(String) #assertResponseSchema(String)
     * @see <a href="http://json-schema.org/">http://json-schema.org/</a>
     */
    @Deprecated
    public void assertResponseStatusLength(Integer expectedStatus, String responseAssert) throws NonReplaceableException, ConfigurationException, FileNotFoundException, URISyntaxException {

        commonspec.getRestResponse().then().statusCode(expectedStatus);

        if (responseAssert == null) {
            return;
        }

        String[] parts = responseAssert.split("'");


        if (responseAssert.contains("text")) {
            assertResponseMessage(parts[1]);
        }

        if (responseAssert.contains("schema")) {
            String schemaData = commonspec.retrieveData(parts[1], "json");
            commonspec.getRestResponse().then().assertThat().body(matchesJsonSchema(schemaData));
        }

        if (responseAssert.contains("length")) {
            Assertions.assertThat(commonspec.getRestResponse().getBody().asString().length()).as("The returned body does not have the expected length").isEqualTo(Integer.valueOf(parts[1]));
        }

    }

    /**
     * Verifies the response body matches a json schema.
     * <p>
     * For this step to work, a previous request must have been executed such as {@link #sendRequestNoDataTable(String, String, String, String, String, String)}
     * or {@link #sendRequestDataTable(String, String, String, String, String, String, DataTable)}. The given file must contain a valid json schema (http://json-schema.org/)
     * <pre>{@code
     * Example:
     *
     * Scenario: Verify that the response body matches a json schema
     *     Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *     When I send a 'GET' request to '/posts'
     *     Then the service response matches the schema in 'schemas/responseSchema.json'
     * }*</pre>
     *
     * @param expectedSchema File under /resources directory that contains the expected schema
     * @throws NonReplaceableException the non replaceable exception
     * @throws ConfigurationException  the configuration exception
     * @throws FileNotFoundException   the file not found exception
     * @throws URISyntaxException      the uri syntax exception
     * @see #sendRequestNoDataTable(String, String, String, String, String, String) #sendRequestNoDataTable(String, String, String, String, String, String)
     * @see #sendRequestDataTable(String, String, String, String, String, String, DataTable) #sendRequestDataTable(String, String, String, String, String, String, DataTable)
     * @see <a href="http://json-schema.org/">http://json-schema.org/</a>
     */
    @Then("^the service response matches the schema in '(.*?)'$")
    public void assertResponseSchema(String expectedSchema) throws NonReplaceableException, ConfigurationException, FileNotFoundException, URISyntaxException {
        String schemaData = commonspec.retrieveData(expectedSchema, "json");
        commonspec.getRestResponse().then().assertThat().body(matchesJsonSchema(schemaData));
    }

    /**
     * Verifies the length of the response body.
     * <p>
     * For this step to work, a previous request must have been executed such as {@link #sendRequestNoDataTable(String, String, String, String, String, String)}
     * or {@link #sendRequestDataTable(String, String, String, String, String, String, DataTable)}
     * <pre>{@code
     * Example:
     *
     * Scenario: Checking the response body length
     *     Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *     When I send a 'GET' request to '/comments/1'
     *     Then the service response length must be '268'
     * }</pre>
     * @see #sendRequestNoDataTable(String, String, String, String, String, String)
     * @see #sendRequestDataTable(String, String, String, String, String, String, DataTable)
     * @see <a href="http://json-schema.org/">http://json-schema.org/</a>
     * @param expextedLength        Expected response body length
     */
    @Then("^the service response length must be '(.*?)'$")
    public void assertResponseLength(Integer expextedLength) {
        Assertions.assertThat(commonspec.getRestResponse().getBody().asString().length()).as("The returned body does not have the expected length").isEqualTo(expextedLength);
    }

    /**
     * Verifies the status response (HTTP response code) of a rest request.
     * <p>
     * For this step to work, a previous request must have been executed such as {@link #sendRequestNoDataTable(String, String, String, String, String, String)}
     * or {@link #sendRequestDataTable(String, String, String, String, String, String, DataTable)}
     * <pre>{@code
     * Example:
     *
     * Scenario: Verify the response status code
     *      Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *      When I send a 'GET' request to '/posts'
     *      Then the service response status must be '200'
     * }</pre>
     * @see #sendRequestNoDataTable(String, String, String, String, String, String)
     * @see #sendRequestDataTable(String, String, String, String, String, String, DataTable)
     * @see <a href="http://json-schema.org/">http://json-schema.org/</a>
     * @param expectedStatus        Expected HTTP status code
     */
    @Then("^the service response status must be '(.*?)'$")
    public void assertResponseStatusCode(Integer expectedStatus) {
        commonspec.getRestResponse().then().statusCode(expectedStatus);
    }

    /**
     * Verifies if the response body contains an specific string
     * <p>
     * For this step to work, a previous request must have been executed such as {@link #sendRequestNoDataTable(String, String, String, String, String, String)}
     * or {@link #sendRequestDataTable(String, String, String, String, String, String, DataTable)}
     * <pre>{@code
     * Example:
     *
     * Scenario: checking if body contains the string 'body'
     *     Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *     When I send a 'GET' request to '/posts'
     *     And the service response must contain the text 'body'
     * }
     * </pre>
     * @see #sendRequestDataTable(String, String, String, String, String, String, DataTable)
     * @see #sendRequestNoDataTable(String, String, String, String, String, String)
     * @see #assertResponseStatusLength(Integer, String)
     * @param expectedText  String to find in the response body
     */
    @Then("^the service response must contain the text '(.*)'$")
    public void assertResponseMessage(String expectedText) {
        ResponseBody body = commonspec.getRestResponse().getBody();
        String bodyAsString = body.asString();
        Assertions.assertThat(bodyAsString).as("Text '" + expectedText + "' was not found in response body").contains(expectedText);
    }


    /**
     * Saves value of a json document for future use.
     * <p>
     * This step is typically used to save the body response of a HTTP request (either the full body
     * response or just an specific element). If this is the case, a previous HTTP request operation
     * must have been performed (with {@link #sendRequestDataTable(String, String, String, String, String, String, DataTable)} or with
     * {@link #sendRequestNoDataTable(String, String, String, String, String, String)})
     * <pre>{@code
     * Example: If element is a jsonpath expression (i.e. $.fragments[0].id), it will be applied over the last httpResponse.
     *
     * Scenario: Saving ALL body in variable 'response'
     *      Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *      When I send a 'GET' request to '/posts'
     *      And I save element '$' in environment variable 'response'
     *
     * Scenario: Saves only the element $.[0].userId of the response
     *      Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *      When I send a 'GET' request to '/posts'
     *      And I save element '$.[0].userId' in environment variable 'USER_ID'
     *
     * Example: If element is a jsonpath expression preceded by some other string (i.e. ["a","b",,"c"].$.[0]), it will be applied over
     * this string. This will help to save the result of a jsonpath expression evaluated over previous stored variable.
     *
     * Scenario: Evaluating a simple string (Will save 'a' in variable 'letter')
     *      And I save element '["a","b","c","d"].$.[0]' in environment variable 'letter'
     *
     * Scenario: Or from a previous HTTP request:
     *      Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *      When I send a 'GET' request to '/users'
     *      And I save element '$.[0]' in environment variable 'first_user'
     *      And I save element '${first_user}.$.username' in environment variable 'username'
     *      Then '${username}' matches 'Bret'
     * }</pre>
     * @see #sendRequestNoDataTable(String, String, String, String, String, String)
     * @see #sendRequestDataTable(String, String, String, String, String, String, DataTable)
     * @see <a href="http://json-schema.org/">http://json-schema.org/</a>
     * @param position position from a search result
     * @param element  key in the json response to be saved
     * @param envVar   thread environment variable where to store the value
     */
    @Given("^I save element (in position '(.+?)' in )?'(.+?)' in environment variable '(.+?)'$")
    public void saveElementEnvironment(String position, String element, String envVar) {

        Pattern pattern = Pattern.compile("^((.*)(\\.)+)(\\$.*)$");
        Matcher matcher = pattern.matcher(element);
        String json;
        String parsedElement;

        if (matcher.find()) {
            json = matcher.group(2);
            parsedElement = matcher.group(4);
        } else {
            json = commonspec.getRestResponse().getBody().asString();
            parsedElement = element;
        }

        String value = "";
        try {
            value = commonspec.getJSONPathString(json, parsedElement, position);
        } catch (PathNotFoundException pe) {
            Assertions.fail("The given path was not found: " + pe.getMessage());
        }

        Assertions.assertThat(value).as("json result is empty").isNotEmpty();
        this.getCommonSpec().getLogger().debug("Element {} found. Equal to {}. Saving in variable '{}'", element, value, envVar);
        ThreadProperty.set(envVar, value);
    }

    /**
     * Specify a custom map of headers to be added to future requests
     * <p>
     * The headers will be applied the following request
     * <pre>{@code
     * Example:
     *
     * Scenario: Set headers for following requests. This (and following) request(s) will contain those headers
     *      Given I send requests to 'dummy-test.com:80'
     *      Given I set headers:
     *          | Authorization  | mySecretToken1234 |
     *          | Content-Type   | application/json  |
     *      When I send a 'GET' request to '/api/v1/shipment/1'
     * }</pre>
     * @see #setCookies(DataTable)
     * @param modifications DataTable containing the custom set of headers to be
     *                      added to the requests. Syntax will be:
     *                      {@code
     *                      | <key> | <value> |
     *                      }
     *                      where:
     *                      key: header key name
     *                      value: value for tue key
     *                      for example:
     *                      if we want to add the header "token" with value "12345678", to the request header
     *                      the modification will be:
     *                      | token | 12345678 |
     */
    @Given("^I set headers:$")
    public void setHeaders(DataTable modifications) {

        Map<String, String> headers = new HashMap<>();

        for (List<String> row: modifications.asLists()) {
            String key = row.get(0);
            String value = row.get(1);
            headers.put(key, value);
            this.getCommonSpec().getLogger().debug("Setting header '{}' with value '{}'", key, value);
            commonspec.getRestRequest().header(key, value);
        }

    }

    /**
     * Specify a custom map of cookies to be added to future requests
     * <p>
     * Works in a similar way that {@link #setHeaders(DataTable)}. The cookies will be applied
     * to the following request in the same scenario
     * <pre>{@code
     * Example:
     *
     * Scenario: Set cookies for following requests
     *      Given I send requests to 'dummy-test.com:80'
     *      Given I set cookies:
     *          | myCookieName  | myCookieValue |
     *      When I send a 'GET' request to '/api/v1/shipment/1' //this (and following) request(s) will contain those cookies
     * }</pre>
     * @see #setHeaders(DataTable)
     * @param modifications DataTable containing the custom set of cookies to be
     *                      added to the requests. Syntax will be:
     *                      {@code
     *                      | <key> | <value> |
     *                      }
     *                      where:
     *                      key: cookie key name
     *                      value: cookie for tue key
     *                      for example:
     *                      if we want to add the cookie "token" with value "12345678", to the request cookie
     *                      the modification will be:
     *                      | token | 12345678 |
     */
    @Given("^I set cookies:$")
    public void setCookies(DataTable modifications) {

        Map<String, String> cookies = new HashMap<>();

        for (List<String> row: modifications.asLists()) {
            String key = row.get(0);
            String value = row.get(1);
            cookies.put(key, value);
            this.getCommonSpec().getLogger().debug("Setting cookie '{}' with value '{}'", key, value);
            commonspec.getRestRequest().cookie(key, value);
        }

    }

    /**
     * Executes the given request to the REST endpont for the specified amount of time in regular intervals, until the response body contains
     * the specified text
     *
     * @param timeout       Maximum time to wait for the text to be present in the response body
     * @param wait          Time between retries
     * @param requestType   Type of request (POST, GET, PATCH, DELETE, PUT)
     * @param endPoint      Endpoint (i.e /user/1)
     * @param responseVal   Expected value to evaluate in the response body
     * @param contains      parameter generated by cucumber because of the optional expression
     * @throws InterruptedException InterruptedException
     */
    @Deprecated
    @When("^in less than '(\\d+)' seconds, checking each '(\\d+)' seconds, I send a '(.*)' request to '(.*)' so that the response( does not)? contains '(.*)'$")
    public void sendRequestTimeout(Integer timeout, Integer wait, String requestType, String endPoint, String contains, String responseVal) throws InterruptedException {

        Boolean searchUntilContains;
        if (contains == null || contains.isEmpty()) {
            searchUntilContains = Boolean.TRUE;
        } else {
            searchUntilContains = Boolean.FALSE;
        }
        Boolean found = !searchUntilContains;
        AssertionError ex = null;

        String type = "";
        Pattern pattern = CommonG.matchesOrContains(responseVal);
        for (int i = 0; (i <= timeout); i += wait) {
            if (found && searchUntilContains) {
                break;
            }

            commonspec.generateRestRequest(requestType, endPoint);
            commonspec.getLogger().debug("Checking response value");

            ResponseBody body = commonspec.getRestResponse().getBody();
            String bodyAsString = body.asString();

            try {
                if (searchUntilContains) {
                    assertThat(bodyAsString).containsPattern(pattern);
                    found = true;
                    timeout = i;
                } else {
                    assertThat(bodyAsString).doesNotContain(responseVal);
                    found = false;
                    timeout = i;
                }
            } catch (AssertionError e) {
                if (!found) {
                    commonspec.getLogger().info("Response value not found after " + i + " seconds");
                } else {
                    commonspec.getLogger().info("Response value found after " + i + " seconds");
                }
                Thread.sleep(wait * 1000);
                ex = e;
            }
            if (!found && !searchUntilContains) {
                break;
            }
        }
        if ((!found && searchUntilContains) || (found && !searchUntilContains)) {
            throw (ex);
        }
        if (searchUntilContains) {
            commonspec.getLogger().info("Success! Response value found after " + timeout + " seconds");
        } else {
            commonspec.getLogger().info("Success! Response value not found after " + timeout + " seconds");
        }
    }

    /**
     * Checks if the headers in the response matches the specified values
     * <p>
     * A previous HTTP request operation must have been executed, such as {@link #sendRequestNoDataTable(String, String, String, String, String, String)}
     * or {@link #sendRequestDataTable(String, String, String, String, String, String, DataTable)}
     * <pre>{@code
     * Example:
     *
     * Scenario: Check value of response headers
     *     Given I send requests to 'dummy-test.com:80'
     *     When I send a 'GET' request to '/api/v1/shipment/1' as 'json'
     *     Then the service response status must be '200'
     *     And the service response headers match the following cases:
     *       | Server           | equal           | nginx |
     *       | Content-Encoding | equal           | gzip  |
     * }</pre>
     *
     * @see #sendRequestDataTable(String, String, String, String, String, String, DataTable)
     * @see #sendRequestNoDataTable(String, String, String, String, String, String)
     * @param table DataTable containing the custom set of headers to be
     *                      added to the requests. Syntax will be:
     *                      {@code
     *                      | <header name> | <condition> | <expected value>
     *                      }
     *                      where:
     *                      header name: Header name
     *                      condition: Condition that is going to be evaluated (available: equal,
     *                      not equal, exists, does not exists, contains, does not contain, length, size)
     *                      expected value: Value used to verify the condition
     *                      for example:
     *                      If we want to verify that the header "Content-Encoding" is equal
     *                      to "application/json" we would do
     *                      | Content-Encoding | equal | application/json |
     */
    @And("^the service response headers match the following cases:$")
    public void checkHeaders(DataTable table) {

        for (List<String> row: table.asLists()) {
            String header = row.get(0);
            String condition = row.get(1);
            String result = row.get(2);

            this.getCommonSpec().getLogger().debug("Checking if header '{}' is '{}' to/than {}", header, condition, result);
            String headerValue = commonspec.getRestResponse().getHeaders().getValue(header);
            commonspec.evaluateJSONElementOperation(headerValue, condition, result, header);
        }

    }

    /**
     * Checks if the cookies in the response matches the specified values
     * <p>
     * Works in a similar way that {@link #checkHeaders(DataTable)}. A previous HTTP request operation must have been executed
     * such as {@link #sendRequestNoDataTable(String, String, String, String, String, String)} or {@link #sendRequestDataTable(String, String, String, String, String, String, DataTable)}
     * @see #checkHeaders(DataTable)
     * @see #sendRequestDataTable(String, String, String, String, String, String, DataTable)
     * @see #sendRequestNoDataTable(String, String, String, String, String, String)
     * @param table DataTable containing the custom set of cookies to be
     *                      added to the requests. Syntax will be:
     *                      {@code
     *                      | <cookies name> | <condition> | <expected value>
     *                      }
     *                      where:
     *                      cookies name: Header name
     *                      condition: Condition that is going to be evaluated (available: equal,
     *                      not equal, exists, does not exists, contains, does not contain, length, size)
     *                      expected value: Value used to verify the condition
     *                      for example:
     *                      If we want to verify that the cookies "Content-Encoding" is equal
     *                      to "application/json" we would do
     *                      | Content-Encoding | equal | application/json |
     */
    @And("^the service response cookies match the following cases:$")
    public void checkCookies(DataTable table) {

        for (List<String> row: table.asLists()) {
            String cookie = row.get(0);
            String condition = row.get(1);
            String result = row.get(2);

            this.getCommonSpec().getLogger().debug("Checking if cookie '{}' is '{}' to/than {}", cookie, condition, result);
            String cookieValue = commonspec.getRestResponse().getCookies().get(cookie);
            commonspec.evaluateJSONElementOperation(cookieValue, condition, result, cookie);
        }

    }

    /**
     * Saves the header value for future use
     * <pre>{@code
     * Example:
     *
     * Scenario: Save Content-Type header from response in variable
     *      Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *      When I send a 'GET' request to '/users'
     *      And I save the response header 'Content-Type' in environment variable 'content-type'
     *      Then '${content-type}' matches 'application/json; charset=utf-8'
     * }</pre>
     * @see #sendRequestDataTable(String, String, String, String, String, String, DataTable)
     * @see #sendRequestNoDataTable(String, String, String, String, String, String)
     * @param headerName    Header name
     * @param varName       Name of the environmental variable
     */
    @And("^I save the response header '(.*)' in environment variable '(.*)'$")
    public void saveHeaderValue(String headerName, String varName) {

        String headerValue = commonspec.getRestResponse().getHeaders().getValue(headerName);
        Assertions.assertThat(headerValue).as("The header " + headerName + " is not present in the response").isNotNull();
        this.getCommonSpec().getLogger().debug("Saving '{}' in variable '{}'", headerValue, varName);
        ThreadProperty.set(varName, headerValue);
    }

    /**
     * Saves the cookie value for future use
     * <p>
     * Similar to {@link #saveHeaderValue(String, String)}
     * @param cookieName  Cookie name
     * @param varName     Name of the environmental variable
     * @throws Throwable  Throwable
     */
    @And("^I save the response cookie '(.*)' in environment variable '(.*)'$")
    public void saveCookieValue(String cookieName, String varName) throws Throwable {

        String cookieValue = commonspec.getRestResponse().getCookies().get(cookieName);
        Assertions.assertThat(cookieValue).as("The cookie " + cookieName + " is not present in the response").isNotNull();
        this.getCommonSpec().getLogger().debug("Saving '{}' in variable '{}'", cookieValue, varName);
        ThreadProperty.set(varName, cookieValue);
    }

    /**
     * Specify a custom map of url query parameters to be added to future request
     * <pre>{@code
     * Example:
     *
     * Scenario: Add ?userId=3 to the url query parameters
     *      Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *      Given I set url parameters:
     *           | userId | 3 |
     *      When I send a 'GET' request to '/posts'
     * }</pre>
     * @see #setupApp(String, String)
     * @see #sendRequestDataTable(String, String, String, String, String, String, DataTable)
     * @see #sendRequestNoDataTable(String, String, String, String, String, String)
     * @param modifications DataTable containing the custom set of url query parameters to be
     *                      added to the requests. Syntax will be:
     *                      {@code
     *                      | <key> | <value> |
     *                      }
     *                      where:
     *                      key: parameters name
     *                      value: parameters value
     *                      for example:
     *                      if we want to add the parameter "id" with value "1", to the request url
     *                      the modification will be:
     *
     *                      Given I set url parameters
     *                          |  id  |  1  |
     *                      When I send a 'GET' request to '/posts'
     *
     *                      This will produce the request '/posts?id=1'
     */
    @Given("^I set url parameters:$")
    public void iSetUrlQueryParameters(DataTable modifications) {

        Map<String, String> queryParams = new HashMap<>();

        for (List<String> row: modifications.asLists()) {
            String key = row.get(0);
            String value = row.get(1);
            queryParams.put(key, value);
            this.getCommonSpec().getLogger().debug("Setting url parameter '{}' to '{}'", key, value);
            commonspec.getRestRequest().queryParam(key, value);
        }
    }

    /**
     * Adds the specified file to the request as a form-params parameter
     * (the request contentType must be changed to 'multipart/form-data')
     * @param filePath      file path
     * @throws URISyntaxException    URISyntaxException
     */
    @And("^I add the file in '(.*)' to the request$")
    public void iAddTheFileToTheRequest(String filePath) throws URISyntaxException {

        URL url = getClass().getClassLoader().getResource(filePath);
        File file = new File(url.toURI());

        this.getCommonSpec().getRestRequest().multiPart(file);
    }

    /**
     * Sets a proxy for the rest client
     * <p>
     * The given URL must have a hostname, port and scheme (a correctly formed URL), for example
     * "http://localhost:8080". If you need to use credentials for connecting to the proxy, you can
     * use {@link #setRestProxyWithCredentials(String, String, String)}
     * <pre>{@code
     * Example:
     *
     * Scenario Setting a proxy
     *      Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *      Given I set the proxy to 'http://localhost:80'
     * }
     * </pre>
     *
     * @see #setRestProxyWithCredentials(String, String, String)
     * @param address                   Fully formed URL (schema + address + port)
     * @throws MalformedURLException    MalformedURLException
     */
    @Given("I set the proxy to {string}")
    public void setRestProxy(String address) throws MalformedURLException {
        this.getCommonSpec().getLogger().debug("Setting proxy {} with username=null and password=null", address);
        this.setRestProxyWithCredentials(address, null, null);
    }

    /**
     * Sets a proxy for the rest client with credentials
     * <p>
     * The given URL must have a hostname, port and scheme (a correctly formed URL), for example
     * "http://localhost:8080".
     * <pre>{@code
     * Example:
     *
     * Scenario: Setting a proxy with credentials
     *      Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *      Given I set the proxy to 'http://localhost:80' with username 'myusername' and password 'mypassword'
     * }
     * </pre>
     *
     * @see #setRestProxy(String)
     * @param address                   Fully formed URL (schema + address + port)
     * @param username                  Username
     * @param password                  Password
     * @throws MalformedURLException    MalformedURLException
     */
    @Given("I set the proxy to {string} with username {string} and password {string}")
    public void setRestProxyWithCredentials(String address, String username, String password) throws MalformedURLException {

        ProxySpecification ps;
        int port = 80;
        URL url = new URL(address);

        if (url.getPort() != -1) {
            port = url.getPort();
        }

        if (username == null && password == null) {
            ps = new ProxySpecification(url.getHost(), port, url.getProtocol());
        } else {
            ps = new ProxySpecification(url.getHost(), port, url.getProtocol()).withAuth(username, password);
        }

        this.getCommonSpec().getLogger().debug("Setting proxy {} with username={} and password={}", address, username, password);
        this.commonspec.getRestRequest().given().proxy(ps);

    }

    /**
     * Generates a REST request of the type specified to the indicated endpoint
     * <p>
     * This step works in the same way as {@link #sendRequestDataTable(String, String, String, String, String, String, DataTable)} or to
     * {@link #sendRequestNoDataTable(String, String, String, String, String, String)}, but in this case, you can pass directly the body to
     * send as parameter. This could be useful if you want to give visibility of the data you are sending, although, if the body
     * you want to send is too large, it might be better to store it in a file and use any of the other two steps.
     * <pre>{@code
     * Example:
     *
     * Scenario: Add the body to be sent directly
     *     Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *     When I send a 'POST' request to '/posts' with body
     *     """
     *       {
     *         "userId": 1,
     *         "title": "This is a test",
     *         "body": "This is a test"
     *       }
     *     """
     *     Then the service response status must be '201'
     *
     * Scenario: Add the graphql body to be sent directly
     *     Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *     When I send a 'POST' request to '/' as 'graphql' with body
     *     """
     *         query {
     *             allUsers(perPage: 10) {
     *                 id
     *                 name
     *             }
     *         }
     *     """
     *     Then the service response status must be '201'
     *
     * Scenario: Add the graphql body and variables to be sent directly
     *     Given I securely send requests to 'jsonplaceholder.typicode.com:443'
     *     When I send a 'POST' request to '/' as 'graphql' with variables '{"perPage": 10}' and body
     *     """
     *         query ($perPage: Int = 1) {
     *             allUsers(perPage: $perPage) {
     *                 id
     *                 name
     *             }
     *         }
     *     """
     *     Then the service response status must be '201'
     * }
     * </pre>
     *
     * @see #sendRequestNoDataTable(String, String, String, String, String, String)
     * @see #sendRequestDataTable(String, String, String, String, String, String, DataTable)
     * @param requestType   HTTP verb (type of request): POST, GET, PUT, PATCH, DELETE
     * @param endPoint      end point to be used
     * @param type          If the content as string or json or graphql
     * @param variables     If the content as graphql
     * @param body          Inline body
     */
    @When("^I send a '(.+?)' request to '(.+?)'( as '(json|string|graphql)')? with( variables '(.+?)' and)? body")
    public void sendRequestInlineBody(String requestType, String endPoint, String type, String variables, DocString body) {
        String content;

        if (type == null) {
            type = "string";
        }

        switch (type) {
            case "json":
                content = JsonValue.readHjson(body.getContent()).asObject().toString();
                break;

            case "graphql":
                content = this.commonspec.buildGraphql(body.getContent(), variables);
                break;

            default:
                content = body.getContent();
                break;
        }

        commonspec.getRestRequest().given().body(content);
        commonspec.generateRestRequest(requestType, endPoint);
        commonspec.getLogger().debug("Saving response");
    }

    /**
     * Every time a request is sent, a new request object is initialized with the same base url and port that
     * was configured in {@link #setupApp(String, String)}. This is because, if the user did previously set
     * headers cookies or url parameters, this data will be added to all feature requests if the same request
     * object is reused
     */
    private void initializeRestClient() {

        commonspec.getLogger().debug("Re-initializing rest-client. Removing headers, cookies and url parameters");

        RequestSpecification spec = new RequestSpecBuilder().setContentType(ContentType.JSON).setRelaxedHTTPSValidation().build();
        commonspec.setRestRequest(given().header("Content-Type", "application/json").spec(spec));

        String baseUrl;
        if (commonspec.getRestPort() != null) {
            baseUrl = commonspec.getRestHost() + ":" + commonspec.getRestPort();
        } else {
            baseUrl = commonspec.getRestHost();
        }

        if (commonspec.getRestProtocol().matches("https://")) {
            this.setupApp("https://", baseUrl);
        } else {
            this.setupApp(null, baseUrl);
        }
    }

    /**
     * Verify that service response time
     * <p>
     * This step verifies that the response time of the last request is lower than the given number. The number provided
     * is in milliseconds
     * <pre>{@code
     * Example:
     *
     *  Scenario: Measuring Response Time
     *     Given I send requests to 'localhost:3000'
     *     When I send a 'GET' request to '/posts'
     *     And the service response time is lower than '500' milliseconds
     * }
     * </pre>
     *
     * @see #assertResponseStatusCode(Integer)
     * @see #assertResponseLength(Integer)
     * @see #assertResponseMessage(String)
     * @param responseTime   Response time value in milliseconds
     */
    @And("the service response time is lower than '{long}' milliseconds")
    public void assertResponseTime(long responseTime) {
        commonspec.getLogger().debug("Getting response time of last request in milliseconds");
        long timeInMs =  this.getCommonSpec().getRestResponse().timeIn(TimeUnit.MILLISECONDS);
        commonspec.getLogger().debug("Response time of last request was {} milliseconds", timeInMs);
        Assertions.assertThat(timeInMs).as("The service response time was higher than the expected '%s' milliseconds", responseTime).isLessThan(responseTime);
    }
}
