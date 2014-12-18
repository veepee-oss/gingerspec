package com.stratio.specs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

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
     * 
     * @param spec
     */
    public WhenGSpec(CommonG spec) {
        this.commonspec = spec;
    }

    /**
     * Wait seconds.
     * 
     * @param seconds
     * @throws InterruptedException
     */
    @When("^I wait '(.*?)' seconds?$")
    public void idleWait(Integer seconds) throws InterruptedException {
        commonspec.getLogger().info("Idling a while");
        Thread.sleep(seconds * DEFAULT_TIMEOUT);
    }

    /**
     * Search for a web element and click on it. Search can be conducted via id, name, xpath expression or css selector.
     * selectors are expected to be in attribute:value format
     * 
     * @param zone
     * @param target
     */
    @When("^I click on '([^:]*?):([^:]*?)'")
    public void searchAndClick(String method, String element) {
        String elem = commonspec.replacePlaceholders(element);
        commonspec.getLogger().info("Clicking at {}", elem);
        List<WebElement> wel = commonspec.locateElement(method, elem);

        assertThat(wel).as("No element found with locator " + element).isNotEmpty();

        wel.get(0).click();
    }

    /**
     * Search for a web element and type on it. Search can be conducted via id, name, xpath expression or css selector.
     * selectors are expected to be in attribute:value format
     * 
     * @param text
     * @param target
     */
    @When("^I type '(.*?)' at '([^:]*?):([^:]*?)'")
    public void searchAndType(String text, String method, String element) {
        String newText = commonspec.replacePlaceholders(text);
        commonspec.getLogger().info("Typing {} on {}", newText, element);
        List<WebElement> wel = commonspec.locateElement(method, element);

        while (newText.length() > 0) {
            if (-1 == newText.indexOf("\\n")) {
                wel.get(0).sendKeys(newText);
                newText = "";
            } else {
                wel.get(0).sendKeys(newText.substring(0, newText.indexOf("\\n")));
                wel.get(0).sendKeys(Keys.ENTER);
                newText = newText.substring(newText.indexOf("\\n") + 2);
            }
        }
    }

    /**
     * Search for a select web element and type on it. Search can be conducted via id, name, xpath expression or css
     * selector. selectors are expected to be in attribute:value format
     * 
     * @param text
     * @param target
     */
    @When("^I select '(.*?)' on '([^:]*?):([^:]*?)'$")
    public void elementSelect(String option, String method, String element) {
        commonspec.getLogger().info("Choosing option on select");
        String opt = commonspec.replacePlaceholders(option);
        List<WebElement> wel = commonspec.locateElement(method, element);

        Select sel = null;
        sel = new Select(wel.get(0));

        if (sel.isMultiple()) {
            sel.deselectAll();
        }
        sel.selectByVisibleText(opt);
    }

    /**
     * Searchs for two webelements dragging the first one to the second
     * 
     * @param source
     * @param destination
     */
    @When("^I drag '([^:]*?):([^:]*?)' and drop it to '([^:]*?):([^:]*?)'$")
    public void dragElement(String smethod, String source, String dmethod, String destination) {
        commonspec.getLogger().info("Dragging element");

        Actions builder = new Actions(commonspec.getDriver());

        List<WebElement> sourceElement = commonspec.locateElement(smethod, source);
        List<WebElement> destinationElement = commonspec.locateElement(dmethod, destination);

        builder.dragAndDrop(sourceElement.get(0), destinationElement.get(0)).perform();
    }
}
