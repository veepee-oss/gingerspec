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

public class SoapServiceGSpec extends BaseGSpec {

    public SoapServiceGSpec(CommonG spec) {
        this.commonspec = spec;
    }

    /**
     * Indicates the library the location of the remote WSDL (descriptor of the remote webservice)
     * This is an initialization step and must be provided before executing any other steps
     * @param remoteWsdlAddress Remote URL of the WSDL document
     */
    @Given("^The webservice WSDL is located in '(.+?)'$")
    public void theWebserviceWSDLIsLocatedIn(String remoteWsdlAddress) throws Throwable {

        this.commonspec.getSoapServiceClient().parseWsdl(remoteWsdlAddress);
        assertThat(this.commonspec.getSoapServiceClient().getTargetNameSpace()).as("Could not find TargetNamespace in WSDL").isNotEmpty();
        assertThat(this.commonspec.getSoapServiceClient().getServiceName()).as("Could not find Service in WSDL").isNotEmpty();
        assertThat(this.commonspec.getSoapServiceClient().getPortName()).as("Could not find Port in WSDL").isNotEmpty();

    }

    /**
     * Executes the remote method in the webservice. A request body must be provided for this operation. There is also the possibility
     * of altering values of the request body before sending providing a datatable
     * @param actionName        Remote method to execute. The method name will be translated to the corresponding SOAPAction
     * @param requestFile       File containing the request (XML)
     * @param modifications     (Optional) a datatable with modifications to perform on the request before sending
     *                          Example:
     *                          Assuming the request XML body contains the following tags:
     *                          <intA>int<intA/>
     *                          <intB>int<intB/>
     *                          To alter the value of intA and intB, a datatable like the following should be provided:
     *                          | intA  | 1 |
     *                          | intB  | 1 |
     *                          And the corresponding values for the given tags in the body will be
     *                          <intA>1<intA/>
     *                          <intB>1<intB/>
     */
    @When("^I execute the method '(.+?)' based on '([^:]+?)'( with:)?$")
    public void iExecuteTheMethodAddBasedOnSchemasAddRequestWith(String actionName, String requestFile, String foo, DataTable modifications) throws Throwable {

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
     * Evaluates the response of the remote websevice against a set of conditions provided
     * by a datatable
     * @param results       Expected results in a datatable
     *                      Example:
     *                      Assuming the response from the webservice (XML response), contains
     *                      the following XML tag:
     *                      <AddResult>2<AddResult/>
     *                      To verify that AddResult tag equals "2", a datatable like the following
     *                      should be provided:
     *                      | AddResult  | equal | 2 |
     *                      Suported operations:
     *                      - equal
     *                      - not equal
     *                      - contains
     *                      - does not contain
     *                      - length
     */
    @Then("^The response matches the following cases:$")
    public void theResponseMatchesTheFollowingCases(DataTable results) throws Throwable {

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
