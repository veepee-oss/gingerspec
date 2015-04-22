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
     * Searchs for two webelements dragging the first one to the second
     * 
     * @param source
     * @param destination
     */
    @When("^I drag '([^:]*?):([^:]*?)' and drop it to '([^:]*?):([^:]*?)'$")
    public void seleniumDrag(String smethod, String source, String dmethod, String destination) {
        commonspec.getLogger().info("Dragging element");

        Actions builder = new Actions(commonspec.getDriver());

        List<WebElement> sourceElement = commonspec.locateElement(smethod, source);
        List<WebElement> destinationElement = commonspec.locateElement(dmethod, destination);

        builder.dragAndDrop(sourceElement.get(0), destinationElement.get(0)).perform();
    }

    /**
     * Click on an numbered {@code url} previously found element.
     * 
     * @param index
     */
    @When("^I click on the element on index '(.*?)'$")
    public void seleniumClick(Integer index) {
        commonspec.getLogger().info("Clicking on element with index {}", index);

        assertThat(commonspec.getPreviousWebElements()).isNotEmpty();
        commonspec.getPreviousWebElements().get(index).click();
    }

    /**
     * Type on an numbered {@code url} previously found element.
     * 
     * @param index
     */
    @When("^I type '(.*?)' on the element on index '(.*?)'$")
    public void seleniumType(String text, Integer index) {
        commonspec.getLogger().info("Typing on element with index {}", index);

        String newText = commonspec.replacePlaceholders(text);

        assertThat(commonspec.getPreviousWebElements()).isNotEmpty();
        while (newText.length() > 0) {
            if (-1 == newText.indexOf("\\n")) {
                commonspec.getPreviousWebElements().get(index).sendKeys(newText);
                newText = "";
            } else {
                commonspec.getPreviousWebElements().get(index).sendKeys(newText.substring(0, newText.indexOf("\\n")));
                commonspec.getPreviousWebElements().get(index).sendKeys(Keys.ENTER);
                newText = newText.substring(newText.indexOf("\\n") + 2);
            }
        }
    }

    /**
     * Choose an option from a select webelement found previously
     * 
     * @param option
     * @param index
     */
    @When("^I select '(.*?)' on the element on index '(.*?)'$")
    public void elementSelect(String option, Integer index) {
        commonspec.getLogger().info("Choosing option on select");
        String opt = commonspec.replacePlaceholders(option);

        Select sel = null;
        sel = new Select(commonspec.getPreviousWebElements().get(index));

        sel.selectByVisibleText(opt);
    }

    /**
     * Choose no option from a select webelement found previously
     * 
     * @param option
     * @param index
     */
    @When("^I de-select every item on the element on index '(.*?)'$")
    public void elementDeSelect(Integer index) {
        commonspec.getLogger().info("Unselecting everything");

        Select sel = null;
        sel = new Select(commonspec.getPreviousWebElements().get(index));

        if (sel.isMultiple()) {
            sel.deselectAll();
        }
    }
}
