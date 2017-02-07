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
package com.stratio.qa.utils;

import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.TestListenerAdapter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Jacoco client for Server coverages.
 */
public class JaCoCoClient extends TestListenerAdapter {

    private static final String DESTFILE = "target/jacocoAT.exec";
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
