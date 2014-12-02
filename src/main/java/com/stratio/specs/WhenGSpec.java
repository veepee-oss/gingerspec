package com.stratio.specs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import java.util.List;

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
    @When("^I click on '(.*?)'")
    public void searchAndClick(String element) {
        String elem = commonspec.replacePlaceholders(element);
        commonspec.getLogger().info("Clicking at {}", element);
        List<WebElement> wel = commonspec.locateElement(element);

        if (wel.size() != 0) {
            wel.get(0).click();
        } else {
            fail("No element found with locator " + element);
        }
    }

    /**
     * Search for a web element and type on it. Search can be conducted via id, name, xpath expression or css selector.
     * selectors are expected to be in attribute:value format
     * 
     * @param text
     * @param target
     */
    @When("^I type '(.*?)' at '(.*?)'")
    public void searchAndType(String text, String element) {
        commonspec.getLogger().info("Typing {} on {}", text, element);
        String newText = commonspec.replacePlaceholders(text);
        List<WebElement> wel = commonspec.locateElement(element);

        assertThat(wel).as("No element found with locator " + element).isNotEmpty();

        wel.get(0).sendKeys(newText);
    }

    /**
     * Search for a select web element and type on it. Search can be conducted via id, name, xpath expression or css
     * selector. selectors are expected to be in attribute:value format
     * 
     * @param text
     * @param target
     */
    @When("^I select '(.*?)' on '(.*?)'$")
    public void elementSelect(String option, String element) {
        commonspec.getLogger().info("Choosing option on select");
        String opt = commonspec.replacePlaceholders(option);
        List<WebElement> wel = commonspec.locateElement(element);

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
    @When("^I drag '(.*?)' and drop it to '(.*?)'$")
    public void dragElement(String source, String destination) {
        commonspec.getLogger().info("Dragging element");
        
        Actions builder = new Actions(commonspec.getDriver());

        List<WebElement> sourceElement = commonspec.locateElement(source);
        List<WebElement> destinationElement = commonspec.locateElement(destination);

        builder.dragAndDrop(sourceElement.get(0), destinationElement.get(0)).perform();
    }
}
