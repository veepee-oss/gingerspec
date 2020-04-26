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

import com.privalia.qa.utils.RemoteSSHConnection;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.assertj.core.api.Assertions;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps definitions for running bash commands and establishing SSH connections with a remote host
 *
 * @author Jose Fernandez
 */
public class SshGSpec extends BaseGSpec {

    /**
     * Default constructor.
     *
     * @param spec CommonG object
     */
    public SshGSpec(CommonG spec) {
        this.commonspec = spec;
    }


    /**
     * Opens a ssh connection to remote host
     *
     * @param remoteHost remote host
     * @param user       remote user
     * @param foo        the foo
     * @param password   (required if pemFile null)
     * @param bar        the bar
     * @param pemFile    (required if password null)
     * @throws Exception exception
     */
    @Given("^I open a ssh connection to '(.+?)' with user '(.+?)'( and password '(.+?)')?( using pem file '(.+?)')?$")
    public void openSSHConnection(String remoteHost, String user, String foo, String password, String bar, String pemFile) throws Exception {
        if ((pemFile == null) || (pemFile.equals("none"))) {
            if (password == null) {
                Assertions.fail("You have to provide a password or a pem file to be used for connection");
            }
            commonspec.setRemoteSSHConnection(new RemoteSSHConnection(user, password, remoteHost, null));
            commonspec.getLogger().debug("Opening ssh connection with password: { " + password + "}", commonspec.getRemoteSSHConnection());
        } else {
            File pem = new File(pemFile);
            if (!pem.exists()) {
                Assertions.fail("Pem file: " + pemFile + " does not exist");
            }
            commonspec.setRemoteSSHConnection(new RemoteSSHConnection(user, null, remoteHost, pemFile));
            commonspec.getLogger().debug("Opening ssh connection with pemFile: {}", commonspec.getRemoteSSHConnection());
        }
    }


    /**
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
     * @param localPath  path where file is located
     * @param remotePath path where file is going to be copy
     * @throws Exception exception
     */
    @Given("^I outbound copy '(.+?)' through a ssh connection to '(.+?)'$")
    public void copyToRemoteFile(String localPath, String remotePath) throws Exception {
        commonspec.getRemoteSSHConnection().copyTo(localPath, remotePath);
    }


    /**
     * Executes the command specified in local system
     *
     * @param command    command to be run locally
     * @param exitStatus command exit status
     * @param envVar     environment variable name
     * @throws Exception exception
     */
    @Given("^I run '(.+?)' locally( with exit status '(.+?)')?( and save the value in environment variable '(.+?)')?$")
    public void executeLocalCommand(String command, Integer exitStatus, String envVar) throws Exception {
        if (exitStatus == null) {
            exitStatus = 0;
        }

        commonspec.runLocalCommand(command);
        commonspec.runCommandLoggerAndEnvVar(exitStatus, envVar, Boolean.TRUE);

        Assertions.assertThat(commonspec.getCommandExitStatus()).as("Command actual exit status (%s) not equal to expected (%s)", commonspec.getCommandExitStatus(), exitStatus).isEqualTo(exitStatus);
    }


    /**
     * Executes the command specified in remote system
     *
     * @param command    command to be run locally
     * @param foo        regex needed to match method
     * @param exitStatus command exit status
     * @param bar        regex needed to match method
     * @param envVar     environment variable name
     * @throws Exception exception
     */
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

        Assertions.assertThat(commonspec.getRemoteSSHConnection().getExitStatus()).isEqualTo(exitStatus);
    }


    /**
     * Checks if {@code expectedCount} element is found, whithin a {@code timeout} and with a location
     * {@code method}. Each negative lookup is followed by a wait of {@code wait} seconds. Selenium times are not
     * accounted for the mentioned timeout.
     *
     * @param timeout the max timeto wait
     * @param wait    checking interval
     * @param command the command
     * @param search  text to search for
     * @throws Exception exception
     */
    @Then("^in less than '(\\d+?)' seconds, checking each '(\\d+?)' seconds, the command output '(.+?)' contains '(.+?)'$")
    public void assertCommandExistsOnTimeOut(Integer timeout, Integer wait, String command, String search) throws Exception {
        Boolean found = false;
        AssertionError ex = null;

        for (int i = 0; (i <= timeout); i += wait) {
            if (found) {
                break;
            }
            commonspec.getLogger().debug("Checking output value");
            commonspec.getRemoteSSHConnection().runCommand(command);
            commonspec.setCommandResult(commonspec.getRemoteSSHConnection().getResult());
            try {
                assertThat(commonspec.getCommandResult()).as("Contains " + search + ".").contains(search);
                found = true;
                timeout = i;
            } catch (AssertionError e) {
                commonspec.getLogger().info("Command output don't found yet after " + i + " seconds");
                Thread.sleep(wait * 1000);
                ex = e;
            }
        }
        if (!found) {
            throw (ex);
        }
        commonspec.getLogger().info("Command output found after " + timeout + " seconds");
    }


    /**
     * Check the existence of a text at a command output
     *
     * @param search        Text to search
     * @throws Exception    Exception
     **/
    @Then("^the command output contains '(.+?)'$")
    public void findShellOutput(String search) throws Exception {
        assertThat(commonspec.getCommandResult()).as("Command output does not contain expected value").contains(search);
    }

    /**
     * Check the non existence of a text at a command output
     *
     * @param search    Text to search
     * @throws Exception    Exception
     **/
    @Then("^the command output does not contain '(.+?)'$")
    public void notFindShellOutput(String search) throws Exception {
        assertThat(commonspec.getCommandResult()).as("Command output do contain value").doesNotContain(search);
    }


    /**
     * Check the exitStatus of previous command execution matches the expected one
     *
     * @param expectedExitStatus    Expected result of the command execution
     * @deprecated Success exit status is directly checked in the "execute remote command" method, so this is not
     * needed anymore.
     * @throws Exception    Exception
     **/
    @Deprecated
    @Then("^the command exit status is '(.+?)'$")
    public void checkShellExitStatus(int expectedExitStatus) throws Exception {
        assertThat(commonspec.getCommandExitStatus()).as("The actual command exit status value (%s) is different than expected value (%s)", commonspec.getCommandExitStatus(), expectedExitStatus).isEqualTo(expectedExitStatus);
    }

}
