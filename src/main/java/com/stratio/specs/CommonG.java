package com.stratio.specs;

import static com.stratio.assertions.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ThreadLocal;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Condition;
import org.hjson.JsonValue;
import org.json.JSONObject;
import org.json.JSONArray;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.ResultSet;
import com.jayway.jsonpath.JsonPath;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.cookie.Cookie;
import com.stratio.conditions.Conditions;
import com.stratio.tests.utils.AerospikeUtil;
import com.stratio.tests.utils.AerospikeUtils;
import com.stratio.tests.utils.CassandraUtil;
import com.stratio.tests.utils.CassandraUtils;
import com.stratio.tests.utils.ElasticSearchUtil;
import com.stratio.tests.utils.ElasticSearchUtils;
import com.stratio.tests.utils.ExceptionList;
import com.stratio.tests.utils.HashUtils;
import com.stratio.tests.utils.HttpResponse;
import com.stratio.tests.utils.MongoDBUtil;
import com.stratio.tests.utils.MongoDBUtils;
import com.stratio.tests.utils.PreviousWebElements;
import com.stratio.tests.utils.ThreadProperty;
import com.stratio.tests.utils.RemoteSSHConnection;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.Row;

import cucumber.api.DataTable;

public class CommonG {

	private static final long DEFAULT_CURRENT_TIME = 1000L;
	private static final int DEFAULT_SLEEP_TIME = 1500;

	private final Logger logger = LoggerFactory.getLogger(ThreadProperty.get("class"));

	private RemoteWebDriver driver = null;
	private String browserName = null;
	private PreviousWebElements previousWebElements = null;
	private String parentWindow = "";
	
	// COPIED FROM COMMON.JAVA
	private AsyncHttpClient client;
	//private String URL;
	private HttpResponse response;
    private ResultSet previousCassandraResults;
	private DBCursor previousMongoResults;
	private List<JSONObject> previousElasticsearchResults;
	private List<Map<String,String>> previousCSVResults;
    private String resultsType="";


	// CONNECTION DETAILS
	private String restHost;
	private String restPort;
	private String webHost;
	private String webPort;

	// REMOTE CONNECTION
	private RemoteSSHConnection remoteSSHConnection;

