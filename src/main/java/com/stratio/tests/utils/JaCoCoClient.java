package com.stratio.tests.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.TestListenerAdapter;
/**
 * Jacoco client for Server coverages.
 * @author Hugo dominguez
 * @author Javier Delgado
 *
 */
public class JaCoCoClient extends TestListenerAdapter {

    private static final String DESTFILE = "target/executions/jacoco-client.exec";
    private static final int PORT = 6300;

    private final Logger logger = LoggerFactory.getLogger(JaCoCoClient.class);

    @Override
    public void onFinish(ITestContext context) {

        FileOutputStream localFile;
        try {
            localFile = new FileOutputStream(DESTFILE);
            final ExecutionDataWriter localWriter = new ExecutionDataWriter(localFile);

            // Open a socket to the coverage agent:
            String address = System.getProperty("JACOCO_SERVER", "localhost");
            final Socket socket = new Socket(InetAddress.getByName(address), PORT);
            final RemoteControlWriter writer = new RemoteControlWriter(socket.getOutputStream());
            final RemoteControlReader reader = new RemoteControlReader(socket.getInputStream());
            reader.setSessionInfoVisitor(localWriter);
            reader.setExecutionDataVisitor(localWriter);

            // Send a dump command and read the response:
            writer.visitDumpCommand(true, true);
            reader.read();

            socket.close();
            localFile.close();
        } catch (IOException e) {
            logger.warn("Exception on fetching remote coverage. is the remote JaCoCo agent set up?: {}", e.getMessage());
        }

    }
}
