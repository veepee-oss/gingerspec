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

import com.jayway.jsonpath.PathNotFoundException;
import com.ning.http.client.cookie.Cookie;
import com.privalia.qa.utils.RemoteSSHConnection;
import com.privalia.qa.utils.ThreadProperty;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.privalia.qa.assertions.Assertions.assertThat;

/**
 * Generic Given Specs.
 * @see <a href="GivenGSpec-annotations.html">Given Steps &amp; Matching Regex</a>
 */
public class GivenGSpec extends BaseGSpec {


    /**
     * Generic constructor.
     *
     * @param spec object
     */
    public GivenGSpec(CommonG spec) {
        this.commonspec = spec;

    }

    /**
     * Save value for future use.
     * <p>
     * If element is a jsonpath expression (i.e. $.fragments[0].id), it will be
     * applied over the last httpResponse.
     * <p>
     * If element is a jsonpath expression preceded by some other string
     * (i.e. ["a","b",,"c"].$.[0]), it will be applied over this string.
     * This will help to save the result of a jsonpath expression evaluated over
     * previous stored variable.
     *
     * @param position position from a search result
     * @param element  key in the json response to be saved
     * @param envVar   thread environment variable where to store the value
     * @throws IllegalAccessException exception
     * @throws IllegalArgumentException exception
     * @throws SecurityException exception
     * @throws NoSuchFieldException exception
     * @throws ClassNotFoundException exception
     * @throws InstantiationException exception
     * @throws InvocationTargetException exception
     * @throws NoSuchMethodException exception
     */
    @Given("^I save element (in position \'(.+?)\' in )?\'(.+?)\' in environment variable \'(.+?)\'.$")
    public void saveElementEnvironment(String foo, String position, String element, String envVar) throws Exception {

        Pattern pattern = Pattern.compile("^((.*)(\\.)+)(\\$.*)$");
        Matcher matcher = pattern.matcher(element);
        String json;
        String parsedElement;

        if (matcher.find()) {
            json = matcher.group(2);
            parsedElement = matcher.group(4);
        } else {
            json = commonspec.getResponse().getResponse();
            parsedElement = element;
        }

        String value = "";
        try {
            value = commonspec.getJSONPathString(json, parsedElement, position);
        } catch (PathNotFoundException pe) {
            commonspec.getLogger().error(pe.getLocalizedMessage());
        }

        assertThat(value).as("json result is empty").isNotEqualTo("");
        ThreadProperty.set(envVar, value);

        ThreadProperty.set(envVar, value);
    }

    /**
     * Specify a custom map of headers to be added to future requests
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
     * @throws Exception
     */
    @Given("^I set headers:.$")
    public void setHeaders(DataTable modifications) throws Throwable {

        LinkedHashMap jsonAsMap = new LinkedHashMap();
        for (int i = 0; i < modifications.raw().size(); i++) {
            String key = modifications.raw().get(i).get(0);
            String value = modifications.raw().get(i).get(1);
            commonspec.getHeaders().put(key, value);
        }

    }

    /**
     * Save value for future use.
     *
     * @param value  value to be saved
     * @param envVar thread environment variable where to store the value
     */
    @Given("^I save \'(.+?)\' in variable \'(.+?)\'$")
    public void saveInEnvironment(String value, String envVar) {
        ThreadProperty.set(envVar, value);
    }

    /**
     * Browse to {@code url} using the current browser.
     *
     * @param path path of running app
     * @throws Exception exception
     */
    @Given("^I( securely)? browse to '(.+?)'$")
    public void seleniumBrowse(String isSecured, String path) throws Exception {
        assertThat(path).isNotEmpty();

        if (commonspec.getWebHost() == null) {
            throw new Exception("Web host has not been set");
        }

        if (commonspec.getWebPort() == null) {
            throw new Exception("Web port has not been set");
        }
        String protocol = "http://";
        if (isSecured != null) {
            protocol = "https://";
        }

        String webURL = protocol + commonspec.getWebHost() + commonspec.getWebPort();

        commonspec.getDriver().get(webURL + path);
        commonspec.setParentWindow(commonspec.getDriver().getWindowHandle());
    }

