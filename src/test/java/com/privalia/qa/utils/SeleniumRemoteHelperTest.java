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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.testng.annotations.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class SeleniumRemoteHelperTest {

    @Test(enabled = false)
    public void getAllNodes() throws JsonProcessingException {
        List<String> availableNodes = new SeleniumRemoteHelper()
                .connectToGrid("localhost:4444")
                .getAllNodesFromGrid();

        Assert.assertNotNull(availableNodes);
        assertThat(availableNodes.size()).isGreaterThan(0);
    }

    @Test(enabled = false)
    public void getAllAvailableNodes() throws JsonProcessingException {
        List<String> availableNodes = new SeleniumRemoteHelper()
                .connectToGrid("localhost:4444")
                .getAllAvailableNodesFromGrid();

        Assert.assertNotNull(availableNodes);
        assertThat(availableNodes.size()).isGreaterThan(0);
    }

    @Test
    public void testFromFile() throws IOException {
        String content = new String(Files.readAllBytes( Paths.get("src/test/resources/grid.html")));

        List<String> availableNodes = new SeleniumRemoteHelper()
                .setPageSource(content)
                .getAllAvailableNodesFromGrid();

        Assert.assertNotNull(availableNodes);
        assertThat(availableNodes.size()).isEqualTo(4);
    }

    @Test
    public void transformToJsonString() throws JsonProcessingException {
        String node = "{server:CONFIG_UUID=e1338830-5d89-460b-acee-c5b465ee4c6c, seleniumProtocol=WebDriver, acceptSslCerts=true, screen-resolution=1920x1080, tz=Europe/Berlin, browserName=firefox, maxInstances=1, platformName=LINUX, screenResolution=400x400, resolution=1920x1080, version=82.0.3, platform=LINUX}";
        String out = new SeleniumRemoteHelper().transformToJsonString(node);
        assertThat(out).isEqualTo("{\"server:CONFIG_UUID\":\"e1338830-5d89-460b-acee-c5b465ee4c6c\",\"seleniumProtocol\":\"WebDriver\",\"acceptSslCerts\":\"true\",\"screen-resolution\":\"1920x1080\",\"tz\":\"Europe/Berlin\",\"browserName\":\"firefox\",\"maxInstances\":\"1\",\"platformName\":\"LINUX\",\"screenResolution\":\"400x400\",\"resolution\":\"1920x1080\",\"version\":\"82.0.3\",\"platform\":\"LINUX\"}");
    }

    @Test
    public void getNodesByFilter() throws JsonProcessingException {
        final List<String> availableNodes = new LinkedList<>();
        availableNodes.add("{\"server:CONFIG_UUID\":\"e1338830-5d89-460b-acee-c5b465ee4c6c\",\"seleniumProtocol\":\"WebDriver\",\"acceptSslCerts\":\"true\",\"screen-resolution\":\"1920x1080\",\"tz\":\"Europe/Berlin\",\"browserName\":\"firefox\",\"maxInstances\":\"1\",\"platformName\":\"LINUX\",\"screenResolution\":\"400x400\",\"resolution\":\"1920x1080\",\"version\":\"82.0.3\",\"platform\":\"LINUX\"}");
        availableNodes.add("{\"server:CONFIG_UUID\":\"2f5d203a-8fdd-4147-b29e-4c877cb75f69\",\"seleniumProtocol\":\"WebDriver\",\"acceptSslCerts\":\"true\",\"screen-resolution\":\"1920x1080\",\"tz\":\"Europe/Berlin\",\"browserName\":\"chrome\",\"maxInstances\":\"1\",\"platformName\":\"LINUX\",\"screenResolution\":\"1920x1080\",\"resolution\":\"1920x1080\",\"version\":\"87.0.4280.66\",\"platform\":\"LINUX\"}");
        availableNodes.add("{\"server:CONFIG_UUID\":\"be713c94-8f8b-4bc6-b706-5f129a940ea8\",\"seleniumProtocol\":\"WebDriver\",\"acceptSslCerts\":\"true\",\"screen-resolution\":\"1920x1080\",\"tz\":\"Europe/Berlin\",\"browserName\":\"firefox\",\"maxInstances\":\"1\",\"platformName\":\"WINDOWS\",\"screenResolution\":\"400x400\",\"resolution\":\"1920x1080\",\"version\":\"82.0.3\",\"platform\":\"WINDOWS\"}");
        availableNodes.add("{\"server:CONFIG_UUID\":\"ce5b55af-e4e4-440e-90bc-4bfcace17d2c\",\"seleniumProtocol\":\"WebDriver\",\"acceptSslCerts\":\"true\",\"screen-resolution\":\"1920x1080\",\"tz\":\"Europe/Berlin\",\"browserName\":\"chrome\",\"maxInstances\":\"1\",\"platformName\":\"WINDOWS\",\"screenResolution\":\"1920x1080\",\"resolution\":\"1920x1080\",\"version\":\"87.0.4280.66\",\"platform\":\"WINDOWS\"}");
        availableNodes.add("{\"server:CONFIG_UUID\":\"ce5b55af-e4e4-440e-90bc-4bfcace17d2c\",\"seleniumProtocol\":\"WebDriver\",\"acceptSslCerts\":\"true\",\"screen-resolution\":\"1920x1080\",\"tz\":\"Europe/Berlin\",\"browserName\":\"chrome\",\"maxInstances\":\"1\",\"platformName\":\"Android\",\"screenResolution\":\"1920x1080\",\"resolution\":\"1920x1080\",\"version\":\"87.0.4280.66\",\"platform\":\"Android\"}");
        availableNodes.add("{\"server:CONFIG_UUID\":\"ce5b55af-e4e4-440e-90bc-4bfcace17d2c\",\"seleniumProtocol\":\"WebDriver\",\"acceptSslCerts\":\"true\",\"screen-resolution\":\"1920x1080\",\"tz\":\"Europe/Berlin\",\"browserName\":\"chrome\",\"maxInstances\":\"1\",\"platformName\":\"iOS\",\"screenResolution\":\"1920x1080\",\"resolution\":\"1920x1080\",\"version\":\"87.0.4280.66\",\"platform\":\"iOS\"}");

        Map<String, String> filter = new HashMap<String, String>();
        assertThat(new SeleniumRemoteHelper().filterNodes(availableNodes, filter).size()).isEqualTo(6);

        filter.clear();
        filter.put("platformName", "(Android|iOS)");
        assertThat(new SeleniumRemoteHelper().filterNodes(availableNodes, filter).size()).isEqualTo(2);

        filter.clear();
        filter.put("platformName", "Android");
        assertThat(new SeleniumRemoteHelper().filterNodes(availableNodes, filter).size()).isEqualTo(1);

        filter.clear();
        filter.put("platformName", "iOS");
        assertThat(new SeleniumRemoteHelper().filterNodes(availableNodes, filter).size()).isEqualTo(1);

        filter.clear();
        filter.put("browserName", "chrome");
        assertThat(new SeleniumRemoteHelper().filterNodes(availableNodes, filter).size()).isEqualTo(4);

        filter.clear();
        filter.put("browserName", "(chrome|firefox)");
        assertThat(new SeleniumRemoteHelper().filterNodes(availableNodes, filter).size()).isEqualTo(6);

        filter.clear();
        filter.put("platform", "(LINUX|WINDOWS)");
        filter.put("screenResolution", "400x400");
        assertThat(new SeleniumRemoteHelper().filterNodes(availableNodes, filter).size()).isEqualTo(2);

        filter.clear();
        filter.put("browserName", "firefox");
        filter.put("platform", "WINDOWS");
        assertThat(new SeleniumRemoteHelper().filterNodes(availableNodes, filter).size()).isEqualTo(1);

        filter.clear();
        filter.put("browserName", "chrome");
        filter.put("platform", "LINUX");
        assertThat(new SeleniumRemoteHelper().filterNodes(availableNodes, filter).size()).isEqualTo(1);

        filter.clear();
        filter.put("browserName", "(chrome|firefox)");
        filter.put("platform", "LINUX");
        assertThat(new SeleniumRemoteHelper().filterNodes(availableNodes, filter).size()).isEqualTo(2);

        filter.clear();
        filter.put("platform", "(iOS|Android|WINDOWS)");
        assertThat(new SeleniumRemoteHelper().filterNodes(availableNodes, filter).size()).isEqualTo(4);

    }

    @Test(enabled = false)
    public void connectToStandAloneNode() throws IOException {
        List<String> sessions = new SeleniumRemoteHelper()
                .connectToStandAloneNode("localhost:4444")
                .getAllSessions();

        Assert.assertNotNull(sessions);
        assertThat(sessions.size()).isGreaterThan(0);
    }

    @Test
    public void getSessionsFromFile() throws IOException {
        String content = new String(Files.readAllBytes( Paths.get("src/test/resources/NodeSessions.json")));
        List<String> sessions = new SeleniumRemoteHelper()
                .setNodeSessions(content)
                .getAllSessions();

        Assert.assertNotNull(sessions);
        assertThat(sessions.size()).isEqualTo(3);
    }
}