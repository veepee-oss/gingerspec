package com.stratio.specs;

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
     */
    public List<WebElement> locateElement(String element) {

        List<WebElement> wel = null;

        if (!element.contains(":")) {
            wel = this.locateSelenium(element);
        } else {
            String[] attrib = element.split(":");
            wel = this.locateCssSelector(attrib[0], attrib[1]);
        }
        return wel;
    }

    /**
     * Looks for webelements inside a selenium context. This search will be made by id, name and xpath expression
     * matching an {@code locator} value
     * 
     * @param element
     */
    private List<WebElement> locateSelenium(String element) {

        List<WebElement> we = new ArrayList<WebElement>();
        we = this.getDriver().findElements(By.id(element));
        if (we.size() == 0) {
            we = this.getDriver().findElements(By.name(element));
            if (we.size() == 0) {
                we = this.getDriver().findElements(By.xpath(element));
            }
        }

        return we;
    }

    /**
     * Looks for webelements inside a selenium context. This search will be made by a css selector using
     * {@code attribute} and {@code value} value
     * 
     * @param attribute
     * @param value
     */
    private List<WebElement> locateCssSelector(String attribute, String value) {
        List<WebElement> we = new ArrayList<WebElement>();
        we = this.getDriver().findElements(By.cssSelector("[" + attribute + "=\"" + value + "\"]"));

        return we;
    }

    /**
     * Replaces a placeholded element, starting with $$ with the corresponding java property
     * 
     * @param element
     */
    public String replacePlaceholders(String element) {
        String newVal = "";
        if (element.contains("${")) {
            String placeholder = element.substring(element.indexOf("${"), element.indexOf("}"));
            String modifier = "";
            String sysProp = "";
            if (placeholder.contains(".")) {
                sysProp = placeholder.substring(2, placeholder.indexOf("."));
                modifier = placeholder.substring(placeholder.indexOf(".") + 1, placeholder.length());
            } else {
                sysProp = placeholder.substring(2, placeholder.length());
            }

            newVal = System.getProperty(sysProp, "");
            if ("toLower".equals(modifier)) {
                return element.substring(0, element.indexOf("${")) + newVal.toLowerCase();
            } else if ("toUpper".equals(modifier)) {
                return element.substring(0, element.indexOf("${")) + newVal.toUpperCase();
            } else {
                return element.substring(0, element.indexOf("${")) + newVal;
            }
        }
        return element;
    }

    public String captureEvidence(WebDriver driver, String type) throws Exception {

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

                FileOutputStream fos = new FileOutputStream(fout, true);

                Writer out = new OutputStreamWriter(fos, "UTF8");
                PrintWriter writer = new PrintWriter(out, false);
                writer.append(source);
                writer.close();
                out.close();
            }

        } else if (type.equals("screenCapture")) {
            outputFile = outputFile + ".png";
            File file = null;
            driver.switchTo().defaultContent();
            ((Locatable) driver.findElement(By.tagName("body"))).getCoordinates().inViewPort();

            if (currentBrowser.startsWith("chrome") || currentBrowser.startsWith("droidemu")) {
                Actions actions = new Actions(driver);
                actions.keyDown(Keys.CONTROL).sendKeys(Keys.HOME).perform();

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

    private File adjustLastCapture(Integer newTrailingImageHeight, List<File> capture) throws IOException {
        // cuts last image just in case it dupes information
        Integer finalHeight = 0;
        Integer finalWidth = 0;

        File trailingImage = capture.get(capture.size() - 1);
        capture.remove(capture.size() - 1);

        BufferedImage oldTrailingImage = ImageIO.read(trailingImage);
        BufferedImage newTrailingImage = new BufferedImage(oldTrailingImage.getWidth(), oldTrailingImage.getHeight()
                - newTrailingImageHeight, BufferedImage.TYPE_INT_RGB);

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

        File temp;

        temp = File.createTempFile("chromecap" + Long.toString(ts), ".png");
        temp.deleteOnExit();
        ImageIO.write(img, "png", temp);

        return temp;
    }

    private File chromeFullScreenCapture(WebDriver driver) throws IOException, InterruptedException {
        driver.switchTo().defaultContent();
        // scroll loop n times to get the whole page if browser is chrome
        ArrayList<File> capture = new ArrayList<File>();

        Boolean atBottom = false;
        Integer windowSize = ((Long) ((JavascriptExecutor) driver)
                .executeScript("return document.documentElement.clientHeight")).intValue();

        Integer accuScroll = 0;
        Integer newTrailingImageHeight = 0;

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
        newTrailingImageHeight = accuScroll - getDocumentHeight(driver);
        return adjustLastCapture(newTrailingImageHeight, capture);
    }

    private Integer getDocumentHeight(WebDriver driver) {
        WebElement body = driver.findElement(By.tagName("html"));
        return body.getSize().getHeight();
    }
}
