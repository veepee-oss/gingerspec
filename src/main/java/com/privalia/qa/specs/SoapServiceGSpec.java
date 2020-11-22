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

import com.privalia.qa.assertions.Assertions;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps definitions for working with SOAP web services
 *
 * @author Jose Fernandez
 */
public class SoapServiceGSpec extends BaseGSpec {

    public SoapServiceGSpec(CommonG spec) {
        this.commonspec = spec;
    }

    /**
     * Set URL of remote WSDL
     * <p>
     * This step parses the remote WSDL (web service description language) webservice descriptor and
     * obtains the basic parameters to execute future requests (like targetNamespace, Service name, methods
     * available, etc). This is a initialization steps and must be used before trying to execute any remote method
     *
     * <pre>
     * Example: consider the webservice running at http://www.dneonline.com/calculator.asmx, with a WSDL located
     * at http://www.dneonline.com/calculator.asmx?WSDL. The first step before executing any other webservice related step should be:
     * {@code
     *      Given The webservice WSDL is located in 'http://www.dneonline.com/calculator.asmx?WSDL'
     * }
     * </pre>
     *
     * @param remoteWsdlAddress Remote URL of the WSDL document
     * @throws Throwable        Throwable
     */
    @Given("^The webservice WSDL is located in '(.+?)'$")
    public void setRemoteWSDL(String remoteWsdlAddress) throws Throwable {

        this.commonspec.getSoapServiceClient().parseWsdl(remoteWsdlAddress);
        assertThat(this.commonspec.getSoapServiceClient().getTargetNameSpace()).as("Could not find TargetNamespace in WSDL").isNotEmpty();
        assertThat(this.commonspec.getSoapServiceClient().getServiceName()).as("Could not find Service in WSDL").isNotEmpty();
        assertThat(this.commonspec.getSoapServiceClient().getPortName()).as("Could not find Port in WSDL").isNotEmpty();

    }

    /**
     * Executes remote webservice method with parameters
     * <p>
     * Executes the remote method in the webservice. A request body must be provided for this operation. There is also the possibility
     * of altering values of the request body before sending providing a datatable. Since the library is capable of using any webservice,
     * it does not know in advance the format of the XML messages that are interchanged between the server and the clients. This is why
     * is necessary to provide the XML request that should be sent when executing a particular method.
     * <p>
     * Following the example of the webservice located at http://www.dneonline.com/calculator.asmx, the webservice exposes
     * 4 methods: Add, Divide, Multiply and Subtract. To execute the "Add" method, the following XML message should be sent to the server:
     * <pre>
     * {@code
     * <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
     *    <soap:Body>
     *       <Add xmlns="http://tempuri.org/">
     *          <intA>int</intA>
     *          <intB>int</intB>
     *       </Add>
     *    </soap:Body>
     * </soap:Envelope>
     * }
     * </pre>
     * Notice how the XML format of the request contains the variables intA and intB. The server will read this variables from the request,
     * perform an "Add" and return a result in XML format. The information regarding the format of the XML requests and responses is usually
     * well documented and readily available for consumers. If this is not the case, you should contact the webservice developers for this information
     * <p>
     * This request XML message format should be stored in a separate file (for this example, lets save it to schemas/AddRequest.xml)
     * To execute the "Add" method with intA = 1 and intB = 1:
     * <pre>
     * {@code
     *      Given The webservice WSDL is located in 'http://www.dneonline.com/calculator.asmx?WSDL'
     *      When I execute the method 'Add' based on 'schemas/AddRequest.xml' with:
     *          | intA | 1 |
     *          | intB | 1 |
     * }
     * </pre>
     * You can provide a datatable containing a set of modifications to be performed on the XML request before it is sent to the server
     * (this is necessary in this particular case). If this is not required, you can use the same step without the datatable:
     * <pre>
     * {@code
     *      When I execute the method 'Add' based on 'schemas/AddRequest.xml'
     * }
     * </pre>
     *
     * @see #setRemoteWSDL(String)
     * @param actionName        Remote method to execute. The method name will be translated to the corresponding SOAPAction
     * @param requestFile       File containing the request (XML)
     * @param foo               parameter generated by cucumber because of the optional expression
     * @param modifications     (Optional) a datatable with modifications to perform on the request before sending
     *                          Example:
     *                          Assuming the request XML body contains the following tags:
     *                          {@code      <intA>int<intA/>
     *                                      <intB>int<intB/> }
     *                          To alter the value of intA and intB, a datatable like the following should be provided:
     *                          {@code      | intA  | 1 |
     *                                      | intB  | 1 | }
     *                          And the corresponding values for the given tags in the body will be
     *                          {@code      <intA>1<intA/>
     *                                      <intB>1<intB/> }
     * @throws Throwable        Throwable
     */
    @When("^I execute the method '(.+?)' based on '([^:]+?)'( with:)?$")
    public void executeWebserviceMethod(String actionName, String requestFile, String foo, DataTable modifications) throws Throwable {

        assertThat(this.commonspec.getSoapServiceClient().getTargetNameSpace()).as("No TargetNamespace found!, you may need specify WSDL in a prior step").isNotEmpty();

        // Retrieve data
        String request = commonspec.retrieveData(requestFile, "string");

        String response = "";
        if (modifications != null) {
            response = this.commonspec.getSoapServiceClient().executeMethodWithParams(actionName, request, modifications.asMap(String.class, String.class));
        } else {
            response = this.commonspec.getSoapServiceClient().executeMethod(actionName, request);
        }

        assertThat(response).as("Last request did not return a response").isNotEmpty();
        this.commonspec.setLastSoapResponse(response);

    }

