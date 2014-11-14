package com.stratio.specs;

import static com.stratio.assertions.Assertions.assertThat;
import static com.stratio.assertions.SeleniumExtractor.linkText;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import cucumber.api.java.en.When;
/**
 * @author Javier Delgado
 * @author Hugo Dominguez
 *
 */
public class WhenGSpec extends BaseGSpec {
    
    public static final int DEFAULT_TIMEOUT = 1000;
    /**
     * Default constructor.
     * @param spec
     */
    public WhenGSpec(CommonG spec) {
        this.commonspec = spec;
    }
/**
 * Wait seconds.
 * @param seconds
 * @throws InterruptedException
 */
    @When("^I wait '(.*?)' seconds?$")
    public void idleWait(Integer seconds) throws InterruptedException {
        commonspec.getLogger().info("Idling a while");
        Thread.sleep(seconds * DEFAULT_TIMEOUT);
    }
/**
 * Action click.
 * @param zone
 * @param target
 */
    @When("^I click on a '(.*?)' '(.*?)'$")
    public void searchAndClick(String zone, String target) {
        commonspec.getLogger().info("Clicking on {}", target);
        WebElement z = commonspec.getDriver().findElement(By.id(zone));
        List<WebElement> anchors = z.findElements(By.linkText(target));

        assertThat(anchors).as("No WebElements found").extracting(linkText()).contains(target).hasSize(1);
        anchors.get(0).click();
    }
/**
 * Search and type.
 * @param value
 * @param target
 */
    @When("^I type '(.*?)' at '(.*?)'$")
    public void searchAndType(String value, String target) {
        commonspec.getLogger().info("Typing {} on {}", value, target);
        commonspec.getDriver().findElement(By.id(target)).sendKeys(value);
    }
}
