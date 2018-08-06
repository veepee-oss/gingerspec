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

package com.privalia.qa.specs;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Realm;
import com.ning.http.client.Realm.AuthScheme;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import com.privalia.qa.conditions.Conditions;
import com.privalia.qa.utils.*;
import cucumber.api.DataTable;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.hjson.JsonValue;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.internal.Locatable;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import ru.yandex.qatools.ashot.shooting.cutter.CutStrategy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.privalia.qa.assertions.Assertions.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.testng.Assert.fail;

public class CommonG {

    private static final long DEFAULT_CURRENT_TIME = 1000L;

    private static final int DEFAULT_SLEEP_TIME = 1500;

    private final Logger logger = LoggerFactory.getLogger(ThreadProperty.get("class"));

    private RemoteWebDriver driver = null;

    private String browserName = null;

    private PreviousWebElements previousWebElements = null;

    private String parentWindow = "";

    private AsyncHttpClient client;

    private HttpResponse response;

    private List<Cookie> cookies = new ArrayList<Cookie>();

    private ResultSet previousCassandraResults;

    private DBCursor previousMongoResults;

    private List<JSONObject> previousElasticsearchResults;

    private List<Map<String, String>> previousCSVResults;

    private String resultsType = "";

    private Set<org.openqa.selenium.Cookie> seleniumCookies = new HashSet<org.openqa.selenium.Cookie>();

    private Map<String, String> headers = new HashMap<>();

    private Map<String, String> restCookies = new HashMap<>();

    private String restHost;

    private String restPort;

    private String webHost;

    private String webPort;

    private RemoteSSHConnection remoteSSHConnection;

    private int commandExitStatus;

    private String commandResult;

    private String restProtocol;

    private ZookeeperSecUtils zkSecClient;

    private Alert SeleniumAlert;

    private List<List<String>> previousSqlResult;

    private RequestSpecification RestRequest;

    private io.restassured.response.Response RestResponse;

    private List<Map<String, String>> lastFileParseResult;

    private Map<String, String> lastFileParseRecord;

    private String lastSoapResponse;

    /**
     * Checks if a given string matches a regular expression or contains a string
     *
     * @param expectedMessage message used for comparing
     * @return boolean
     */
    public static Pattern matchesOrContains(String expectedMessage) {
        Pattern pattern;
        if (expectedMessage.startsWith("regex:")) {
            String regex = expectedMessage.substring(expectedMessage.indexOf("regex:") + 6, expectedMessage.length());
            pattern = Pattern.compile(regex);
        } else {
            pattern = Pattern.compile(Pattern.quote(expectedMessage));
        }
        return pattern;
    }

    /**
     * Returns the last selected record (Map<String, String>)
     * @return
     */
    public Map<String, String> getLastFileParseRecord() {
        return lastFileParseRecord;
    }

    /**
     * Sets the record for the last operation (as (Map<String, String>))
     * @param lastFileParseRecord
     */
    public void setLastFileParseRecord(Map<String, String> lastFileParseRecord) {
        this.lastFileParseRecord = lastFileParseRecord;
    }

    /**
     * Returns the las response from a remote method execution in the webservice as an XML String
     * @return XML String
     */
    public String getLastSoapResponse() {
        return lastSoapResponse;
    }

    /**
     * Sets the response of the execution of a remote method in a webservice as an XML String
     * @param lastSoapResponse
     */
    public void setLastSoapResponse(String lastSoapResponse) {
        this.lastSoapResponse = lastSoapResponse;
    }

    /**
     * Returns the records resulted from the last operation when
     * decoding/parsing files
     * @return
     */
    public List<Map<String, String>> getLastFileParseResult() {
        return lastFileParseResult;
    }

    /**
     * Stores the result of an operation when decoding/parsing files
     * @param lastFileParseResult
     */
    public void setLastFileParseResult(List<Map<String, String>> lastFileParseResult) {
        this.lastFileParseResult = lastFileParseResult;
    }

    /**
     * Set the values of the cookies used when performing rest requests
     * @return
     */
    public Map<String, String> getRestCookies() {
        return restCookies;
    }

    /**
     * Returns the values of the cookies used in the rest requests
     * @param restCookies
     */
    public void setRestCookies(Map<String, String> restCookies) {
        this.restCookies = restCookies;
    }

    /**
     * Get the previos Rest response (restassured)
     * @return
     */
    public io.restassured.response.Response getRestResponse() {
        return RestResponse;
    }

    /**
     * Sets the Rest response (restassured)
     * @param restResponse
     */
    public void setRestResponse(io.restassured.response.Response restResponse) {
        RestResponse = restResponse;
    }

    /**
     * Returns the Rest Request object (restassured)
     * @return
     */
    public RequestSpecification getRestRequest() {
        return this.RestRequest;
    }

    /***
     * Sets the Rest request object (restassured)
     * @param restRequest
     */
    public void setRestRequest(RequestSpecification restRequest) {
        this.RestRequest = restRequest;
    }

    /**
     * Get the SQL result from the last step
     * @return
     */
    public List<List<String>> getPreviousSqlResult() {
        return previousSqlResult;
    }

    /**
     * Sets the result of the SQL sentence
     * @param previousSqlResult
     */
    public void setPreviousSqlResult(List<List<String>> previousSqlResult) {
        this.previousSqlResult = previousSqlResult;
    }

    /**
     * Get the common remote connection.
     *
     * @return RemoteConnection
     */
    public RemoteSSHConnection getRemoteSSHConnection() {
        return remoteSSHConnection;
    }

    /**
     * Set the remote connection.
     */
    public void setRemoteSSHConnection(RemoteSSHConnection remoteSSHConnection) {
        this.remoteSSHConnection = remoteSSHConnection;
    }

    /**
     * Get the common REST host.
     *
     * @return String
     */
    public String getRestHost() {
        return this.restHost;
    }

    /**
     * Set the REST host.
     *
     * @param restHost api host
     */
    public void setRestHost(String restHost) {
        this.restHost = restHost;
    }

    /**
     * Get the common REST port.
     *
     * @return String
     */
    public String getRestPort() {
        return this.restPort;
    }

    /**
     * Set the REST port.
     *
     * @param restPort api port
     */
    public void setRestPort(String restPort) {
        this.restPort = restPort;
    }

    /**
     * Get the common WEB host.
     *
     * @return String
     */
    public String getWebHost() {
        return this.webHost;
    }

    /**
     * Set the WEB host.
     *
     * @param webHost host where app is running
     */
    public void setWebHost(String webHost) {
        this.webHost = webHost;
    }

    /**
     * Get the common WEB port.
     *
     * @return String
     */
    public String getWebPort() {
        return this.webPort;
    }

    /**
     * Set the WEB port.
     *
     * @param webPort port where app is running
     */
    public void setWebPort(String webPort) {
        this.webPort = webPort;
    }

    /**
     * Get the common logger.
     *
     * @return Logger
     */
    public Logger getLogger() {
        return this.logger;
    }

    /**
     * Get the exception list.
     *
     * @return List(Exception)
     */
    public List<Exception> getExceptions() {
        return ExceptionList.INSTANCE.getExceptions();
    }

    /**
     * Get the textFieldCondition list.
     *
     * @return List(Exception)
     */
    public Condition<WebElement> getTextFieldCondition() {
        return Conditions.INSTANCE.getTextFieldCondition();
    }

    /**
     * Get the cassandra utils.
     *
     * @return CassandraUtils
     */
    public CassandraUtils getCassandraClient() {
        return CassandraUtil.INSTANCE.getCassandraUtils();
    }