    /**
     * Verifies response of webservice.
     * <p>
     * Evaluates the response of the remote webservice against a set of conditions provided by a datatable. Following
     * the previous example, the XML response for the "Add" have the format below:
     * <pre>
     * {@code
     * <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
     *    <soap:Body>
     *       <AddResponse xmlns="http://tempuri.org/">
     *          <AddResult>int</AddResult>
     *       </AddResponse>
     *    </soap:Body>
     * </soap:Envelope>
     * }
     * </pre>
     * The result of the "Add" operation will be present in the AddResult tag in the XML response. Performing an "Add"
     * method with intA=1 and intB=1, we could expect the AddResult to be 2
     * <pre>
     * {@code
     *      Scenario: Execute "Add" operation
     *          Given The webservice WSDL is located in 'http://www.dneonline.com/calculator.asmx?WSDL'
     *          When I execute the method 'Add' based on 'schemas/AddRequest.xml' with:
     *              | intA | 1 |
     *              | intB | 1 |
     *          Then The response matches the following cases:
     *              | AddResult | equal | 2 |
     * }
     * </pre>
     * It is not necessary to store in a separate file the format of the response as it was done with the request, but is
     * important to know the structure of the XML beforehand so we could know in advance what XML tags to evaluate
     *
     * @see #setRemoteWSDL(String)
     * @see #executeWebserviceMethod(String, String, String, DataTable)
     * @param results       Expected results in a datatable
     *                      Example:
     *                      Assuming the response from the webservice (XML response), contains
     *                      the following XML tag:
     *                      {@code <AddResult>2<AddResult/>}
     *                      To verify that AddResult tag equals "2", a datatable like the following
     *                      should be provided:
     *                      | AddResult  | equal | 2 |
     *                      Suported operations:
     *                      - equal
     *                      - not equal
     *                      - contains
     *                      - does not contain
     *                      - length
     * @throws Throwable    Throwable
     */
    @Then("^The response matches the following cases:$")
    public void evaluateWebserviceResponse(DataTable results) throws Throwable {

        String response = this.commonspec.getLastSoapResponse();

        for (List<String> row : results.asLists()) {
            String variable = row.get(0);
            String condition = row.get(1);
            String result = row.get(2);

            switch (condition) {

                case "equal":
                    assertThat(this.commonspec.getSoapServiceClient().evaluateXml(response, variable)).matches(result);
                    break;

                case "not equal":
                    assertThat(this.commonspec.getSoapServiceClient().evaluateXml(response, variable)).doesNotMatch(result);
                    break;

                case "contains":
                    assertThat(this.commonspec.getSoapServiceClient().evaluateXml(response, variable)).contains(result);
                    break;

                case "does not contain":
                    assertThat(this.commonspec.getSoapServiceClient().evaluateXml(response, variable)).doesNotContain(result);
                    break;

                case "length":
                    assertThat(this.commonspec.getSoapServiceClient().evaluateXml(response, variable).length()).isEqualTo(Integer.parseInt(result));
                    break;

                default:
                    Assertions.fail("Not implemented condition: " + condition);
            }
        }
    }
}