    /**
     * Set app host and port {@code host, @code port}
     *
     * @param host host where app is running
     * @param port port where app is running
     */
    @Given("^My app is running in '([^:]+?)(:.+?)?'$")
    public void setupApp(String host, String port) {
        assertThat(host).isNotEmpty();

        String restProtocol = "http://";

        if (port == null) {
            port = ":80";
        }

        if ("443".equals(port.substring(1))) {
            restProtocol = "https://";
        }

        commonspec.setWebHost(host);
        commonspec.setWebPort(port);
        commonspec.setRestProtocol(restProtocol);
        commonspec.setRestHost(host);
        commonspec.setRestPort(port);
    }

    /**
     * Send requests to {@code restHost @code restPort}.
     *
     * @param restHost host where api is running
     * @param restPort port where api is running
     */
    @Given("^I( securely)? send requests to '([^:]+?)(:.+?)?'.$")
    public void setupRestClient(String isSecured, String restHost, String restPort) {
        String restProtocol = "http://";

        if (isSecured != null) {
            restProtocol = "https://";
        }


        if (restHost == null) {
            restHost = "localhost";
        }

        if (restPort == null) {
            if (isSecured == null)  {
                restPort = ":80";
            } else {
                restPort = ":443";
            }
        }

        commonspec.setRestProtocol(restProtocol);
        commonspec.setRestHost(restHost);
        commonspec.setRestPort(restPort);
    }

    /**
     * Maximizes current browser window. Mind the current resolution could break a test.
     */
    @Given("^I maximize the browser$")
    public void seleniumMaximize() {
        commonspec.getDriver().manage().window().maximize();
    }

    /**
     * Switches to a frame/ iframe.
     */
    @Given("^I switch to the iframe on index '(\\d+?)'$")
    public void seleniumSwitchFrame(Integer index) {

        assertThat(commonspec.getPreviousWebElements()).as("There are less found elements than required")
                .hasAtLeast(index);

        WebElement elem = commonspec.getPreviousWebElements().getPreviousWebElements().get(index);
        commonspec.getDriver().switchTo().frame(elem);
    }

    /**
     * Swith to the iFrame where id matches idframe
     *
     * @param idframe iframe to swith to
     * @throws IllegalAccessException exception
     * @throws NoSuchFieldException exception
     * @throws ClassNotFoundException exception
     */
    @Given("^I switch to iframe with '([^:]*?):(.+?)'$")
    public void seleniumIdFrame(String method, String idframe) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        assertThat(commonspec.locateElement(method, idframe, 1));