    /**
     * Get the elasticSearch utils.
     *
     * @return ElasticSearchUtils
     */
    public ElasticSearchUtils getElasticSearchClient() {
        return ElasticSearchUtil.INSTANCE.getElasticSearchUtils();
    }

    /**
     * Get the Kafka utils.
     *
     * @return KafkaUtils
     */
    public KafkaUtils getKafkaUtils() {
        return KafkaUtil.INSTANCE.getKafkaUtils();
    }

    /**
     * Get the MongoDB utils.
     *
     * @return MongoDBUtils
     */
    public MongoDBUtils getMongoDBClient() {
        return MongoDBUtil.INSTANCE.getMongoDBUtils();
    }

    /**
     * Get the SQL utils.
     *
     * @return SqlUtils
     */
    public SqlUtils getSqlClient() {
        return SqlUtil.INSTANCE.getSqlUtils();
    }

    /**
     * Get the SOAP Services utils.
     *
     * @return SoapServiceUtils
     */
    public SoapServiceUtils getSoapServiceClient() {
        return SoapServiceUtil.INSTANCE.getSoapServiceUtils();
    }

    /**
     * Get the File Parser class
     *
     * @return FileParserUtils
     */
    public  FileParserUtils getFileParserUtil() {
        return FileParserUtil.INSTANCE.getFileParserUtils();
    }

    /**
     * Get the Zookeeper Sec utils.
     *
     * @return ZookeperSecUtils
     */
    public ZookeeperSecUtils getZookeeperSecClient() {
        return ZookeeperSecUtil.INSTANCE.getZookeeperSecUtils();
    }

    /**
     * Get the remoteWebDriver.
     *
     * @return RemoteWebDriver
     */
    public RemoteWebDriver getDriver() {
        return driver;
    }

    /**
     * Set the remoteDriver.
     *
     * @param driver driver to be used for testing
     */
    public void setDriver(RemoteWebDriver driver) {
        this.driver = driver;
    }

    /**
     * Get the browser name.
     *
     * @return String
     */
    public String getBrowserName() {
        return browserName;
    }

    /**
     * Set the browser name.
     *
     * @param browserName browser to be used for testing
     */
    public void setBrowserName(String browserName) {
        this.browserName = browserName;
    }

    /**
     * Looks for webelements inside a selenium context. This search will be made
     * by id, name and xpath expression matching an {@code locator} value
     *
     * @param method class of element to be searched
     * @param element webElement searched in selenium context
     * @param expectedCount integer. Expected number of elements.
     * @return List(WebElement)
     * @throws IllegalAccessException exception
     * @throws IllegalArgumentException exception
     * @throws SecurityException exception
     * @throws NoSuchFieldException exception
     * @throws ClassNotFoundException exception
     */
    public List<WebElement> locateElement(String method, String element,
                                          Integer expectedCount) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        List<WebElement> wel = null;

        if ("id".equals(method)) {
            logger.debug("Locating {} by id", element);
            wel = this.getDriver().findElements(By.id(element));
        } else if ("name".equals(method)) {
            logger.debug("Locating {} by name", element);
            wel = this.getDriver().findElements(By.name(element));
        } else if ("class".equals(method)) {
            logger.debug("Locating {} by class", element);
            wel = this.getDriver().findElements(By.className(element));
        } else if ("xpath".equals(method)) {
            logger.debug("Locating {} by xpath", element);
            wel = this.getDriver().findElements(By.xpath(element));
        } else if ("css".equals(method)) {
            wel = this.getDriver().findElements(By.cssSelector(element));
        } else {
            fail("Unknown search method: " + method);
        }

        if (expectedCount != -1) {
            PreviousWebElements pwel = new PreviousWebElements(wel);
            assertThat(this, pwel).as("Element count doesnt match").hasSize(expectedCount);
        }

