package com.stratio.specs;

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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.hjson.JsonValue;
import org.json.JSONObject;
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

import com.jayway.jsonpath.JsonPath;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.cookie.Cookie;
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
import com.stratio.tests.utils.ThreadProperty;

import cucumber.api.DataTable;

public class CommonG {

	private static final long DEFAULT_CURRENT_TIME = 1000L;
	private static final int DEFAULT_SLEEP_TIME = 1500;

	private final Logger logger = LoggerFactory.getLogger(ThreadProperty.get("class"));

	private RemoteWebDriver driver = null;
	private String browserName = null;
	private List<WebElement> previousWebElements = null;
	private String parentWindow = "";
	
	// COPIED FROM COMMON.JAVA
	private AsyncHttpClient client;
	//private String URL;
	private HttpResponse response;

	// CONNECTION DETAILS
	private String host;
	private String port;
	private String uRL;
	private String restHost;
	private String restPort;
	private String webHost;
	private String webPort;
	private String restURL;
	private String webURL;
	
	private String aux;
	
	public String getAux() {
	    return this.aux;
	}
	
	public void setAux(String aux) {
	    this.aux = aux;
	}
	
	/**
	 * Get the common host.
	 * 
	 * @return String
	 */
	public String getHost() {
		return this.host;
	}
	
	/**
	 * Get the common port.
	 * 
	 * @return String
	 */
	public String getPort() {
		return this.port;
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
	 * Set the host.
	 * 
	 * @param host
	 */
	public void setHost(String host) {
		this.host = host;
	}
	
	/**
	 * Set the port.
	 * 
	 * @param port
	 */
	public void setPort(String port) {
		this.port = port;
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
	 */
	public List<WebElement> locateElement(String method, String element,
			Integer expectedCount) {

		List<WebElement> wel = null;
		String newElement = replacePlaceholders(element);

		if ("id".equals(method)) {
			logger.info("Locating {} by id", newElement);
			wel = this.getDriver().findElements(By.id(newElement));
		} else if ("name".equals(method)) {
			logger.info("Locating {} by name", newElement);
			wel = this.getDriver().findElements(By.name(newElement));
		} else if ("class".equals(method)) {
			logger.info("Locating {} by class", newElement);
			wel = this.getDriver().findElements(By.className(newElement));
		} else if ("xpath".equals(method)) {
			logger.info("Locating {} by xpath", newElement);
			wel = this.getDriver().findElements(By.xpath(newElement));
		} else if ("css".equals(method)) {
			wel = this.getDriver().findElements(By.cssSelector(newElement));
		} else {
			fail("Unknown search method: " + method);
		}

		if (expectedCount != -1) {
			assertThat(wel.size()).as("Element count doesnt match").isEqualTo(
					expectedCount);
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
		String newElement = replacePlaceholders(element);

		if ("id".equals(method)) {
			logger.info("Locating {} by id", newElement);
			wel = this.getDriver().findElements(By.id(newElement));
		} else if ("name".equals(method)) {
			logger.info("Locating {} by name", newElement);
			wel = this.getDriver().findElements(By.name(newElement));
		} else if ("class".equals(method)) {
			logger.info("Locating {} by class", newElement);
			wel = this.getDriver().findElements(By.className(newElement));
		} else if ("xpath".equals(method)) {
			logger.info("Locating {} by xpath", newElement);
			wel = this.getDriver().findElements(By.xpath(newElement));
		} else if ("css".equals(method)) {
			wel = this.getDriver().findElements(By.cssSelector(newElement));
		} else {
			fail("Unknown search method: " + method);
		}

		return wel;
	}

	/**
	 * Replaces every placeholded element, enclosed in ${} with the
	 * corresponding java property
	 * 
	 * @param element
	 * 
	 * @return String
	 */
	public String replacePlaceholders(String element) {
		String newVal = element;
		while (newVal.contains("${")) {
			String placeholder = newVal.substring(newVal.indexOf("${"),
					newVal.indexOf("}") + 1);
			String modifier = "";
			String sysProp = "";
			if (placeholder.contains(".")) {
				sysProp = placeholder.substring(2, placeholder.indexOf("."));
				modifier = placeholder.substring(placeholder.indexOf(".") + 1,
						placeholder.length() - 1);
			} else {
				sysProp = placeholder.substring(2, placeholder.length() - 1);
			}

			String prop = "";
			if ("toLower".equals(modifier)) {
				prop = System.getProperty(sysProp, "").toLowerCase();
			} else if ("toUpper".equals(modifier)) {
				prop = System.getProperty(sysProp, "").toUpperCase();
			} else {
				prop = System.getProperty(sysProp, "");
			}
			newVal = newVal.replace(placeholder, prop);
		}

		return newVal;
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
	public List<WebElement> getPreviousWebElements() {
		return previousWebElements;
	}

	/**
	 * Set the previous webElement
	 * 
	 */
	public void setPreviousWebElements(List<WebElement> previousWebElements) {
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

	public String getRestURL() {
	    return restURL;
	}

	public void setRestURL(String restURL) {
	    this.restURL = restURL;
	}
	
	public String getWebURL() {
	    return webURL;
	}

	public void setWebURL(String webURL) {
	    this.webURL = webURL;
	}
	
	public String getURL() {
	    return uRL;
	}

	public void setURL(String uRL) {
	    this.uRL = uRL;
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
		    newValue = replacePlaceholders(newValue);
		    
		    modifiedData = JsonValue.readHjson(modifiedData).asObject().toString();
		    
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
	    		default:
	    		    throw new Exception("Modification type does not exist: " + operation);
		    }
		    modifiedData = new JSONObject(linkedHashMap).toString();
		}
	    } else {
		for (int i = 0; i < modifications.raw().size(); i ++) {
		    String value = modifications.raw().get(i).get(0);
		    String operation =  modifications.raw().get(i).get(1);
		    String newValue =  modifications.raw().get(i).get(2);
		    newValue = replacePlaceholders(newValue);
	    
		    switch(operation.toUpperCase()) {
	    		case "DELETE":
	    		    modifiedData = modifiedData.replace(value,"");
	    		    break;
	    		case "ADD":
	    		    modifiedData = modifiedData + newValue;
	    		    break;
	    		case "UPDATE":
	    		    modifiedData = modifiedData.replace(value, newValue);
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

	    String restURL = this.getURL();
	    if (restURL == null) {
		restURL = this.getRestURL();
		if (restURL == null) {
		    throw new Exception("Application URL has not been set");
		}
	    }
	    
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
			this.getLogger().info("Sending request as: {}", type);
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
	 * Saves the 
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
	
}
