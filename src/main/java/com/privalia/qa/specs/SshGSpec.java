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

import com.privalia.qa.utils.RemoteSSHConnection;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
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
     * <p>
     * Connects to the remote server by ssh with the given username and password. After the connection is open,
     * you can execute commands on the remote server or send and receive commands.
     *
     * <pre>
     * Example: Connecting to a remote server
     * {@code
     *      Given I open a ssh connection to '10.200.56.59' with user 'myuser' and password 'temporal'
     *      When I run 'ls -l' in the ssh connection and save the value in environment variable 'RESULT'
     *      Then '${RESULT}' contains 'total'
     * }
     * </pre>
     *
     * @see #executeCommand(String, String, Integer, String, String)
     * @see UtilsGSpec#checkValue(String, String, String)
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
     * <p>
     * Copies a file located in a remote server to a local destination via scp (secure copy) command in Linux.
     * Before using this step, you must first open a ssh connection with {@link #openSSHConnection(String, String, String, String, String, String)}
     *
     * <pre>
     * Example:
     * {@code
     *      Given I open a ssh connection to '10.200.56.59' with user 'myuser' and password 'temporal'
     *      Then I inbound copy '/tmp/exampleJSON.conf' through a ssh connection to 'fileFromSsh.conf'
     * }
     * </pre>
     *
     * @see #openSSHConnection(String, String, String, String, String, String)
     * @see #copyToRemoteFile(String, String)
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
     * <p>
     * Copies a file located in the local machine to a remote server via scp (secure copy) command in Linux.
     * Before using this step, you must first open a ssh connection with {@link #openSSHConnection(String, String, String, String, String, String)}
     *
     * <pre>
     * Example:
     * {@code
     *      Given I open a ssh connection to '10.200.56.59' with user 'myuser' and password 'temporal'
     *      Then I outbound copy 'exampleJSON.conf' through a ssh connection to '/tmp/exampleJSON.conf'
     * }
     * </pre>
     *
     * @see #openSSHConnection(String, String, String, String, String, String)
     * @see #copyFromRemoteFile(String, String)
     * @param localPath  path where file is located
     * @param remotePath path where file is going to be copy
     * @throws Exception exception
     */
    @Given("^I outbound copy '(.+?)' through a ssh connection to '(.+?)'$")
    public void copyToRemoteFile(String localPath, String remotePath) throws Exception {
        commonspec.getRemoteSSHConnection().copyTo(localPath, remotePath);
    }


    /**
     * Executes the command specified in the local system
     * <p>
     * Executes the given command in the local system. Unless specified, this step expects the returned exit status
     * of the executed command to be zero (more info <a href="https://www.gnu.org/software/bash/manual/html_node/Exit-Status.html">here</a>),
     * however, you can also specify any exit code you like. This step also provides the possibility of saving the returned
     * response into a variable and use it in the following steps
     *
     * <pre>
     * Example: Run a command locally
     * {@code
     *      Given I run 'ls /tmp | wc -l' locally
     * }
     * Example: Run a command locally and expect exit status to be 127
     * {@code
     *      Then I run 'lss /tmp' in the ssh connection with exit status '127'
     * }
     * Example: Run a command and save its value in variable for further inspection
     * {@code
     *      Given I run 'ls /tmp | wc -l' locally and save the value in environment variable 'WORDCOUNT'
     *      Then '${WORDCOUNT}' is '14'
     * }
     * </pre>
     * @see UtilsGSpec#checkValue(String, String, String)
     * @see <a href="https://www.gnu.org/software/bash/manual/html_node/Exit-Status.html">Exit status</a>
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
     * <p>
     * Executes the given command in the ssh connection. For this step to work, you must first connect to a remote server
     * by ssh using the step {@link #openSSHConnection(String, String, String, String, String, String)}. Unless specified,
     * this step expects the returned exit status of the executed command to be zero.
     * (more info <a href="https://www.gnu.org/software/bash/manual/html_node/Exit-Status.html">here</a>), however, you can
     * also specify any exit code you like. This step also provides the possibility of saving the returned response into a
     * variable and use it in the following steps
     *
     * <pre>
     * Example: Run a command locally
     * {@code
     *      Given I open a ssh connection to '10.200.56.59' with user 'myuser' and password 'temporal'
     *      Then I run 'ls /tmp' in the ssh connection
     * }
     * Example: Run a command locally and expect exit status to be 127
     * {@code
     *      Given I open a ssh connection to '10.200.56.59' with user 'myuser' and password 'temporal'
     *      When I run 'lss /tmp' in the ssh connection with exit status '127'
     * }
     * Example: Run a command and save its value in variable for further inspection
     * {@code
     *      Given I open a ssh connection to '10.200.56.59' with user 'myuser' and password 'temporal'
     *      When I run 'ls -la /tmp' in the ssh connection and save the value in environment variable 'DEFEXSTAT'
     *      Then '${DEFEXSTAT}' contains 'total'
     * }
     * </pre>
     * @see #openSSHConnection(String, String, String, String, String, String)
     * @see UtilsGSpec#checkValue(String, String, String)
     * @see <a href="https://www.gnu.org/software/bash/manual/html_node/Exit-Status.html">Exit status</a>
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
     * Validates command output with timeout
     * <p>
     * This step executes a shell command periodically on a remote ssh connection and evaluates the command output for a
     * given string. The control flow of the feature continue if the string is found before the maximun amount of time
     * (in seconds), otherwise, the feature fails. An ssh connection needs to be established for this step to work
     * <p>
     * For example, to check every 2 seconds, for a maximum of 20 seconds if the file "text.txt" exists in the remote server
     *
     * <pre>
     * {@code
     *      Given I open a ssh connection to 'my-remote-server-address' with user 'root' and password '1234'
     *      Then in less than '20' seconds, checking each '2' seconds, the command output 'ls' contains 'test.txt'
     * }
     * </pre>
     *
     * @see #openSSHConnection(String, String, String, String, String, String)
     * @param timeout the max time to wait
     * @param wait    checking interval
     * @param command the command to execute
     * @param search  text to search on the command output
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
     * <p>
     * Verifies that the result of a previously executed command contains the given text.
     * <pre>
     * Example: Check if the result of the command contains the string '2'
     * {@code
     *      Given I run 'wc -l testOutput.txt' locally
     *      Then the command output contains '2'
     * }
     * </pre>
     * @see #executeLocalCommand(String, Integer, String)
     * @see #notFindShellOutput(String)
     * @param search        Text to search
     * @throws Exception    Exception
     **/
    @Then("^the command output contains '(.+?)'$")
    public void findShellOutput(String search) throws Exception {
        assertThat(commonspec.getCommandResult()).as("Command output does not contain expected value").contains(search);
    }

    /**
     * Check the non existence of a text at a command output
     * <p>
     * Verifies that the result of a previously executed command does not contains the given text.
     * <pre>
     * Example: Check if the result of the command does not contains the string '2'
     * {@code
     *      Given I run 'wc -l testOutput.txt' locally
     *      Then the command output does not contains '2'
     * }
     * </pre>
     * @see #executeLocalCommand(String, Integer, String)
     * @see #findShellOutput(String)
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
