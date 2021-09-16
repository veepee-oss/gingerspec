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

package com.privalia.qa.utils;

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
