package com.stratio.specs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stratio.tests.utils.AerospikeUtil;
import com.stratio.tests.utils.AerospikeUtils;
import com.stratio.tests.utils.CassandraUtil;
import com.stratio.tests.utils.CassandraUtils;
import com.stratio.tests.utils.ElasticSearchUtil;
import com.stratio.tests.utils.ElasticSearchUtils;
import com.stratio.tests.utils.ExceptionList;
import com.stratio.tests.utils.HashUtils;
import com.stratio.tests.utils.MongoDBUtil;
import com.stratio.tests.utils.MongoDBUtils;
import com.stratio.tests.utils.ThreadProperty;

/**
 * @author Javier Delgado
 * @author Hugo Dominguez
 * 
 */
public class CommonG {

    private static final long DEFAULT_CURRENT_TIME = 1000L;
    private static final int DEFAULT_SLEEP_TIME = 1500;

    private final Logger logger = LoggerFactory.getLogger(ThreadProperty.get("class"));

    private RemoteWebDriver driver = null;
    private String browserName = null;
    private List<WebElement> previousWebElements = null;
    private String parentWindow = "";

    /**
     * Get the common logger.
     * 
     * @return
     */
    public Logger getLogger() {
        return this.logger;
    }

    /**
     * Get the exception list.
     * 
     * @return
     */
    public List<Exception> getExceptions() {
        return ExceptionList.INSTANCE.getExceptions();
    }

    /**
     * Get the cassandra utils.
     * 
     * @return
     */
    public CassandraUtils getCassandraClient() {
        return CassandraUtil.INSTANCE.getCassandraUtils();
    }

    /**
     * Get the elasticSearch utils.
     * 
     * @return
     */
    public ElasticSearchUtils getElasticSearchClient() {
        return ElasticSearchUtil.INSTANCE.getElasticSearchUtils();
    }

    /**
     * Get the Aerospike utils.
     * 
     * @return
     */
    public AerospikeUtils getAerospikeClient() {
        return AerospikeUtil.INSTANCE.getAeroSpikeUtils();
    }

    /**
     * Get the MongoDB utils.
     * 
     * @return
     */
    public MongoDBUtils getMongoDBClient() {
        return MongoDBUtil.INSTANCE.getMongoDBUtils();
    }

    /**
     * Get the remoteWebDriver.
     * 
     * @return
     */
    public RemoteWebDriver getDriver() {
        return driver;
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
     * @return
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
     * Looks for webelements inside a selenium context. This search will be made by id, name and xpath expression
     * matching an {@code locator} value
     * 
     * @param element
     * @throws Exception
     */
    public List<WebElement> locateElement(String method, String element, Integer expectedCount) {

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
            assertThat(wel.size()).as("Element count doesnt match").isEqualTo(expectedCount);
        }

        return wel;
    }

    /**
     * Replaces every placeholded element, enclosed in ${} with the corresponding java property
     * 
     * @param element
     */
    public String replacePlaceholders(String element) {
        String newVal = element;
        while (newVal.contains("${")) {
            String placeholder = newVal.substring(newVal.indexOf("${"), newVal.indexOf("}") + 1);
            String modifier = "";
            String sysProp = "";
            if (placeholder.contains(".")) {
                sysProp = placeholder.substring(2, placeholder.indexOf("."));
                modifier = placeholder.substring(placeholder.indexOf(".") + 1, placeholder.length() - 1);
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

    public String captureEvidence(WebDriver driver, String type) {

        String dir = "./target/executions/";

        String clazz = ThreadProperty.get("class");
        String currentBrowser = ThreadProperty.get("browser");
        String currentData = ThreadProperty.get("dataSet");

        if (!currentData.equals("")) {
            currentData = currentData.replaceAll("[\\\\|\\/|\\|\\s|:|\\*]", "_");
        }

        currentData = HashUtils.doHash(currentData);
        String outputFile = dir + clazz + "/" + currentBrowser + "-" + currentData
                + new Timestamp(new java.util.Date().getTime());

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
            ((Locatable) driver.findElement(By.tagName("body"))).getCoordinates().inViewPort();

            if (currentBrowser.startsWith("chrome") || currentBrowser.startsWith("droidemu")) {
                Actions actions = new Actions(driver);
                actions.keyDown(Keys.CONTROL).sendKeys(Keys.HOME).perform();
                actions.keyUp(Keys.CONTROL).perform();

                file = chromeFullScreenCapture(driver);
            } else {
                file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            }
            try {
                FileUtils.copyFile(file, new File(outputFile));
            } catch (IOException e) {
                logger.error("Exception on copying browser screen capture", e);
            }
        }

        return outputFile;

    }

    private File adjustLastCapture(Integer newTrailingImageHeight, List<File> capture) {
        // cuts last image just in case it dupes information
        Integer finalHeight = 0;
        Integer finalWidth = 0;

        File trailingImage = capture.get(capture.size() - 1);
        capture.remove(capture.size() - 1);

        BufferedImage oldTrailingImage;
        File temp = null;
        try {
            oldTrailingImage = ImageIO.read(trailingImage);
            BufferedImage newTrailingImage = new BufferedImage(oldTrailingImage.getWidth(),
                    oldTrailingImage.getHeight() - newTrailingImageHeight, BufferedImage.TYPE_INT_RGB);

            newTrailingImage.createGraphics().drawImage(oldTrailingImage, 0, 0 - newTrailingImageHeight, null);

            File newTrailingImageF = File.createTempFile("tmpnewTrailingImage", ".png");
            newTrailingImageF.deleteOnExit();

            ImageIO.write(newTrailingImage, "png", newTrailingImageF);

            capture.add(newTrailingImageF);

            finalWidth = ImageIO.read(capture.get(0)).getWidth();
            for (File cap : capture) {
                finalHeight += ImageIO.read(cap).getHeight();
            }

            BufferedImage img = new BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_INT_RGB);

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
                .executeScript("return document.documentElement.clientHeight")).intValue();

        Integer accuScroll = 0;
        Integer newTrailingImageHeight = 0;

        try {
            while (!atBottom) {

                Thread.sleep(DEFAULT_SLEEP_TIME);
                capture.add(((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE));

                ((JavascriptExecutor) driver).executeScript("if(window.screen)" + " {window.scrollBy(0," + windowSize
                        + ");};");

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

    public List<WebElement> getPreviousWebElements() {
        return previousWebElements;
    }

    public void setPreviousWebElements(List<WebElement> previousWebElements) {
        this.previousWebElements = previousWebElements;
    }

    public String getParentWindow() {
        return this.parentWindow;
    }

    public void setParentWindow(String windowHandle) {
        this.parentWindow = windowHandle;

    }
}
