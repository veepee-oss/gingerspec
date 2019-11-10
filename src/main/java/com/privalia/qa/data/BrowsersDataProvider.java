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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class BrowsersDataProvider {

    public static final int DEFAULT_TIMEOUT = 20000;

    public static final int DEFAULT_LESS_LENGTH = 4;

    private static final Logger LOGGER = LoggerFactory.getLogger(BrowsersDataProvider.class);

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
     *
     * @param filter browser selected for test execution
     * @return browsers list
     */
    private static List<String> gridBrowsers(Map<String, String> filter) throws IOException {

        ArrayList<String> response = new ArrayList<String>();
        LOGGER.debug("Trying to get a list of Selenium-available browsers");

        String grid = System.getProperty("SELENIUM_GRID");
        String node = System.getProperty("SELENIUM_NODE");

        if (grid != null) {
            grid = "http://" + grid + "/grid/console";
            Document doc;
            try {
                doc = Jsoup.connect(grid).timeout(DEFAULT_TIMEOUT).get();
            } catch (IOException e) {
                LOGGER.error("Exception on connecting to Selenium grid: {}", e.getMessage());
                return response;
            }

            Elements slaves = (Elements) doc.select("div.proxy");

            for (Element slave : slaves) {
                String slaveStatus = slave.select("p.proxyname").first().text();
                if (!slaveStatus.contains("Connection") && !slaveStatus.contains("Conexión")) {
                    Integer iBusy = 0;
                    Elements browserList = slave.select("div.content_detail").select("*[title]");
                    Elements busyBrowserList = slave.select("div.content_detail").select("p > .busy");
                    for (Element browserDetails : browserList) {
                        if (browserDetails.attr("title").startsWith("{")) {
                            boolean filterCheck = true;
                            for (Map.Entry<String, String> f : filter.entrySet()) {
                                String stringFilter = f.getKey() + "=" + f.getValue() + "[,|}]";
                                Pattern patFilter = Pattern.compile(stringFilter);
                                Matcher mFilter = patFilter.matcher(browserDetails.attr("title"));
                                filterCheck = filterCheck & mFilter.find();
                            }
                            if (filterCheck) {
                                Pattern pat = Pattern.compile("browserName=(.*?),.*?(version|platformVersion)=(.*?),.*?platform=(.*?)[,|}]");
                                Matcher m = pat.matcher(browserDetails.attr("title"));
                                while (m.find()) {
                                    response.add(m.group(1) + "_" + m.group(3) + "_" + m.group(4));
                                }
                            }
                        } else {
                            String version = busyBrowserList.get(iBusy).parent().text();
                            String browser = busyBrowserList.get(iBusy).text();
                            version = version.substring(2);
                            version = version.replace(browser, "");
                            String browserSrc = busyBrowserList.get(iBusy).select("img").attr("src");
                            if (!browserSrc.equals("")) {
                                browser = browserSrc.substring(browserSrc.lastIndexOf('/') + 1, browserSrc.length()
                                        - DEFAULT_LESS_LENGTH);
                            }
                            response.add(browser + "_" + version);
                            iBusy++;
                        }
                    }
                }
            }
        } else if (node != null) {

            /**
             * Verify that the selenium standalone server actually exists and is online by trying a connection
             */
            Document doc;
            try {
                doc = Jsoup.connect("http://" + node + "/wd/hub/static/resource/hub.html").timeout(DEFAULT_TIMEOUT).get();
            } catch (IOException e) {
                LOGGER.error("Exception on connecting to Selenium node: {}", e.getMessage());
                return response;
            }

            /**
             * the process for connecting to a standalone node is the same as with the selenium grid, so SELENIUM_GRID is
             * set to the value of the address of the node (the SELENIUM_GRID variable is later used in {@link com.privalia.qa.specs.HookGSpec} class to
             * create the connection)
             */
            System.setProperty("SELENIUM_GRID", node);


            /**
             * Is necessary to set the browser type the node supports, since this information is later used in the
             * {@link com.privalia.qa.specs.HookGSpec} class to create the correct capabilities object
             */
            String nodeType = System.getProperty("SELENIUM_NODE_TYPE");

            if (nodeType == null) {
                LOGGER.warn("No Selenium Node browser type specified!. Using 'chrome' as default....");
                response.add("chrome_1.0");
            } else {
                response.add(nodeType + "_1.0");
            }


        } else {

            /**
             * If neither SELENIUM_GRID nor SELENIUM_NODE variables are found, the system will automatically try to use a
             * local driver. For this, SELENIUM_GRID is set to "local" and if no browser found, chrome is used as default.
             * Check {@link com.privalia.qa.specs.HookGSpec} for more info
             */

            LOGGER.warn("No Selenium Grid or Node address specified!. Trying to use local webdriver....");
            System.setProperty("SELENIUM_GRID", "local");
            String browser = System.getProperty("browser");
            if (browser == null) {
                LOGGER.warn("No browser specified, using chrome as default");
                response.add("chrome_1.0");
            } else {
                response.add(browser + "_1.0");
            }

        }

        // Sort response
        Collections.sort(response);
        return response;
    }
}