        return wel;
    }

    /**
     * Similar to {@link CommonG#locateElement(String, String, Integer)}. Looks for webelements inside a selenium context
     * but with a wait condition. Instead of returning immediately a fail if the element is not found, the method waits a
     * maximum time (poolMaxTime) in which the condition is checked in intervals (poolingInterval).
     * The method also verify if the required elements are of the type specified.
     *
     * @param poolingInterval   Time between consecutive condition evaluations
     * @param poolMaxTime       Maximum time to wait for the condition to be true
     * @param method            class of element to be searched
     * @param element           webElement searched in selenium context
     * @param expectedCount     integer. Expected number of elements.
     * @param type              The expected style of the element: visible, clickable, present, hidden
     * @return                  List(WebElement)
     */
    public List<WebElement> locateElementWithPooling(int poolingInterval, int poolMaxTime, String method, String element,
                                          Integer expectedCount, String type) {

        Wait wait = new FluentWait(driver)
                .withTimeout(poolMaxTime, SECONDS)
                .pollingEvery(poolingInterval, SECONDS)
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(ElementNotVisibleException.class)
                .ignoring(TimeoutException.class)
                .ignoring(WebDriverException.class);

        logger.debug("Waiting for {} elements by xpath to be {}", expectedCount, type);

        try {
            List<WebElement> wel = (List<WebElement>) wait.until(new ElementCountByMethod(method, element, expectedCount));

            if ("visible".matches(type)) {
                wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(getByType(method, element)));
            } else if ("clickable".matches(type)) {
                wait.until(ExpectedConditions.elementToBeClickable(getByType(method, element)));
            } else if ("present".matches(type)) {
                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(getByType(method, element)));
            } else if ("hidden".matches(type)) {
                wait.until(ExpectedConditions.invisibilityOfElementLocated(getByType(method, element)));
            } else {
                fail("Unknown type: " + type);
            }

            return wel;
        } catch (Exception e) {
            this.getLogger().error("An exception ocurred: " + e.getMessage());
            this.getExceptions().add(e);
            throw e;
        }

    }

    /**
     * Locates an element within a document given a method and an element reference
     * @param method    class of element to be searched (id, name, class, xpath, css)
     * @param element   webElement searched in selenium context
     * @return          a By which locates elements by the method specified
     */
    private By getByType(String method, String element) {
        if ("id".equals(method)) {
            return By.id(element);
        } else if ("name".equals(method)) {
            return By.name(element);
        } else if ("class".equals(method)) {
            return By.className(element);
        } else if ("xpath".equals(method)) {
            return By.xpath(element);
        } else if ("css".equals(method)) {
            return By.className(element);
        }

        fail("Unknown method: " + method);
        return null;
    }

    /**
     * Similar to {@link CommonG#locateElementWithPooling(int, int, String, String, Integer, String)}, looks for an alert message
     * inside a selenium context. The method waits a maximum time (poolMaxTime) in which the condition is checked in intervals (poolingInterval).
     * @param poolingInterval   Time between consecutive condition evaluations
     * @param poolMaxTime       Maximum time to wait for the condition to be true
     * @return                  A selenium Alert object
     */
    public Alert waitAlertWithPooling(int poolingInterval, int poolMaxTime) {
        Wait wait = new FluentWait(driver)
                .withTimeout(poolMaxTime, SECONDS)
                .pollingEvery(poolingInterval, SECONDS)
                .ignoring(NoSuchElementException.class)
                .ignoring(java.util.NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(ElementNotVisibleException.class);

        logger.debug("Waiting for {} seconds for an alert to appear", poolMaxTime);
        return (Alert) wait.until(ExpectedConditions.alertIsPresent());

    }

    /**
     * Dismiss any existing alert message in the current selenium context
     */
    public void dismissSeleniumAlert() {
        assertThat(this, this.getSeleniumAlert()).as("There is not an alert present in the page").isNotEqualTo(null);
        this.getSeleniumAlert().dismiss();
    }

    /**
     * Accepts any existing alert message in the current selenium context
     */
    public void acceptSeleniumAlert() {
        assertThat(this, this.getSeleniumAlert()).as("There is not an alert present in the page").isNotEqualTo(null);
        this.getSeleniumAlert().accept();
    }

     /**
     * Capture a snapshot or an evidence in the driver
     *
     * @param driver driver used for testing
     * @param type type
     * @return String
     */
    public String captureEvidence(WebDriver driver, String type) {
        return captureEvidence(driver, type, "");
    }

    /**
     * Capture a snapshot or an evidence in the driver
     *
     * @param driver driver used for testing
     * @param type type
     * @param suffix suffix
     * @return String
     */
    public String captureEvidence(WebDriver driver, String type, String suffix) {

        String testSuffix = System.getProperty("TESTSUFFIX");
        String dir = "./target/executions/";
        if (testSuffix != null) {
            dir = dir + testSuffix + "/";
        }

        String clazz = ThreadProperty.get("class");
        String currentBrowser = ThreadProperty.get("browser");
        String currentData = ThreadProperty.get("dataSet");

        if (!currentData.equals("")) {
            currentData = currentData
                    .replaceAll("[\\\\|\\/|\\|\\s|:|\\*]", "_");
        }

        if (!"".equals(currentData)) {
            currentData = "-" + HashUtils.doHash(currentData);
        }

        Timestamp ts = new Timestamp(new java.util.Date().getTime());
        String outputFile = dir + clazz + "/"
                + ThreadProperty.get("feature") + "." + ThreadProperty.get("scenario") + "/" + currentBrowser +
                currentData + ts.toString() + suffix;

        outputFile = outputFile.replaceAll(" ", "_");

        if (type.endsWith("htmlSource")) {
            if (type.equals("framehtmlSource")) {
                boolean isFrame = (Boolean) ((JavascriptExecutor) driver)
                        .executeScript("return window.top != window.self");

                if (isFrame) {
                    outputFile = outputFile + "frame.html";
                } else {
                    outputFile = "";
                }
            } else if (type.equals("htmlSource")) {
                driver.switchTo().defaultContent();
                outputFile = outputFile + ".html";
            }

            if (!outputFile.equals("")) {
                String source = ((RemoteWebDriver) driver).getPageSource();

                File fout = new File(outputFile);
                boolean dirs = fout.getParentFile().mkdirs();

                try (FileOutputStream fos = new FileOutputStream(fout, true)) {
                    Writer out = new OutputStreamWriter(fos, "UTF8");
                    PrintWriter writer = new PrintWriter(out, false);
                    writer.append(source);
                    writer.close();
                    out.close();
                } catch (IOException e) {
                    logger.error("Exception on evidence capture", e);
                }
            }

        } else if ("screenCapture".equals(type)) {
            outputFile = outputFile + ".png";
            File file = null;
            driver.switchTo().defaultContent();
            ((Locatable) driver.findElement(By.tagName("body")))
                    .getCoordinates().inViewPort();

            if (currentBrowser.startsWith("android")
                    || currentBrowser.startsWith("droidemu")) {
                Actions actions = new Actions(driver);
                actions.keyDown(Keys.CONTROL).sendKeys(Keys.HOME).perform();
                actions.keyUp(Keys.CONTROL).perform();

                file = chromeFullScreenCapture(driver);
            } else {

                Screenshot screenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(500)).takeScreenshot(driver);
                try {
                    file = new File("target/temp");
                    ImageIO.write(screenshot.getImage(), "PNG",  file);
                } catch (IOException e) {
                    logger.error("Exception on taking screenshot", e);
                }
            }
            try {
                FileUtils.copyFile(file, new File(outputFile));
                file.delete();
            } catch (IOException e) {
                logger.error("Exception on copying browser screen capture", e);
            }
        }

        return outputFile;

    }

    private File adjustLastCapture(Integer newTrailingImageHeight,
                                   List<File> capture) {
        // cuts last image just in case it dupes information
        Integer finalHeight = 0;
        Integer finalWidth = 0;

        File trailingImage = capture.get(capture.size() - 1);
        capture.remove(capture.size() - 1);

        BufferedImage oldTrailingImage;
        File temp = null;
        try {
            oldTrailingImage = ImageIO.read(trailingImage);
            BufferedImage newTrailingImage = new BufferedImage(
                    oldTrailingImage.getWidth(), oldTrailingImage.getHeight()
                    - newTrailingImageHeight,
                    BufferedImage.TYPE_INT_RGB);

            newTrailingImage.createGraphics().drawImage(oldTrailingImage, 0,
                    0 - newTrailingImageHeight, null);

            File newTrailingImageF = File.createTempFile("tmpnewTrailingImage",
                    ".png");
            newTrailingImageF.deleteOnExit();

            ImageIO.write(newTrailingImage, "png", newTrailingImageF);

            capture.add(newTrailingImageF);

            finalWidth = ImageIO.read(capture.get(0)).getWidth();
            for (File cap : capture) {
                finalHeight += ImageIO.read(cap).getHeight();
            }

            BufferedImage img = new BufferedImage(finalWidth, finalHeight,
                    BufferedImage.TYPE_INT_RGB);

            Integer y = 0;
            BufferedImage tmpImg = null;
            for (File cap : capture) {
                tmpImg = ImageIO.read(cap);
                img.createGraphics().drawImage(tmpImg, 0, y, null);
                y += tmpImg.getHeight();
            }

            long ts = System.currentTimeMillis() / DEFAULT_CURRENT_TIME;

            temp = File.createTempFile("chromecap" + Long.toString(ts), ".png");
            temp.deleteOnExit();
            ImageIO.write(img, "png", temp);

        } catch (IOException e) {
            logger.error("Cant read image", e);
        }
        return temp;
    }

    private File chromeFullScreenCapture(WebDriver driver) {
        driver.switchTo().defaultContent();
        // scroll loop n times to get the whole page if browser is chrome
        ArrayList<File> capture = new ArrayList<File>();

        Boolean atBottom = false;
        Integer windowSize = ((Long) ((JavascriptExecutor) driver)
                .executeScript("return document.documentElement.clientHeight"))
                .intValue();

        Integer accuScroll = 0;
        Integer newTrailingImageHeight = 0;

        try {
            while (!atBottom) {

                Thread.sleep(DEFAULT_SLEEP_TIME);
                capture.add(((TakesScreenshot) driver)
                        .getScreenshotAs(OutputType.FILE));

                ((JavascriptExecutor) driver).executeScript("if(window.screen)"
                        + " {window.scrollBy(0," + windowSize + ");};");

                accuScroll += windowSize;
                if (getDocumentHeight(driver) <= accuScroll) {
                    atBottom = true;
                }
            }

        } catch (InterruptedException e) {
            logger.error("Interrupted waits among scrolls", e);
        }

        newTrailingImageHeight = accuScroll - getDocumentHeight(driver);
        return adjustLastCapture(newTrailingImageHeight, capture);
    }

    private Integer getDocumentHeight(WebDriver driver) {
        WebElement body = driver.findElement(By.tagName("html"));
        return body.getSize().getHeight();
    }

    /**
     * Returns the previous webElement
     *
     * @return List(WebElement)
     */
    public PreviousWebElements getPreviousWebElements() {
        return previousWebElements;
    }

    /**
     * Set the previous webElement
     */
    public void setPreviousWebElements(PreviousWebElements previousWebElements) {
        this.previousWebElements = previousWebElements;
    }

    /**
     * Returns the parentWindow
     *
     * @return String
     */
    public String getParentWindow() {
        return this.parentWindow;
    }

    /**
     * Sets the parentWindow
     */
    public void setParentWindow(String windowHandle) {
        this.parentWindow = windowHandle;

    }

    // COPIED FROM COMMON.JAVA
    public AsyncHttpClient getClient() {
        return client;
    }

    public void setClient(AsyncHttpClient client) {
        this.client = client;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(String endpoint, Response response) throws IOException {

        Integer statusCode = response.getStatusCode();
        String httpResponse = response.getResponseBody();
        List<Cookie> cookies = response.getCookies();
        this.response = new HttpResponse(statusCode, httpResponse, cookies);
    }

    /**
     * Returns the information contained in file passed as parameter. the file
     * is read with UTF-8 charset by default
     *
     * @param baseData path to file to be read
     * @param type     type of information, it can be: json|string
     * @return String
     */
    public String retrieveData(String baseData, String type) {
        return this.retrieveData(baseData, type, "UTF-8");
    }

    /**
     * Returns the information contained in file passed as parameter
     *
     * @param baseData path to file to be read
     * @param type     type of information, it can be: json|string
     * @param charset  charset to use when reading the file
     * @return String
     */
    public String retrieveData(String baseData, String type, String charset) {
        String result;

        InputStream stream = getClass().getClassLoader().getResourceAsStream(baseData);

        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        Reader reader;

        if (stream == null) {
            this.getLogger().error("File does not exist: {}", baseData);
            return "ERR! File not found: " + baseData;
        }

        try {
            reader = new BufferedReader(new InputStreamReader(stream, charset));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (Exception readerexception) {
            this.getLogger().error(readerexception.getMessage());
        } finally {
            try {
                stream.close();
            } catch (Exception closeException) {
                this.getLogger().error(closeException.getMessage());
            }
        }
        String text = writer.toString();

        String std = text.replace("\r", "").replace("\n", ""); // make sure we have unix style text regardless of the input


        if ("json".equals(type)) {
            result = JsonValue.readHjson(std).asObject().toString();
        } else {
            result = std;
        }
        return result;
    }


    /**
     * Returns the information modified
     *
     * @param data          string containing the information
     * @param type          type of information, it can be: json|string
     * @param modifications modifications to apply with a format:
     *                      WHERE,ACTION,VALUE
     *                      <p>
     *                      {@code
     *                      DELETE: Delete the key in json or string in current value
     *                      in case of DELETE action modifications is |key1|DELETE|N/A|
     *                      and with json {"key1":"value1","key2":{"key3":null}}
     *                      returns {"key2":{"key3":null}}
     *                      Example 2:
     *                      {"key1":"val1", "key2":"val2"} -> | key1 | DELETE | N/A | -> {"key2":"val2"}
     *                      "mystring" -> | str | DELETE | N/A | -> "mying"
     *                      }
     *                      <p>
     *                      {@code
     *                      ADD: Add new key to json or append string to current value.
     *                      in case of ADD action is  |N/A|ADD|&config=config|,
     *                      and with data  username=username&password=password
     *                      returns username=username&password=password&config=config
     *                      Example 2:
     *                      {"key1":"val1", "key2":"val2"} -> | key3 | ADD | val3 | -> {"key1":"val1", "key2":"val2", "key3":"val3"}
     *                      "mystring" -> | N/A | ADD | new | -> "mystringnew"
     *                      }
     *                      <p>
     *                      {@code
     *                      UPDATE: Update value in key or modify part of string.
     *                      in case of UPDATE action is |username=username|UPDATE|username=NEWusername|,
     *                      and with data username=username&password=password
     *                      returns username=NEWusername&password=password
     *                      Example 2:
     *                      {"key1":"val1", "key2":"val2"} -> | key1 | UPDATE | newval1 | -> {"key1":"newval1", "key2":"val2"}
     *                      "mystring" -> | str | UPDATE | mod | -> "mymoding"
     *                      }
     *                      <p>
     *                      {@code
     *                      PREPEND: Prepend value to key value or to string
     *                      in case of PREPEND action is |username=username|PREPEND|key1=value1&|,
     *                      and with data username=username&password=password
     *                      returns key1=value1&username=username&password=password
     *                      Example 2:
     *                      {"key1":"val1", "key2":"val2"} -> | key1 | PREPEND | new | -> {"key1":"newval1", "key2":"val2"}
     *                      "mystring" -> | N/A | PREPEND | new | -> "newmystring"
     *                      }
     *                      <p>
     *                      {@code
     *                      REPLACE: Update value in key or modify part of string.
     *                      in case of REPLACE action is |key2.key3|REPLACE|lu->REPLACE|
     *                      and with json {"key1":"value1","key2":{"key3":"value3"}}
     *                      returns {"key1":"value1","key2":{"key3":"vaREPLACEe3"}}
     *                      the  format is (WHERE,  ACTION,  CHANGE FROM -> TO).
     *                      REPLACE replaces a string or its part per other string
     *                      }
     *                      <p>
     *                      {@code
     *                      if modifications has fourth argument, the replacement is effected per special json object
     *                      the format is:
     *                      (WHERE,   ACTION,    CHANGE_TO, JSON_TYPE),
     *                      WHERE is the key, ACTION is REPLACE,
     *                      CHANGE_TO is the new value of the key,
     *                      JSON_TYPE is the type of jason object,
     *                      there are 5 special cases of json object replacements:
     *                      array|object|number|boolean|null
     *                      }
     *                      <p>
     *                      {@code
     *                      example1: |key2.key3|REPLACE|5|number|
     *                      with json {"key1":"value1","key2":{"key3":"value3"}}
     *                      returns {"key1":"value1","key2":{"key3":5}}
     *                      in this case it replaces value of key3
     *                      per jason number
     *                      }<p>
     *                      {@code
     *                      example2: |key2.key3|REPLACE|{}|object|
     *                      with json  {"key1":"value1","key2":{"key3":"value3"}}
     *                      returns  {"key1":"value1","key2":{"key3":{}}}
     *                      in this case it replaces per empty json object
     *                      }<p>
     *                      {@code
     *                      APPEND: Append value to key value or to string
     *                      {"key1":"val1", "key2":"val2"} -> | key1 | APPEND | new | -> {"key1":"val1new", "key2":"val2"}
     *                      "mystring" -> | N/A | APPEND | new | -> "mystringnew"
     *                      }
     * @return String
     * @throws Exception
     */
    public String modifyData(String data, String type, DataTable modifications) throws Exception {
        String modifiedData = data;
        JsonUtils jsonUtils = new JsonUtils();

        if ("json".equals(type)) {
            modifiedData = jsonUtils.modifyDataJson(data, type, modifications);

        } else {
            modifiedData = jsonUtils.modifyDataString(data, type, modifications);
        }
        return modifiedData;
    }

    /**
     * Generates the request based on the type of request, the end point, the data and type passed
     *
     * @param requestType type of request to be sent
     * @param secure      type of protocol
     * @param user        user to be used in request
     * @param password    password to be used in request
     * @param endPoint    end point to sent the request to
     * @param data        to be sent for PUT/POST requests
     * @param type        type of data to be sent (json|string)
     * @param codeBase64  XXX
     * @throws Exception exception
     */
    @Deprecated
    public Future<Response> generateRequest(String requestType, boolean secure, String user, String password, String endPoint, String data, String type, String codeBase64) throws Exception {
        return generateRequest(requestType, secure, user, password, endPoint, data, type);
    }

    /**
     * Generates the request based on the type of request, the end point, the data and type passed
     *
     * @param requestType type of request to be sent
     * @param secure      type of protocol
     * @param user        user to be used in request
     * @param password    password to be used in request
     * @param endPoint    end point to sent the request to
     * @param data        to be sent for PUT/POST requests
     * @param type        type of data to be sent (json|string)
     * @throws Exception exception
     */
    public Future<Response> generateRequest(String requestType, boolean secure, String user, String password, String endPoint, String data, String type) throws Exception {

        String protocol = this.getRestProtocol();
        Future<Response> response = null;
        BoundRequestBuilder request;
        Realm realm = null;


        if (this.getRestHost() == null) {
            throw new Exception("Rest host has not been set");
        }

        if (this.getRestPort() == null) {
            throw new Exception("Rest port has not been set");
        }

        if (this.getRestProtocol() == null) {
            protocol = "http://";
        }

        String restURL = protocol + this.getRestHost() + this.getRestPort();

        // Setup user and password for requests
        if (user != null) {
            realm = new Realm.RealmBuilder()
                    .setPrincipal(user)
                    .setPassword(password)
                    .setUsePreemptiveAuth(true)
                    .setScheme(AuthScheme.BASIC)
                    .build();
        }

        switch (requestType.toUpperCase()) {
            case "GET":
                request = this.getClient().prepareGet(restURL + endPoint);

                if ("json".equals(type)) {
                    request = request.setHeader("Content-Type", "application/json");
                } else if ("string".equals(type)) {
                    this.getLogger().debug("Sending request as: {}", type);
                    request = request.setHeader("Content-Type", "application/x-www-form-urlencoded");
                }

                if (this.getResponse() != null) {
                    this.getLogger().debug("Reusing coookies: {}", this.getResponse().getCookies());
                    request = request.setCookies(this.getResponse().getCookies());
                }

                for (Cookie cook : this.getCookies()) {
                    request = request.addCookie(cook);
                }

                if (this.getSeleniumCookies().size() > 0) {
                    for (org.openqa.selenium.Cookie cookie : this.getSeleniumCookies()) {
                        request.addCookie(new Cookie(cookie.getName(), cookie.getValue(),
                                false, cookie.getDomain(), cookie.getPath(), 99, false, false));
                    }
                }

                if (!this.headers.isEmpty()) {
                    for (Map.Entry<String, String> header : headers.entrySet()) {
                        request = request.setHeader(header.getKey(), header.getValue());
                    }
                }

                if (user != null) {
                    request = request.setRealm(realm);
                }

                response = request.execute();
                break;
            case "DELETE":
                request = this.getClient().prepareDelete(restURL + endPoint);

                if (this.getResponse() != null) {
                    request = request.setCookies(this.getResponse().getCookies());
                }

                if (this.getSeleniumCookies().size() > 0) {
                    for (org.openqa.selenium.Cookie cookie : this.getSeleniumCookies()) {
                        request.addCookie(new Cookie(cookie.getName(), cookie.getValue(),
                                false, cookie.getDomain(), cookie.getPath(), 99, false, false));
                    }
                }

                for (Cookie cook : this.getCookies()) {
                    request = request.addCookie(cook);
                }

                if (!this.headers.isEmpty()) {
                    for (Map.Entry<String, String> header : headers.entrySet()) {
                        request = request.setHeader(header.getKey(), header.getValue());
                    }
                }

                if (user != null) {
                    request = request.setRealm(realm);
                }

                response = request.execute();
                break;
            case "POST":
                if (data == null) {
                    Exception missingFields = new Exception("Missing fields in request.");
                    throw missingFields;
                } else {
                    request = this.getClient().preparePost(restURL + endPoint).setBody(data);
                    if ("json".equals(type)) {
                        request = request.setHeader("Content-Type", "application/json");
                    } else if ("string".equals(type)) {
                        this.getLogger().debug("Sending request as: {}", type);
                        request = request.setHeader("Content-Type", "application/x-www-form-urlencoded");
                    }

                    if (this.getResponse() != null) {
                        request = request.setCookies(this.getResponse().getCookies());
                    }

                    if (this.getSeleniumCookies().size() > 0) {
                        for (org.openqa.selenium.Cookie cookie : this.getSeleniumCookies()) {
                            request.addCookie(new Cookie(cookie.getName(), cookie.getValue(),
                                    false, cookie.getDomain(), cookie.getPath(), 99, false, false));
                        }
                    }

                    for (Cookie cook : this.getCookies()) {
                        request = request.addCookie(cook);
                    }

                    if (!this.headers.isEmpty()) {
                        for (Map.Entry<String, String> header : headers.entrySet()) {
                            request = request.setHeader(header.getKey(), header.getValue());
                        }
                    }

                    if (user != null) {
                        request = request.setRealm(realm);
                    }

                    response = this.getClient().executeRequest(request.build());
                    break;
                }
            case "PUT":
                if (data == null) {
                    Exception missingFields = new Exception("Missing fields in request.");
                    throw missingFields;
                } else {
                    request = this.getClient().preparePut(restURL + endPoint).setBody(data);
                    if ("json".equals(type)) {
                        request = request.setHeader("Content-Type", "application/json");
                    } else if ("string".equals(type)) {
                        request = request.setHeader("Content-Type", "application/x-www-form-urlencoded");
                    }

                    if (this.getResponse() != null) {
                        request = request.setCookies(this.getResponse().getCookies());
                    }

                    if (this.getSeleniumCookies().size() > 0) {
                        for (org.openqa.selenium.Cookie cookie : this.getSeleniumCookies()) {
                            request.addCookie(new Cookie(cookie.getName(), cookie.getValue(),
                                    false, cookie.getDomain(), cookie.getPath(), 99, false, false));
                        }
                    }

                    for (Cookie cook : this.getCookies()) {
                        request = request.addCookie(cook);
                    }

                    if (!this.headers.isEmpty()) {
                        for (Map.Entry<String, String> header : headers.entrySet()) {
                            request = request.setHeader(header.getKey(), header.getValue());
                        }
                    }

                    if (user != null) {
                        request = request.setRealm(realm);
                    }

                    response = this.getClient().executeRequest(request.build());
                    break;
                }
            case "CONNECT":
            case "PATCH":
                if (data == null) {
                    Exception missingFields = new Exception("Missing fields in request.");
                    throw missingFields;
                } else {
                    request = this.getClient().preparePatch(restURL + endPoint).setBody(data);
                    if ("json".equals(type)) {
                        request = request.setHeader("Content-Type", "application/json");
                    } else if ("string".equals(type)) {
                        this.getLogger().debug("Sending request as: {}", type);
                        request = request.setHeader("Content-Type", "application/x-www-form-urlencoded");
                    }

                    if (this.getResponse() != null) {
                        request = request.setCookies(this.getResponse().getCookies());
                    }

                    if (this.getSeleniumCookies().size() > 0) {
                        for (org.openqa.selenium.Cookie cookie : this.getSeleniumCookies()) {
                            request.addCookie(new Cookie(cookie.getName(), cookie.getValue(),
                                    false, cookie.getDomain(), cookie.getPath(), 99, false, false));
                        }
                    }

                    for (Cookie cook : this.getCookies()) {
                        request = request.addCookie(cook);
                    }

                    if (!this.headers.isEmpty()) {
                        for (Map.Entry<String, String> header : headers.entrySet()) {
                            request = request.setHeader(header.getKey(), header.getValue());
                        }
                    }

                    if (user != null) {
                        request = request.setRealm(realm);
                    }

                    response = this.getClient().executeRequest(request.build());
                    break;
                }
            case "HEAD":
            case "OPTIONS":
            case "REQUEST":
            case "TRACE":
                throw new Exception("Operation not implemented: " + requestType);
            default:
                throw new Exception("Operation not valid: " + requestType);
        }
        return response;
    }


    /**
     * Generates the request based on the type of request, the end point, the data and type passed
     *
     * @param requestType type of request to be sent
     * @param secure      type of protocol
     * @param endPoint    end point to sent the request to
     * @param data        to be sent for PUT/POST requests
     * @param type        type of data to be sent (json|string)
     * @throws Exception exception
     */
    @Deprecated
    public Future<Response> generateRequest(String requestType, boolean secure, String endPoint, String data, String type, String codeBase64) throws Exception {
        return generateRequest(requestType, false, null, null, endPoint, data, type, "");
    }

    /**
     * Generates a request to a REST endpoint
     * @param requestType   Request type (GET, POST, PUT, DELETE, PATCH)
     * @param endPoint      Final endpoint (i.e /user/1)
     * @throws Exception
     */
    public void generateRestRequest(String requestType, String endPoint) throws Exception {

        this.getRestRequest().basePath(endPoint);

        this.getLogger().debug("Generating " + requestType + " reauest to " + endPoint);

        switch (requestType) {
            case "GET":
                this.setRestResponse(this.getRestRequest().when().get());
                break;

            case "POST":
                this.setRestResponse(this.getRestRequest().when().post());
                break;

            case "PUT":
                this.setRestResponse(this.getRestRequest().when().put());
                break;

            case "DELETE":
                this.setRestResponse(this.getRestRequest().when().delete());
                break;

            case "PATCH":
                this.setRestResponse(this.getRestRequest().when().patch());
                break;

            default:
                throw new Exception("Operation not implemented: " + requestType);

        }

    }


    /**
     * Saves the value in the attribute in class extending CommonG.
     *
     * @param element attribute in class where to store the value
     * @param value   value to be stored
     * @throws NoSuchFieldException exception
     * @throws SecurityException exception
     * @throws IllegalArgumentException exception
     * @throws IllegalAccessException exception
     * @throws InstantiationException exception
     * @throws ClassNotFoundException exception
     * @throws NoSuchMethodException exception
     * @throws InvocationTargetException exception
     */

    public void setPreviousElement(String element, String value) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        Reflections reflections = new Reflections("com.privalia");
        Set classes = reflections.getSubTypesOf(CommonG.class);

        Object pp = (classes.toArray())[0];
        String qq = (pp.toString().split(" "))[1];
        Class<?> c = Class.forName(qq.toString());

        Field ff = c.getDeclaredField(element);
        ff.setAccessible(true);
        ff.set(null, value);
    }

    public ResultSet getCassandraResults() {
        return previousCassandraResults;
    }

    public void setCassandraResults(ResultSet results) {
        this.previousCassandraResults = results;
    }

    public DBCursor getMongoResults() {
        return previousMongoResults;
    }

    public void setMongoResults(DBCursor results) {
        this.previousMongoResults = results;
    }

    public List<JSONObject> getElasticsearchResults() {
        return previousElasticsearchResults;
    }

    public void setElasticsearchResults(List<JSONObject> results) {
        this.previousElasticsearchResults = results;
    }

    public List<Map<String, String>> getCSVResults() {
        return previousCSVResults;
    }

    public void setCSVResults(List<Map<String, String>> results) {
        this.previousCSVResults = results;
    }

    public String getResultsType() {
        return resultsType;
    }

    public void setResultsType(String resultsType) {
        this.resultsType = resultsType;
    }

    public Set<org.openqa.selenium.Cookie> getSeleniumCookies() {
        return seleniumCookies;
    }

    public void setSeleniumCookies(Set<org.openqa.selenium.Cookie> cookies) {
        this.seleniumCookies = cookies;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Checks the different results of a previous query to CSV file
     *
     * @param expectedResults A DataTable Object with all data needed for check the results. The DataTable must contains at least 2 columns:
     *                        a) A field column from the result
     *                        b) Occurrences column (Integer type)
     *                        <p>
     *                        Example:
     *                        |latitude| longitude|place     |occurrences|
     *                        |12.5    |12.7      |Valencia  |1           |
     *                        |2.5     | 2.6      |Madrid   |0           |
     *                        |12.5    |13.7      |Sevilla   |1           |
     *                        IMPORTANT: All columns must exist
     * @throws Exception exception
     */
    public void resultsMustBeCSV(DataTable expectedResults) throws Exception {
        if (getCSVResults() != null) {
            //Map for cucumber expected results
            List<Map<String, Object>> resultsListExpected = new ArrayList<Map<String, Object>>();
            Map<String, Object> resultsCucumber;

            for (int e = 1; e < expectedResults.getGherkinRows().size(); e++) {
                resultsCucumber = new HashMap<String, Object>();

                for (int i = 0; i < expectedResults.getGherkinRows().get(0).getCells().size(); i++) {
                    resultsCucumber.put(expectedResults.getGherkinRows().get(0).getCells().get(i), expectedResults.getGherkinRows().get(e).getCells().get(i));

                }
                resultsListExpected.add(resultsCucumber);
            }
            getLogger().debug("Expected Results: " + resultsListExpected.toString());

            getLogger().debug("Obtained Results: " + getCSVResults().toString());

            //Comparisons
            int occurrencesObtained = 0;
            int iterations = 0;
            int occurrencesExpected = 0;
            String nextKey;
            for (int e = 0; e < resultsListExpected.size(); e++) {
                iterations = 0;
                occurrencesObtained = 0;
                occurrencesExpected = Integer.parseInt(resultsListExpected.get(e).get("occurrences").toString());

                List<Map<String, String>> results = getCSVResults();
                for (Map<String, String> result : results) {
                    Iterator<String> it = resultsListExpected.get(0).keySet().iterator();

                    while (it.hasNext()) {
                        nextKey = it.next();
                        if (!nextKey.equals("occurrences")) {
                            if (result.get(nextKey).toString().equals(resultsListExpected.get(e).get(nextKey).toString())) {
                                iterations++;
                            }
                        }

                        if (iterations == resultsListExpected.get(0).keySet().size() - 1) {
                            occurrencesObtained++;
                            iterations = 0;
                        }
                    }
                    iterations = 0;
                }

                assertThat(occurrencesExpected).overridingErrorMessage("In row " + e + " have been found "
                        + occurrencesObtained + " results and " + occurrencesExpected + " were expected").isEqualTo(occurrencesObtained);
            }

        } else {
            throw new Exception("You must execute a query before trying to get results");
        }
    }


    /**
     * Checks the different results of a previous query to Cassandra database
     *
     * @param expectedResults A DataTable Object with all data needed for check the results. The DataTable must contains at least 2 columns:
     *                        a) A field column from the result
     *                        b) Occurrences column (Integer type)
     *                        <p>
     *                        Example:
     *                        |latitude| longitude|place     |occurrences|
     *                        |12.5    |12.7      |Valencia  |1           |
     *                        |2.5     | 2.6      |Madrid    |0           |
     *                        |12.5    |13.7      |Sevilla   |1           |
     *                        IMPORTANT: All columns must exist
     * @throws Exception exception
     */
    public void resultsMustBeCassandra(DataTable expectedResults) throws Exception {
        if (getCassandraResults() != null) {
            //Map for query results
            ColumnDefinitions columns = getCassandraResults().getColumnDefinitions();
            List<Row> rows = getCassandraResults().all();

            List<Map<String, Object>> resultsListObtained = new ArrayList<Map<String, Object>>();
            Map<String, Object> results;

            for (int i = 0; i < rows.size(); i++) {
                results = new HashMap<String, Object>();
                for (int e = 0; e < columns.size(); e++) {
                    results.put(columns.getName(e), rows.get(i).getObject(e));

                }
                resultsListObtained.add(results);

            }
            getLogger().debug("Results: " + resultsListObtained.toString());
            //Map for cucumber expected results
            List<Map<String, Object>> resultsListExpected = new ArrayList<Map<String, Object>>();
            Map<String, Object> resultsCucumber;

            for (int e = 1; e < expectedResults.getGherkinRows().size(); e++) {
                resultsCucumber = new HashMap<String, Object>();

                for (int i = 0; i < expectedResults.getGherkinRows().get(0).getCells().size(); i++) {
                    resultsCucumber.put(expectedResults.getGherkinRows().get(0).getCells().get(i), expectedResults.getGherkinRows().get(e).getCells().get(i));

                }
                resultsListExpected.add(resultsCucumber);
            }
            getLogger().debug("Expected Results: " + resultsListExpected.toString());

            //Comparisons
            int occurrencesObtained = 0;
            int iterations = 0;
            int occurrencesExpected = 0;
            String nextKey;
            for (int e = 0; e < resultsListExpected.size(); e++) {
                iterations = 0;
                occurrencesObtained = 0;
                occurrencesExpected = Integer.parseInt(resultsListExpected.get(e).get("occurrences").toString());

                for (int i = 0; i < resultsListObtained.size(); i++) {

                    Iterator<String> it = resultsListExpected.get(0).keySet().iterator();

                    while (it.hasNext()) {
                        nextKey = it.next();
                        if (!nextKey.equals("occurrences")) {
                            if (resultsListObtained.get(i).get(nextKey).toString().equals(resultsListExpected.get(e).get(nextKey).toString())) {
                                iterations++;
                            }

                        }

                        if (iterations == resultsListExpected.get(0).keySet().size() - 1) {
                            occurrencesObtained++;
                            iterations = 0;
                        }
                    }

                    iterations = 0;
                }
                assertThat(occurrencesExpected).overridingErrorMessage("In row " + e + " have been found "
                        + occurrencesObtained + " results and " + occurrencesExpected + " were expected").isEqualTo(occurrencesObtained);

            }
        } else {
            throw new Exception("You must execute a query before trying to get results");
        }
    }


    /**
     * Checks the different results of a previous query to Mongo database
     *
     * @param expectedResults A DataTable Object with all data needed for check the results. The DataTable must contains at least 2 columns:
     *                        a) A field column from the result
     *                        b) Occurrences column (Integer type)
     *                        <p>
     *                        Example:
     *                        |latitude| longitude|place     |occurrences|
     *                        |12.5    |12.7      |Valencia  |1           |
     *                        |2.5     | 2.6      |Madrid    |0           |
     *                        |12.5    |13.7      |Sevilla   |1           |
     *                        IMPORTANT: All columns must exist
     * @throws Exception exception
     */
    public void resultsMustBeMongo(DataTable expectedResults) throws Exception {
        if (getMongoResults() != null) {
            //Map for cucumber expected results
            List<Map<String, Object>> resultsListExpected = new ArrayList<Map<String, Object>>();
            Map<String, Object> resultsCucumber;

            for (int e = 1; e < expectedResults.getGherkinRows().size(); e++) {
                resultsCucumber = new HashMap<String, Object>();

                for (int i = 0; i < expectedResults.getGherkinRows().get(0).getCells().size(); i++) {
                    resultsCucumber.put(expectedResults.getGherkinRows().get(0).getCells().get(i), expectedResults.getGherkinRows().get(e).getCells().get(i));

                }
                resultsListExpected.add(resultsCucumber);
            }
            getLogger().debug("Expected Results: " + resultsListExpected.toString());

            //Comparisons
            int occurrencesObtained = 0;
            int iterations = 0;
            int occurrencesExpected = 0;
            String nextKey;
            for (int e = 0; e < resultsListExpected.size(); e++) {
                iterations = 0;
                occurrencesObtained = 0;
                occurrencesExpected = Integer.parseInt(resultsListExpected.get(e).get("occurrences").toString());

                String resultsListObtained = "[";
                DBCursor cursor = getMongoResults();
                while (cursor.hasNext()) {

                    DBObject row = cursor.next();

                    resultsListObtained = resultsListObtained + row.toString();
                    if (cursor.hasNext()) {
                        resultsListObtained = ", " + resultsListObtained;
                    }

                    Iterator<String> it = resultsListExpected.get(0).keySet().iterator();

                    while (it.hasNext()) {
                        nextKey = it.next();
                        if (!nextKey.equals("occurrences")) {
                            if (row.get(nextKey).toString().equals(resultsListExpected.get(e).get(nextKey).toString())) {
                                iterations++;
                            }
                        }

                        if (iterations == resultsListExpected.get(0).keySet().size() - 1) {
                            occurrencesObtained++;
                            iterations = 0;
                        }
                    }
                    iterations = 0;
                    if (cursor.hasNext()) {
                        resultsListObtained = resultsListObtained + ",";
                    }
                }

                resultsListObtained = resultsListObtained + "]";
                getLogger().debug("Results: " + resultsListObtained);

                assertThat(occurrencesExpected).overridingErrorMessage("In row " + e + " have been found "
                        + occurrencesObtained + " results and " + occurrencesExpected + " were expected").isEqualTo(occurrencesObtained);
            }

        } else {
            throw new Exception("You must execute a query before trying to get results");
        }
    }

    /**
     * Checks the different results of a previous query to Elasticsearch database
     *
     * @param expectedResults A DataTable Object with all data needed for check the results. The DataTable must contains at least 2 columns:
     *                        a) A field column from the result
     *                        b) Occurrences column (Integer type)
     *                        <p>
     *                        Example:
     *                        |latitude| longitude|place     |occurrences|
     *                        |12.5    |12.7      |Valencia  |1           |
     *                        |2.5     | 2.6      |Madrid    |0           |
     *                        |12.5    |13.7      |Sevilla   |1           |
     *                        IMPORTANT: All columns must exist
     * @throws Exception exception
     */
    public void resultsMustBeElasticsearch(DataTable expectedResults) throws Exception {
        if (getElasticsearchResults() != null) {
            List<List<String>> expectedResultList = expectedResults.raw();
            //Check size
            assertThat(expectedResultList.size() - 1).overridingErrorMessage(
                    "Expected number of columns to be" + (expectedResultList.size() - 1)
                            + "but was " + previousElasticsearchResults.size())
                    .isEqualTo(previousElasticsearchResults.size());
            List<String> columnNames = expectedResultList.get(0);
            for (int i = 0; i < previousElasticsearchResults.size(); i++) {
                for (int j = 0; j < columnNames.size(); j++) {
                    assertThat(expectedResultList.get(i + 1).get(j)).overridingErrorMessage("In row " + i + "and "
                            + "column " + j
                            + "have "
                            + "been "
                            + "found "
                            + expectedResultList.get(i + 1).get(j) + " results and " + previousElasticsearchResults.get(i).get(columnNames.get(j)).toString() + " were "
                            + "expected").isEqualTo(previousElasticsearchResults.get(i).get(columnNames.get(j)).toString());
                }
            }
        } else {
            throw new Exception("You must execute a query before trying to get results");
        }
    }

    /**
     * Runs a command locally
     *
     * @param command command used to be run locally
     */
    public void runLocalCommand(String command) throws Exception {

        String result = "";
        String line;
        Process p;
        try {
            p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            p.waitFor();
        } catch (java.io.IOException e) {
            this.commandExitStatus = 1;
            this.commandResult = "Error";
            return;
        }

        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line = input.readLine()) != null) {
            result += line;
        }

        input.close();
        this.commandResult = result;
        this.commandExitStatus = p.exitValue();

        p.destroy();

        if (p.isAlive()) {
            p.destroyForcibly();
        }

    }

    public int getCommandExitStatus() {
        return commandExitStatus;
    }

    public void setCommandExitStatus(int commandExitStatus) {
        this.commandExitStatus = commandExitStatus;
    }

    public String getCommandResult() {
        return commandResult;
    }

    public void setCommandResult(String commandResult) {
        this.commandResult = commandResult;
    }

    public String getRestProtocol() {
        return restProtocol;
    }

    /**
     * Set the REST host.
     *
     * @param restProtocol api protocol "http or https"
     */
    public void setRestProtocol(String restProtocol) {
        this.restProtocol = restProtocol;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }


    /**
     * Parse jsonpath expression from a given string.
     * <p>
     * If the string is json we can obtain its keys using ~ symbol.
     * <p>
     * If position is not null and the result of jsonpath expression is an array,
     * then this function will return the element at the given position at the array.
     * <p>
     * If position is null, it will return the result of the jsonpath evaluation as string.
     *
     * @param jsonString string to be parsed
     * @param expr       jsonpath expression
     * @param position   position from a search result
     */
    public String getJSONPathString(String jsonString, String expr, String position) {

        String value;

        if (expr.contains(".~")) {
            this.getLogger().debug("Expression referred to json keys");
            Pattern pattern = Pattern.compile("^(.*?).~(.*?)$");
            Matcher matcher = pattern.matcher(expr);
            String aux = null;
            String op = null;
            if (matcher.find()) {
                aux = matcher.group(1);
                op = matcher.group(2);
            }
            LinkedHashMap auxData = JsonPath.parse(jsonString).read(aux);
            JSONObject json = new JSONObject(auxData);
            List<String> keys = IteratorUtils.toList(json.keys());
            List<String> stringKeys = new ArrayList<String>();
            if (op.equals("")) {
                for (String key : keys) {
                    stringKeys.add("\"" + key + "\"");
                }
                value = stringKeys.toString();
            } else {
                Pattern patternOp = Pattern.compile("^\\[(-?\\d+)\\]$");
                Matcher matcherOp = patternOp.matcher(op);
                Integer index = null;
                Boolean isNegative = false;
                if (matcherOp.find()) {
                    if (matcherOp.group(1).contains("-")) {
                        isNegative = true;
                    }
                    index = Integer.parseInt(matcherOp.group(1).replace("-", ""));
                }
                if (isNegative) {
                    value = keys.get(keys.size() - index).toString();
                } else {
                    value = keys.get(index).toString();
                }

            }
        } else {
            String result = JsonValue.readHjson(jsonString).toString();
            Object data = JsonPath.parse(result).read(expr);
            if (position != null) {
                JSONArray jsonArray = new JSONArray(data.toString());
                value = jsonArray.get(Integer.parseInt(position)).toString();
            } else {
                if (data instanceof LinkedHashMap) {
                    value = (new JSONObject((LinkedHashMap) data)).toString();
                } else {
                    value = data.toString();
                }
            }
        }
        return value;
    }


    /**
     * Remove a subelement in a JsonPath
     *
     * @param jsonString String of the json
     * @param expr       regex to be removed
     */

    public String removeJSONPathElement(String jsonString, String expr) {

        Configuration conf = Configuration.builder().jsonProvider(new GsonJsonProvider()).mappingProvider(new GsonMappingProvider()).build();
        DocumentContext context = JsonPath.using(conf).parse(jsonString);
        context.delete(expr);
        return context.jsonString();
    }

    /**
     * The function searches over the array by certain field value,
     * and replaces occurences with the parameter provided.
     *
     * @param jsonString Original json object
     * @param key        Key to search
     * @param value      Value to replace key with
     */
    public String replaceJSONPathElement(String jsonString, String key, String value) {
        return JsonPath.parse(jsonString).set(key, value).jsonString();
    }

    /**
     * Evaluate an expression.
     * <p>
     * Object o could be a string or a list.
     *
     * @param o         object to be evaluated
     * @param condition condition to compare
     * @param result    expected result
     */
    public void evaluateJSONElementOperation(Object o, String condition, String result) throws Exception {

        if (o instanceof String) {
            String value = (String) o;
            switch (condition) {
                case "equal":
                    assertThat(value).as("Evaluate JSONPath/value does not match with proposed value").isEqualTo(result);
                    break;
                case "not equal":
                    assertThat(value).as("Evaluate JSONPath/value match with proposed value").isNotEqualTo(result);
                    break;
                case "contains":
                    assertThat(value).as("Evaluate JSONPath/value does not contain proposed value").contains(result);
                    break;
                case "does not contain":
                    assertThat(value).as("Evaluate JSONPath/value contain proposed value").doesNotContain(result);
                    break;
                case "length":
                    assertThat(value).as("Evaluate JSONPath/value contain proposed value").hasSize(Integer.parseInt(result));
                    break;
                case "exists":
                    assertThat(value).as("Evaluate JSONPath/value contain proposed value").isNotNull();
                    break;
                case "does not exists":
                    assertThat(value).as("Evaluate JSONPath/value contain proposed value").isNull();
                    break;
                case "size":
                    JsonValue jsonObject = JsonValue.readHjson(value);
                    if (jsonObject.isArray()) {
                        assertThat(jsonObject.asArray()).as("Keys size does not match").hasSize(Integer.parseInt(result));
                    } else {
                        Assertions.fail("Expected array for size operation check");
                    }
                    break;
                default:
                    Assertions.fail("Not implemented condition");
                    break;
            }
        } else if (o instanceof List) {
            List<String> keys = (List<String>) o;
            switch (condition) {
                case "contains":
                    assertThat(keys).as("Keys does not contain that name").contains(result);
                    break;
                case "size":
                    assertThat(keys).as("Keys size does not match").hasSize(Integer.parseInt(result));
                    break;
                default:
                    Assertions.fail("Operation not implemented for JSON keys");
            }
        }

    }

    public ZookeeperSecUtils getZkSecClient() {
        return zkSecClient;
    }

    public void runCommandAndGetResult(String command) throws Exception {
        getRemoteSSHConnection().runCommand(command);
        setCommandResult(getRemoteSSHConnection().getResult());
    }

    public String updateMarathonJson(String json) {
        return removeJSONPathElement(removeJSONPathElement(removeJSONPathElement(json, ".versionInfo"), ".version"), ".uris.*");
    }

    public void runCommandLoggerAndEnvVar(int exitStatus, String envVar, Boolean local) {
        List<String> logOutput = Arrays.asList(this.getCommandResult().split("\n"));
        StringBuffer log = new StringBuffer();
        int logLastLines = 25;
        if (logOutput.size() < 25) {
            logLastLines = logOutput.size();
        }
        for (String s : logOutput.subList(logOutput.size() - logLastLines, logOutput.size())) {
            log.append(s).append("\n");
        }

        if (envVar != null) {
            if (this.getRemoteSSHConnection() != null && !local) {
                ThreadProperty.set(envVar, this.getRemoteSSHConnection().getResult().trim());
            } else {
                ThreadProperty.set(envVar, this.getCommandResult().trim());
            }
        }
        if (this.getCommandExitStatus() != exitStatus) {
            if (System.getProperty("logLevel", "") != null && System.getProperty("logLevel", "").equalsIgnoreCase("debug")) {
                if (!("".equals(this.getCommandResult()))) {
                    this.getLogger().debug("Command complete stdout:\n{}", this.getCommandResult());
                }
            } else {
                this.getLogger().error("Command last {} lines stdout:", logLastLines);
                this.getLogger().error("{}", log);
            }
        } else {
            if (!("".equals(this.getCommandResult()))) {
                this.getLogger().debug("Command complete stdout:\n{}", this.getCommandResult());
            }
        }
    }

    public Alert getSeleniumAlert() {
        return SeleniumAlert;
    }

    public void setSeleniumAlert(Alert seleniumAlert) {
        SeleniumAlert = seleniumAlert;
    }

}