	/**
	 * Get the common remote connection.
	 *
	 * @return RemoteConnection
	 */
	public RemoteSSHConnection getRemoteSSHConnection() {
		return remoteSSHConnection;
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
	 * Get the common REST port.
	 * 
	 * @return String
	 */
	public String getRestPort() {
		return this.restPort;
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
	 * Get the common WEB port.
	 * 
	 * @return String
	 */
	public String getWebPort() {
		return this.webPort;
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
	 * @return List<Exception>
	 */
	public List<Exception> getExceptions() {
		return ExceptionList.INSTANCE.getExceptions();
	}
	
	/**
	 * Get the textFieldCondition list.
	 * 
	 * @return List<Exception>
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
	 * Get the Aerospike utils.
	 * 
	 * @return AerospikeUtils
	 */
	public AerospikeUtils getAerospikeClient() {
		return AerospikeUtil.INSTANCE.getAeroSpikeUtils();
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
	 * Get the remoteWebDriver.
	 * 
	 * @return RemoteWebDriver
	 */
	public RemoteWebDriver getDriver() {
		return driver;
	}

	/**
	 * Set the remote connection.
	 *
	 */
	public void setRemoteSSHConnection(RemoteSSHConnection remoteSSHConnection) {
		this.remoteSSHConnection = remoteSSHConnection;
	}

	/**
	 * Set the REST host.
	 * 
	 * @param restHost
	 */
	public void setRestHost(String restHost) {
		this.restHost = restHost;
	}
	
	/**
	 * Set the REST port.
	 * 
	 * @param restPort
	 */
	public void setRestPort(String restPort) {
		this.restPort = restPort;
	}
	
	/**
	 * Set the WEB host.
	 * 
	 * @param webHost
	 */
	public void setWebHost(String webHost) {
		this.webHost = webHost;
	}
	
	/**
	 * Set the WEB port.
	 * 
	 * @param webPort
	 */
	public void setWebPort(String webPort) {
		this.webPort = webPort;
	}
	
	
	/**
	 * Set the remoteDriver.
	 * 
	 * @param driver
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
	 * @param browserName
	 */
	public void setBrowserName(String browserName) {
		this.browserName = browserName;
	}

	/**
	 * Looks for webelements inside a selenium context. This search will be made
	 * by id, name and xpath expression matching an {@code locator} value
	 * 
	 * @param element
	 * @throws Exception
	 * 
	 * @return List<WebElement>
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws ClassNotFoundException 
	 */
	public List<WebElement> locateElement(String method, String element,
			Integer expectedCount) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		List<WebElement> wel = null;
		
		if ("id".equals(method)) {
			logger.info("Locating {} by id", element);
			wel = this.getDriver().findElements(By.id(element));
		} else if ("name".equals(method)) {
			logger.info("Locating {} by name", element);
			wel = this.getDriver().findElements(By.name(element));
		} else if ("class".equals(method)) {
			logger.info("Locating {} by class", element);
			wel = this.getDriver().findElements(By.className(element));
		} else if ("xpath".equals(method)) {
			logger.info("Locating {} by xpath", element);
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
	 * Looks for webelements inside a selenium context. This search will be made
	 * by id, name and xpath expression matching an {@code locator} value
	 * 
	 * @param element
	 * @throws Exception
	 * 
	 *             return List<WebElement>
	 */
	public List<WebElement> locateElements(String method, String element) {

		List<WebElement> wel = null;

		if ("id".equals(method)) {
			logger.info("Locating {} by id", element);
			wel = this.getDriver().findElements(By.id(element));
		} else if ("name".equals(method)) {
			logger.info("Locating {} by name", element);
			wel = this.getDriver().findElements(By.name(element));
		} else if ("class".equals(method)) {
			logger.info("Locating {} by class", element);
			wel = this.getDriver().findElements(By.className(element));
		} else if ("xpath".equals(method)) {
			logger.info("Locating {} by xpath", element);
			wel = this.getDriver().findElements(By.xpath(element));
		} else if ("css".equals(method)) {
			wel = this.getDriver().findElements(By.cssSelector(element));
		} else {
			fail("Unknown search method: " + method);
		}

		return wel;
	}
	
	/**
	 * Capture a snapshot or an evidence in the driver
	 * 
	 * @param driver
	 * @param type
	 * 
	 * @return String
	 */
	public String captureEvidence(WebDriver driver, String type) {

		String dir = "./target/executions/";

		String clazz = ThreadProperty.get("class");
		String currentBrowser = ThreadProperty.get("browser");
		String currentData = ThreadProperty.get("dataSet");

		if (!currentData.equals("")) {
			currentData = currentData
					.replaceAll("[\\\\|\\/|\\|\\s|:|\\*]", "_");
		}

		currentData = HashUtils.doHash(currentData);
		String outputFile = dir + clazz + "/" + currentBrowser + "-"
				+ currentData + new Timestamp(new java.util.Date().getTime());

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

				FileOutputStream fos;
				try {
					fos = new FileOutputStream(fout, true);
					Writer out = new OutputStreamWriter(fos, "UTF8");
					PrintWriter writer = new PrintWriter(out, false);
					writer.append(source);
					writer.close();
					out.close();
				} catch (IOException e) {
					logger.error("Exception on evidence capture", e);
				}
			}

		} else if (type.equals("screenCapture")) {
			outputFile = outputFile + ".png";
			File file = null;
			driver.switchTo().defaultContent();
			((Locatable) driver.findElement(By.tagName("body")))
					.getCoordinates().inViewPort();

			if (currentBrowser.startsWith("chrome")
					|| currentBrowser.startsWith("droidemu")) {
				Actions actions = new Actions(driver);
				actions.keyDown(Keys.CONTROL).sendKeys(Keys.HOME).perform();
				actions.keyUp(Keys.CONTROL).perform();

				file = chromeFullScreenCapture(driver);
			} else {
				file = ((TakesScreenshot) driver)
						.getScreenshotAs(OutputType.FILE);
			}
			try {
				FileUtils.copyFile(file, new File(outputFile));
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
	 * @return List<WebElement>
	 */
	public PreviousWebElements getPreviousWebElements() {
		return previousWebElements;
	}

	/**
	 * Set the previous webElement
	 * 
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
	 * 
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
	 * Returns the information contained in file passed as parameter
	 * 
	 * @param baseData path to file to be read
	 * @param type type of information, it can be: json|string
	 * 
	 * @return String
	 * @throws Exception 
	 */
	public String retrieveData(String baseData, String type) throws Exception {
	    String result;
	    
	    InputStream stream = getClass().getClassLoader().getResourceAsStream(baseData);
		
	    Writer writer=new StringWriter();
	    char[] buffer=new char[1024];
	    Reader reader;
	    
	    if (stream == null) {
		throw new Exception("File does not exist: " + baseData);
	    }
	    
	    try {
		reader=new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		int n;
		while ((n=reader.read(buffer))!=-1) writer.write(buffer, 0, n);
	    } finally {
		stream.close();
	    }
	    String text = writer.toString();
		
	    String std=text.replace("\r", ""); // make sure we have unix style text regardless of the input
	    std.replace("\n", "");

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
	 * @param data string containing the information
	 * @param type type of information, it can be: json|string
	 * @param modifications changes to make
	 * 
	 * @return String
	 * @throws Exception 
	 */
	public String modifyData(String data, String type, DataTable modifications) throws Exception {
	    String modifiedData = data;
	    
	    if ("json".equals(type)) {
		LinkedHashMap linkedHashMap;
		for (int i = 0; i < modifications.raw().size(); i ++) {
		    String composeKey = modifications.raw().get(i).get(0);
		    String operation =  modifications.raw().get(i).get(1);
		    String newValue =  modifications.raw().get(i).get(2);
	    
		    modifiedData = JsonValue.readHjson(modifiedData).asObject().toString();
		    
		    modifiedData = modifiedData.replaceAll("null", "\"TO_BE_NULL\"");
		    switch(operation.toUpperCase()) {
	    		case "DELETE":
	    		    linkedHashMap = JsonPath.parse(modifiedData).delete(composeKey).json();
	    		    break;
	    		case "ADD":
	    		    // Get the last key
	    		    String newKey;
	    		    String newComposeKey;
	    		    if (composeKey.contains(".")) {
	    			newKey = composeKey.substring(composeKey.lastIndexOf('.') + 1);
	    			newComposeKey = composeKey.substring(0, composeKey.lastIndexOf('.'));
	    		    } else {
	    			newKey = composeKey;
	    			newComposeKey = "$";
	    		    }
	    		    linkedHashMap = JsonPath.parse(modifiedData).put(newComposeKey, newKey, newValue).json();
	    		    break;
	    		case "UPDATE":
	    		    linkedHashMap = JsonPath.parse(modifiedData).set(composeKey, newValue).json();
	    		    break;
	    		case "APPEND":
	    		    String appendValue = JsonPath.parse(modifiedData).read(composeKey);
	    		    linkedHashMap = JsonPath.parse(modifiedData).set(composeKey, appendValue + newValue).json();
	    		    break;
	    		case "PREPEND":
	    		    String prependValue = JsonPath.parse(modifiedData).read(composeKey);
	    		    linkedHashMap = JsonPath.parse(modifiedData).set(composeKey, newValue + prependValue).json();
	    		    break;
	    		case "REPLACE":
	    		    String replaceValue = JsonPath.parse(modifiedData).read(composeKey);
	    		    String toBeReplaced = newValue.split("->")[0];
	    		    String replacement = newValue.split("->")[1];
	    		    newValue = replaceValue.replace(toBeReplaced, replacement);
	    		    linkedHashMap = JsonPath.parse(modifiedData).set(composeKey, newValue).json();	    		    
	    		    break;
	    		default:
	    		    throw new Exception("Modification type does not exist: " + operation);
		    }
		    
		    modifiedData = new JSONObject(linkedHashMap).toString();
		    modifiedData = modifiedData.replaceAll("\"TO_BE_NULL\"", "null");
		}
	    } else {
		for (int i = 0; i < modifications.raw().size(); i ++) {
		    String value = modifications.raw().get(i).get(0);
		    String operation =  modifications.raw().get(i).get(1);
		    String newValue =  modifications.raw().get(i).get(2);
	    
		    switch(operation.toUpperCase()) {
	    		case "DELETE":
	    		    modifiedData = modifiedData.replace(value,"");
	    		    break;
	    		case "ADD":
	    		case "APPEND":
	    		    modifiedData = modifiedData + newValue;
	    		    break;
	    		case "UPDATE":
	    		case "REPLACE":
	    		    modifiedData = modifiedData.replace(value, newValue);
	    		    break;
	    		case "PREPEND":
	    		    modifiedData = newValue + modifiedData;
	    		    break;
	    		default:
	    		    throw new Exception("Modification type does not exist: " + operation);
		    }
		}
	    }
	    return modifiedData;
	}
	
	/**
	 * Generates the request based on the type of request, the end point, the data and type passed
	 * @param requestType type of request to be sent
	 * @param endPoint end point to sent the request to
	 * @param data to be sent for PUT/POST requests
	 * @param type type of data to be sent (json|string)
	 * 
	 * @throws Exception 
	 * 
	 */
	public Future<Response> generateRequest(String requestType, String endPoint, String data, String type) throws Exception {
	    Future<Response> response = null;
	    BoundRequestBuilder request;

	    if (this.getRestHost() == null) {
		throw new Exception("Rest host has not been set");
	    }
	    
	    if (this.getRestPort() == null) {
		throw new Exception("Rest port has not been set");
	    }
	    
	    String restURL = "http://" + this.getRestHost() + this.getRestPort();
	    
	    switch(requestType.toUpperCase()) {
	    case "GET":
		request = this.getClient().prepareGet(restURL + endPoint);

		if (this.getResponse() != null) {
		    request = request.setCookies(this.getResponse().getCookies());
		}

		response = request.execute();
		break;
	    case "DELETE":
		request = this.getClient().prepareDelete(restURL + endPoint);

		if (this.getResponse() != null) {
		    request = request.setCookies(this.getResponse().getCookies());
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
			request = request.setHeader("Content-Type","application/json");
		    } else if ("string".equals(type)){
			this.getLogger().debug("Sending request as: {}", type);
			request = request.setHeader("Content-Type", "application/x-www-form-urlencoded");
		    }

		    if (this.getResponse() != null) {
			request = request.setCookies(this.getResponse().getCookies());
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
			request = request.setHeader("Content-Type","application/json");
		    } else if ("string".equals(type)){
			request = request.setHeader("Content-Type", "application/x-www-form-urlencoded");
		    }

		    if (this.getResponse() != null) {
			request = request.setCookies(this.getResponse().getCookies());
		    }

		    response = this.getClient().executeRequest(request.build());
		    break;
		}
	    case "CONNECT":
	    case "PATCH":
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
	 * Saves the value in the attribute in class extending CommonG.
	 * 
	 * @param element attribute in class where to store the value
	 * @param value value to be stored
	 * 
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	
	public void setPreviousElement(String element, String value) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
	    Reflections reflections = new Reflections("com.stratio");    
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

	public List<Map<String,String>> getCSVResults() { return previousCSVResults; }

	public void setCSVResults(List<Map<String,String>> results) {	this.previousCSVResults = results; }

    public String getResultsType() {
        return resultsType;
    }

    public void setResultsType(String resultsType) {
        this.resultsType = resultsType;
    }


	/**
     * Checks the different results of a previous query to CSV file
     *
     * @param expectedResults A DataTable Object with all data needed for check the results. The DataTable must contains at least 2 columns:
     * a) A field column from the result
     * b) Occurrences column (Integer type)
     *
     * Example:
     *      |latitude| longitude|place     |occurrences|
            |12.5    |12.7      |Valencia  |1           |
            |2.5     | 2.6      |Stratio   |0           |
            |12.5    |13.7      |Sevilla   |1           |
     * IMPORTANT: All columns must exist
     * @throws Exception
     */
	public void resultsMustBeCSV(DataTable expectedResults) throws Exception {
		if (getCSVResults() != null) {
			//Map for cucumber expected results
			List<Map<String,Object>>resultsListExpected=new ArrayList<Map<String,Object>>();
			Map<String,Object> resultsCucumber;

			for (int e=1; e<expectedResults.getGherkinRows().size();e++) {
				resultsCucumber = new HashMap<String,Object>();

				for (int i=0; i<expectedResults.getGherkinRows().get(0).getCells().size(); i++) {
					resultsCucumber.put(expectedResults.getGherkinRows().get(0).getCells().get(i), expectedResults.getGherkinRows().get(e).getCells().get(i));

				}
				resultsListExpected.add(resultsCucumber);
			}
			getLogger().debug("Expected Results: " + resultsListExpected.toString());

			getLogger().debug("Obtained Results: " + getCSVResults().toString());

			//Comparisons
			int occurrencesObtained=0;
			int iterations=0;
			int occurrencesExpected=0;
			String nextKey;
			for (int e=0; e<resultsListExpected.size(); e++) {
				iterations=0;
				occurrencesObtained=0;
				occurrencesExpected=Integer.parseInt(resultsListExpected.get(e).get("occurrences").toString());

				List<Map<String,String>> results = getCSVResults();
				for (Map<String,String> result : results) {
					Iterator<String> it = resultsListExpected.get(0).keySet().iterator();

					while (it.hasNext()) {
						nextKey = it.next();
						if (!nextKey.equals("occurrences")){
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
     * a) A field column from the result
     * b) Occurrences column (Integer type)
     *
     * Example:
     *      |latitude| longitude|place     |occurrences|
            |12.5    |12.7      |Valencia  |1           |
            |2.5     | 2.6      |Stratio   |0           |
            |12.5    |13.7      |Sevilla   |1           |
     * IMPORTANT: All columns must exist
     * @throws Exception
     */
	public void resultsMustBeCassandra(DataTable expectedResults) throws Exception {
		if (getCassandraResults()!=null) {
			//Map for query results
			ColumnDefinitions columns=getCassandraResults().getColumnDefinitions();
			List<Row> rows = getCassandraResults().all();

			List<Map<String,Object>>resultsListObtained=new ArrayList<Map<String,Object>>();
			Map<String,Object> results;

			for (int i=0; i<rows.size();i++) {
				results = new HashMap<String,Object>();
				for (int e=0; e<columns.size();e++) {
					results.put(columns.getName(e), rows.get(i).getObject(e));

				}
				resultsListObtained.add(results);

			}
			getLogger().debug("Results: "+resultsListObtained.toString());
			//Map for cucumber expected results
			List<Map<String,Object>>resultsListExpected=new ArrayList<Map<String,Object>>();
			Map<String,Object> resultsCucumber;

			for (int e=1; e<expectedResults.getGherkinRows().size();e++) {
				resultsCucumber = new HashMap<String,Object>();

				for (int i=0; i<expectedResults.getGherkinRows().get(0).getCells().size(); i++) {
					resultsCucumber.put(expectedResults.getGherkinRows().get(0).getCells().get(i), expectedResults.getGherkinRows().get(e).getCells().get(i));

				}
				resultsListExpected.add(resultsCucumber);
			}
			getLogger().debug("Expected Results: "+resultsListExpected.toString());

			//Comparisons
			int occurrencesObtained=0;
			int iterations=0;
			int occurrencesExpected=0;
			String nextKey;
			for (int e=0; e<resultsListExpected.size(); e++) {
				iterations=0;
				occurrencesObtained=0;
				occurrencesExpected=Integer.parseInt(resultsListExpected.get(e).get("occurrences").toString());

				for (int i=0; i<resultsListObtained.size(); i++) {

					Iterator<String> it = resultsListExpected.get(0).keySet().iterator();

					while (it.hasNext()) {
						nextKey=it.next();
						if (!nextKey.equals("occurrences")){
							if (resultsListObtained.get(i).get(nextKey).toString().equals(resultsListExpected.get(e).get(nextKey).toString())) {
								iterations++;
							}

						}

						if (iterations==resultsListExpected.get(0).keySet().size()-1) {
							occurrencesObtained++;
							iterations=0;
						}
					}

					iterations=0;
				}
				assertThat(occurrencesExpected).overridingErrorMessage("In row " +e+ " have been found "
						+occurrencesObtained+" results and "+ occurrencesExpected +" were expected").isEqualTo(occurrencesObtained);

			}
		} else {
			throw new Exception("You must execute a query before trying to get results");
		}
	}


	/**
     * Checks the different results of a previous query to Mongo database
     *
     * @param expectedResults A DataTable Object with all data needed for check the results. The DataTable must contains at least 2 columns:
     * a) A field column from the result
     * b) Occurrences column (Integer type)
     *
     * Example:
     *      |latitude| longitude|place     |occurrences|
            |12.5    |12.7      |Valencia  |1           |
            |2.5     | 2.6      |Stratio   |0           |
            |12.5    |13.7      |Sevilla   |1           |
     * IMPORTANT: All columns must exist
     * @throws Exception
     */
	public void resultsMustBeMongo(DataTable expectedResults) throws Exception {
		if (getMongoResults() != null) {
			//Map for cucumber expected results
			List<Map<String,Object>>resultsListExpected=new ArrayList<Map<String,Object>>();
			Map<String,Object> resultsCucumber;

			for (int e=1; e<expectedResults.getGherkinRows().size();e++) {
				resultsCucumber = new HashMap<String,Object>();

				for (int i=0; i<expectedResults.getGherkinRows().get(0).getCells().size(); i++) {
					resultsCucumber.put(expectedResults.getGherkinRows().get(0).getCells().get(i), expectedResults.getGherkinRows().get(e).getCells().get(i));

				}
				resultsListExpected.add(resultsCucumber);
			}
			getLogger().debug("Expected Results: "+resultsListExpected.toString());

			//Comparisons
			int occurrencesObtained=0;
			int iterations=0;
			int occurrencesExpected=0;
			String nextKey;
			for (int e=0; e<resultsListExpected.size(); e++) {
				iterations=0;
				occurrencesObtained=0;
				occurrencesExpected=Integer.parseInt(resultsListExpected.get(e).get("occurrences").toString());

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
						nextKey=it.next();
						if (!nextKey.equals("occurrences")){
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
	 * @param expectedResults: A DataTable Object with all data needed for check the results. The DataTable must contains at least 2 columns:
	 * a) A field column from the result
	 * b) Occurrences column (Integer type)
	 *
	 * Example:
	 *      |latitude| longitude|place     |occurrences|
			|12.5    |12.7      |Valencia  |1           |
			|2.5     | 2.6      |Stratio   |0           |
			|12.5    |13.7      |Sevilla   |1           |
	 * IMPORTANT: All columns must exist
	 * @throws Exception
	 */
	public void resultsMustBeElasticsearch(DataTable expectedResults) throws Exception {
		if (getElasticsearchResults() != null) {
			List<List<String>> expectedResultList = expectedResults.raw();
			//Check size
			assertThat(expectedResultList.size()-1).overridingErrorMessage(
						"Expected number of columns to be" + (expectedResultList.size()-1)
						+ "but was " + previousElasticsearchResults.size())
						.isEqualTo(previousElasticsearchResults.size());
			List<String> columnNames = expectedResultList.get(0);
			for(int i = 0; i< previousElasticsearchResults.size(); i++){
				for(int j =0; j < columnNames.size(); j++){
					assertThat(expectedResultList.get(i+1).get(j)).overridingErrorMessage("In row " + i + "and "
							+ "column " + j
							+ "have "
							+ "been "
							+ "found "
							+ expectedResultList.get(i+1).get(j) + " results and " + previousElasticsearchResults.get(i).get(columnNames.get(j)).toString() + " were "
							+ "expected").isEqualTo(previousElasticsearchResults.get(i).get(columnNames.get(j)).toString());
				}
			}
		} else {
			throw new Exception("You must execute a query before trying to get results");
		}


	}
	
}
