package com.privalia.qa.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains several functions to extract information from a remote Selenium grid
 * @author José Fernández
 */
public final class SeleniumGridHelper {

    public static final int DEFAULT_TIMEOUT = 20000;

    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumGridHelper.class);

    private static final Pattern p = Pattern.compile("title=\"(\\{.*?})\" \\/>");

    public SeleniumGridHelper setPageSource(String pageSource) {
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
    public SeleniumGridHelper connectToGrid(String gridBaseUrl) {

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
     * @return List of free nodes
     */
    public List<String> getAllAvailableNodes() throws JsonProcessingException {

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
     */
    public List<String> getAllNodes() throws JsonProcessingException {

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
     * @param node  Capabilities string
     * @return      Capabilities string with proper json string format
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

}