        if (method.equals("id") || method.equals("name")) {
            commonspec.getDriver().switchTo().frame(idframe);
        } else {
            throw new ClassNotFoundException("Can not use this method to switch iframe");
        }
    }

    /**
     * Switches to a parent frame/ iframe.
     */
    @Given("^I switch to a parent frame$")
    public void seleniumSwitchAParentFrame() {
        commonspec.getDriver().switchTo().parentFrame();
    }

    /**
     * Switches to the frames main container.
     */
    @Given("^I switch to the main frame container$")
    public void seleniumSwitchParentFrame() {
        commonspec.getDriver().switchTo().frame(commonspec.getParentWindow());
    }

    /**
     * Opens a ssh connection to remote host
     *
     * @param remoteHost remote host
     * @param user remote user
     * @param password (required if pemFile null)
     * @param pemFile (required if password null)
     * @throws Exception exception
     *
     */
    @Given("^I open a ssh connection to '(.+?)' with user '(.+?)'( and password '(.+?)')?( using pem file '(.+?)')?$")
    public void openSSHConnection(String remoteHost, String user, String foo, String password, String bar, String pemFile) throws Exception {
        if ((pemFile == null) || (pemFile.equals("none"))) {
            if (password == null) {
                throw new Exception("You have to provide a password or a pem file to be used for connection");
            }
            commonspec.setRemoteSSHConnection(new RemoteSSHConnection(user, password, remoteHost, null));
            commonspec.getLogger().debug("Opening ssh connection with password: { " + password + "}", commonspec.getRemoteSSHConnection());
        } else {
            File pem = new File(pemFile);
            if (!pem.exists()) {
                throw new Exception("Pem file: " + pemFile + " does not exist");
            }
            commonspec.setRemoteSSHConnection(new RemoteSSHConnection(user, null, remoteHost, pemFile));
            commonspec.getLogger().debug("Opening ssh connection with pemFile: {}", commonspec.getRemoteSSHConnection());
        }
    }

    public List<Cookie> addSsoToken(HashMap<String, String> ssoCookies, String[] tokenList) {
        List<Cookie> cookiesAttributes = new ArrayList<>();

        for (String tokenKey : tokenList) {
            cookiesAttributes.add(new Cookie(tokenKey, ssoCookies.get(tokenKey),
                    false, null,
                    null, 999999, false, false));
        }
        return cookiesAttributes;
    }

    /*
     * Copies file/s from remote system into local system
     *
     * @param remotePath path where file is going to be copy
     * @param localPath path where file is located
     * @throws Exception exception
     *
     */
    @Given("^I inbound copy '(.+?)' through a ssh connection to '(.+?)'$")
    public void copyFromRemoteFile(String remotePath, String localPath) throws Exception {
        commonspec.getRemoteSSHConnection().copyFrom(remotePath, localPath);
    }

    /**
     * Copies file/s from local system to remote system
     *
     * @param remotePath path where file is going to be copy
     * @param localPath path where file is located
     * @throws Exception exception
     */
    @Given("^I outbound copy '(.+?)' through a ssh connection to '(.+?)'$")
    public void copyToRemoteFile(String localPath, String remotePath) throws Exception {
        commonspec.getRemoteSSHConnection().copyTo(localPath, remotePath);
    }

    /**
     * Executes the command specified in local system
     *
     * @param command command to be run locally
     * @param foo regex needed to match method
     * @param exitStatus command exit status
     * @param bar regex needed to match method
     * @param envVar environment variable name
     * @throws Exception exception
     **/
    @Given("^I run '(.+?)' locally( with exit status '(.+?)')?( and save the value in environment variable '(.+?)')?$")
    public void executeLocalCommand(String command, String foo, Integer exitStatus, String bar, String envVar) throws Exception {
        if (exitStatus == null) {
            exitStatus = 0;
        }

        commonspec.runLocalCommand(command);
        commonspec.runCommandLoggerAndEnvVar(exitStatus, envVar, Boolean.TRUE);

        assertThat(commonspec.getCommandExitStatus()).isEqualTo(exitStatus);
    }

    /**
     * Executes the command specified in remote system
     *
     * @param command command to be run locally
     * @param foo regex needed to match method
     * @param exitStatus command exit status
     * @param bar regex needed to match method
     * @param envVar environment variable name
     * @throws Exception exception
     **/
    @Given("^I run '(.+?)' in the ssh connection( with exit status '(.+?)')?( and save the value in environment variable '(.+?)')?$")
    public void executeCommand(String command, String foo, Integer exitStatus, String bar, String envVar) throws Exception {
        if (exitStatus == null) {
            exitStatus = 0;
        }

        command = "set -o pipefail && " + command + " | grep . --color=never; exit $PIPESTATUS";
        commonspec.getRemoteSSHConnection().runCommand(command);
        commonspec.setCommandResult(commonspec.getRemoteSSHConnection().getResult());
        commonspec.setCommandExitStatus(commonspec.getRemoteSSHConnection().getExitStatus());
        commonspec.runCommandLoggerAndEnvVar(exitStatus, envVar, Boolean.FALSE);

        assertThat(commonspec.getRemoteSSHConnection().getExitStatus()).isEqualTo(exitStatus);
    }

    /**
     * Get all opened windows and store it.
     */
    @Given("^a new window is opened$")
    public void seleniumGetwindows() {
        Set<String> wel = commonspec.getDriver().getWindowHandles();

        assertThat(wel).as("Element count doesnt match").hasSize(2);
    }
}
