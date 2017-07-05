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

package com.stratio.qa.data;

import com.google.common.collect.Lists;
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

    private BrowsersDataProvider() {
    }

    /**
     * Get the browsers available in a selenium grid.
     *
     * @param context context
     * @param testConstructor testConstructor
     * @return an iterator
     * @throws Exception exception
     */
    @DataProvider(parallel = true)
    public static Iterator<String[]> availableBrowsers(ITestContext context, Constructor<?> testConstructor)
            throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        List<String> browsers = gridBrowsers(map);

        return buildIterator(browsers);
    }


    /**
     * Get unique browsers available in a selenium grid.
     *
     * @param context context
     * @param testConstructor testConstructor
     * @return an iterator
     * @throws Exception exception
     */
    @DataProvider(parallel = true)
    public static Iterator<String[]> availableUniqueBrowsers(ITestContext context, Constructor<?> testConstructor)
            throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        List<String> browsers = gridBrowsers(map);

        HashSet<String> hs = new HashSet<String>();
        hs.addAll(browsers);
        browsers.clear();
        browsers.addAll(hs);

        return buildIterator(browsers);
    }

    /**
     * Get the browsers available with "iOS" as platformName in a selenium grid.
     *
     * @param context context
     * @param testConstructor testConstructor
     * @return an iterator
     * @throws Exception
     */
    @DataProvider(parallel = true)
    public static Iterator<String[]> availableIOSBrowsers(ITestContext context, Constructor<?> testConstructor)
            throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        map.put("platformName", "iOS");
        List<String> browsers = gridBrowsers(map);

        return buildIterator(browsers);
    }

    /**
     * Get the browsers available with "Android" or "iOS" as platformName in a selenium grid.
     *
     * @param context context
     * @param testConstructor testConstructor
     * @return an iterator
     * @throws Exception
     */
    @DataProvider(parallel = true)
    public static Iterator<String[]> availableMobileBrowsers(ITestContext context, Constructor<?> testConstructor)
            throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        map.put("platformName", "(Android|iOS)");
        List<String> browsers = gridBrowsers(map);

        return buildIterator(browsers);
    }

    /**
     * Build an String Iterator from String List.
     *
     * @param browsers browsers
     * @return an iterator
     */
    private static Iterator<String[]> buildIterator(List<String> browsers) {

        List<String[]> lData = Lists.newArrayList();

        for (String s : browsers) {
            lData.add(new String[]{s});
        }

        if (lData.size() == 0) {
            lData.add(new String[]{""});
        }

        return lData.iterator();
    }

    /**
     * Return available grid browsers applying filter defined by Map content.
     * Filter -> Regexp as: "filter.key()=filter.value(key)[,|}]"
     *
     * @param filter browser selected for test execution
     * @return browsers list
     */
    private static List<String> gridBrowsers(Map<String, String> filter) {

        ArrayList<String> response = new ArrayList<String>();

        String grid = System.getProperty("SELENIUM_GRID");

        if (grid != null) {
            grid = "http://" + grid + "/grid/console";
            Document doc;
            try {
                doc = Jsoup.connect(grid).timeout(DEFAULT_TIMEOUT).get();
            } catch (IOException e) {
                LOGGER.debug("Exception on connecting to Selenium grid: {}", e.getMessage());
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
                                Pattern pat = Pattern.compile("browserName=(.*?),.*?(version=(.*?)[,|}])");
                                Matcher m = pat.matcher(browserDetails.attr("title"));
                                while (m.find()) {
                                    response.add(m.group(1) + "_" + m.group(3));
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
        }
        // Sort response
        Collections.sort(response);
        return response;
    }
}