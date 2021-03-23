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

package com.privalia.qa.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.privalia.qa.utils.SeleniumRemoteHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Handles the connection to a Selenium Grid/Standalone Nodes to all classes
 * with the {@link org.testng.annotations.Factory} annotation in the constructor
 */
public final class BrowsersDataProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrowsersDataProvider.class);

    private static SeleniumRemoteHelper seleniumRemoteHelper = new SeleniumRemoteHelper();

    public BrowsersDataProvider() {
    }

    /**
     * Get the browsers available in a selenium grid.
     *
     * @param context         context
     * @param testConstructor testConstructor
     * @return an iterator
     * @throws Exception exception
     */
    @DataProvider(parallel = true)
    public static Object[] availableBrowsers(ITestContext context, Constructor<?> testConstructor)
            throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        List<String> browsers = gridBrowsers(map);

        return (Object[]) browsers.toArray();
    }


    /**
     * Get unique browsers available in a selenium grid.
     *
     * @param context         context
     * @param testConstructor testConstructor
     * @return an iterator
     * @throws Exception exception
     */
    @DataProvider(parallel = true)
    public static Object[] availableUniqueBrowsers(ITestContext context, Constructor<?> testConstructor)
            throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        List<String> browsers = gridBrowsers(map);

        HashSet<String> hs = new HashSet<String>();
        hs.addAll(browsers);
        browsers.clear();
        browsers.addAll(hs);

        return (Object[]) browsers.toArray();
    }

    /**
     * Get the browsers available with "iOS" as platformName in a selenium grid.
     *
     * @param context         context
     * @param testConstructor testConstructor
     * @return an iterator
     * @throws Exception Exception
     */
    @DataProvider(parallel = true)
    public static Object[] availableIOSBrowsers(ITestContext context, Constructor<?> testConstructor)
            throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        map.put("platformName", "iOS");
        List<String> browsers = gridBrowsers(map);

        return (Object[]) browsers.toArray();
    }

    /**
     * Get the browsers available with "Android" or "iOS" as platformName in a selenium grid.
     *
     * @param context         context
     * @param testConstructor testConstructor
     * @return an iterator
     * @throws Exception the exception
     */
    @DataProvider(parallel = true)
    public static Object[] availableMobileBrowsers(ITestContext context, Constructor<?> testConstructor)
            throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        map.put("platformName", "(Android|iOS)");
        List<String> browsers = gridBrowsers(map);

        return (Object[]) browsers.toArray();
    }

    /**
     * Return available grid browsers applying filter defined by Map content.
     * Filter -> Regexp as: "filter.key()=filter.value(key)[,|}]"
     * <p>
     * This method will try to connect to the given grid/node and read its list of
     * capabilities as a json string. If more than one node is found (like in the case
     * of several nodes connected to a grid), this will return a List of all the
     * capabilities found
     *
     * @param filter browser selected for test execution
     * @return list of capabilities of the active sessions
     */
    private static List<String> gridBrowsers(Map<String, String> filter) throws IOException {

        ArrayList<String> response = new ArrayList<>();

        LOGGER.debug("Trying to get a list of Selenium-available browsers");

        String grid = System.getProperty("SELENIUM_GRID");

        if (grid != null) {

            List<String> availableNodes = seleniumRemoteHelper
                    .connectToGrid(grid)
                    .getAllAvailableNodesFromGrid();

            /**
             * if browserName, platform, platformName or version (typical capabilities fot browser selection) are found,
             * use those capabilities as filter
             */
            if (System.getProperty("browserName") != null) {
                filter.put("browserName", System.getProperty("browserName"));
            }
            if (System.getProperty("platform") != null) {
                filter.put("platform", System.getProperty("platform"));
            }
            if (System.getProperty("version") != null) {
                filter.put("version", System.getProperty("version"));
            }
            if (System.getProperty("platformName") != null) {
                filter.put("platformName", System.getProperty("platformName"));
            }

            response.addAll(seleniumRemoteHelper.filterNodes(availableNodes, filter));

            if (response.size() == 0) {
                LOGGER.warn("No nodes match the given filter: " + filter.toString());
            }

        } else {
            LOGGER.warn("No Selenium grid detected, using local driver");
            response.add(null);
        }

        Collections.sort(response);
        return response;
    }
}