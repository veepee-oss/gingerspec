package com.stratio.cucumber.aspects;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.remote.ScreenshotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stratio.assertions.SeleniumAssert;
import com.stratio.tests.utils.HashUtils;
import com.stratio.tests.utils.ThreadProperty;

@Aspect
public class SeleniumAspect {

    final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    @Pointcut("call(* com.stratio.assertions.SeleniumAssert.*(..))" + " || call(* org.openqa.selenium.*.click(..))"
            + " || call(* org.openqa.selenium.*.findElement(..))")
    protected void exceptionCallPointcut() {
    }

    @Around(value = "exceptionCallPointcut()")
    public Object aroundExceptionCalls(ProceedingJoinPoint pjp) throws Throwable {
        Object retVal = null;
        try {
            retVal = pjp.proceed();
            return retVal;
        } catch (Throwable ex) {
            WebDriver driver = null;           
            if (ex instanceof WebDriverException) {                
                logger.info("Got a selenium exception");
                if (!(pjp.getThis() instanceof WebDriver)) {
                    throw ex;    
                }
                driver = (WebDriver) pjp.getThis();
            } else if ((pjp.getTarget() instanceof SeleniumAssert) && (ex instanceof AssertionError)) {
                logger.info("Got a SeleniumAssert response");
                SeleniumAssert as = (SeleniumAssert) pjp.getTarget();
                Class<?> c = as.getClass().getSuperclass();
                Field actual = c.getDeclaredField("actual");
                actual.setAccessible(true);
                Object realActual = actual.get(as);

                if (realActual instanceof WebDriver) {
                    driver = (WebDriver) actual.get(as);
                } else if (realActual instanceof RemoteWebElement) {
                    driver = ((RemoteWebElement) actual.get(as)).getWrappedDriver();
                }
            }
            if (driver != null) {
                captureEvidence(driver, "framehtmlSource");
                captureEvidence(driver, "htmlSource");
                captureEvidence(driver, "screenCapture");
            } else {
                logger.info("Got no Selenium driver to capture a screen");
            }
            throw ex;
        }
    }

    private Integer getDocumentHeight(WebDriver driver) {
        WebElement body = driver.findElement(By.tagName("html"));
        return body.getSize().getHeight();
    }

    private File adjustLastCapture(Integer newTrailingImageHeight, ArrayList<File> capture) throws IOException {
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

        long ts = System.currentTimeMillis() / 1000L;

        File temp;

        temp = File.createTempFile("chromecap" + String.valueOf(ts), ".png");
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
            Thread.sleep(1500);
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

    private String captureEvidence(WebDriver driver, String type) throws Exception {

        String dir = "./target/executions/";

        String clazz = ThreadProperty.get("class");
        String currentBrowser = ThreadProperty.get("browser");
        String currentData = ThreadProperty.get("dataSet");

        if (!currentData.equals("")) {
            currentData = currentData.replaceAll("[\\\\|\\/|\\|\\s|:|\\*]", "_");
        }

        currentData = HashUtils.doHash(currentData);
        String outputFile = dir + clazz + "/" + currentBrowser + "-" + currentData;

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
                fout.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(fout, true);

                Writer out = new OutputStreamWriter(fos, "UTF8");
                PrintWriter writer = new PrintWriter(out, false);
                writer.append(source);
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
                e.printStackTrace();
            }
        }

        return outputFile;

    }
}