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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains several functions to extract information from a remote Selenium grid/standalone node
 * @author José Fernández
 */
public final class SeleniumRemoteHelper {

    public static final int DEFAULT_TIMEOUT = 20000;

    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumRemoteHelper.class);

    public String getNodeSessions() {
        return this.nodeSessions;
    }

    public SeleniumRemoteHelper setNodeSessions(String nodeSessions) {
        this.nodeSessions = nodeSessions;
        return this;
    }

    private String nodeSessions;

    public SeleniumRemoteHelper setPageSource(String pageSource) {
        this.pageSource = pageSource;
        return this;
    }

    public String getPageSource() {
        return pageSource;
    }

    private String pageSource;

    /**
     * Connects to the grid to the given ip:port (i.e localhost:4444)
     *
     * @param gridBaseUrl grid ip:port
     * @return this object to allow chaining
     */
    public SeleniumRemoteHelper connectToGrid(String gridBaseUrl) {

        String grid = "http://" + gridBaseUrl + "/grid/console";
        Document doc;
        try {
            LOGGER.debug("Connecting to Selenium grid in {}", gridBaseUrl);
            doc = Jsoup.connect(grid).timeout(DEFAULT_TIMEOUT).get();
        } catch (IOException e) {
            LOGGER.error("Exception on connecting to Selenium grid: {}", e.getMessage());
            return null;
        }

        this.setPageSource(doc.html());
        return this;
    }

    /**
     * Returns the list of the nodes that are available (not busy). Each node in the
     * list if represented by a json string with the capabilities of the node
     *
     * @deprecated This method may not work with newer version of selenium grid since the UI has changed completely!
     * @return List of free nodes
     * @throws JsonProcessingException the json processing exception
     */
    @Deprecated
    public List<String> getAllAvailableNodesFromGrid() throws JsonProcessingException {

        Pattern p = Pattern.compile("title=\"(\\{.*?})\" \\/>");
        Matcher m = p.matcher(this.getPageSource());
        ArrayList<String> result = new ArrayList<String>();

        if (m.find()) {
            result.add(this.transformToJsonString(m.group(1)));
            while (m.find()) {
                result.add(this.transformToJsonString(m.group(1)));
            }
            LOGGER.debug("{} nodes detected", result.size());
        } else {
            LOGGER.warn("No nodes connected to the Selenium grid!");
        }
        return result;
    }

    /**
     * Returns the list of all the nodes (regardless if they are busy or not). Each node in the
     * list if represented by a json string with the capabilities of the node
     *
     * @return List of all nodes
     * @throws JsonProcessingException the json processing exception
     */
    public List<String> getAllNodesFromGrid() throws JsonProcessingException {

        Pattern p = Pattern.compile("<p>capabilities: Capabilities (.*?)<\\/p>");
        Matcher m = p.matcher(this.getPageSource());
        ArrayList<String> result = new ArrayList<String>();

        if (m.find()) {
            result.add(this.transformToJsonString(m.group(1)));
            while (m.find()) {
                result.add(this.transformToJsonString(m.group(1)));
            }
            LOGGER.debug("{} nodes detected", result.size());
        } else {
            LOGGER.warn("No nodes connected to the Selenium grid!");
        }
        return result;
    }

    /**
     * Apply the given filter to the list of nodes and only returns the nodes that matches
     * the given filter. The filter contains key-value pairs, where the key is the name of
     * one particular capability and the value the capability value.
     * For example, the filter browserName=firefox, will return from the given list of nodes
     * the ones where browserName has value firefox in the list of capabilities
     *
     * @param availableNodes    List of nodes
     * @param filter            Filter to apply
     * @return                  nodes that match the filter
     */
    public List<String> filterNodes(List<String> availableNodes, Map<String, String> filter) {

        ArrayList<String> result = new ArrayList<String>();
        HashSet<String> hs = new HashSet<String>();
        Pattern p = Pattern.compile("^\\((.+)\\)$");
        List<String> availableNodesCopy = new LinkedList<String>();
        availableNodesCopy.addAll(availableNodes);

        for (Map.Entry<String, String> entry : filter.entrySet()) {

            Matcher m = p.matcher(entry.getValue());
            hs.clear();

            if (m.find()) {

                String[] options = m.group(1).split("\\|");
                for (int i = 0; i <= options.length - 1; i++) {
                    for (String node : availableNodesCopy) {
                        if (node.contains("\"" + entry.getKey() + "\"" + ":" + "\"" + options[i] + "\"")) {
                            hs.add(node);
                        }
                    }
                }
            } else {
                for (String node : availableNodesCopy) {
                    if (node.contains("\"" + entry.getKey() + "\"" + ":" + "\"" + entry.getValue() + "\"")) {
                        hs.add(node);
                    }
                }
            }

            availableNodesCopy.clear();
            availableNodesCopy.addAll(hs);
        }

        return availableNodesCopy;
    }

    /**
     * Transforms the string representation of the node's capabilities into a proper json string
     *
     * @param node Capabilities string
     * @return Capabilities string with proper json string format
     * @throws JsonProcessingException the json processing exception
     */
    public String transformToJsonString(String node) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        String[] nodedetails = node.replace("{", "").replace("}", "").split(",");
        Map<String, String> nodeDetailsMap = new HashMap<String, String>();
        for (String detail : nodedetails) {
            try {
                nodeDetailsMap.put(detail.split("=")[0].trim(), detail.split("=")[1].trim());
            } catch (Exception e) {

            }
        }

        return objectMapper.writeValueAsString(nodeDetailsMap);
    }

    /**
     * Verifies the Standalone node is in the address specified and get the list
     * of sessions
     * @param standAloneNode    Address as ip:port (i.e: localhost:4444)
     * @return                  This to allow chaining
     * @throws IOException      IOException
     */
    public SeleniumRemoteHelper connectToStandAloneNode(String standAloneNode) throws IOException {

        LOGGER.debug("Trying to connect to {}", "http://" + standAloneNode + "/wd/hub/sessions");
        URL url = new URL("http://" + standAloneNode + "/wd/hub/sessions");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        if (con.getResponseCode() != 200) {
            LOGGER.error("Exception on connecting to node, response code {}: {}", con.getResponseCode(), con.getResponseMessage());
            return null;
        }

        LOGGER.debug("Response code {} with message {}", con.getResponseCode(), con.getResponseMessage());

        //Read the json response to get the capabilities of the active sessions
        InputStream in = new BufferedInputStream(con.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line;
        StringBuilder result = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        this.setNodeSessions(new String(result));
        return this;
    }

    /**
     * Gets the fetch list of sessions and returns them as list of capabilities
     * @return              list of sessions capabilities
     * @throws IOException  IOException
     */
    public List<String> getAllSessions() throws IOException {

        ArrayList<String> response = new ArrayList<String>();
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode sessions = (ArrayNode) mapper.readTree(this.getNodeSessions()).get("value");

        for (JsonNode session: sessions) {
            response.add(session.get("capabilities").toString());
        }
        return response;
    }
}
