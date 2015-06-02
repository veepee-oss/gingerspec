package com.stratio.specs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import com.stratio.cucumber.converter.ArrayListConverter;

import cucumber.api.Transform;
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
    @When("^I wait '(\\d+?)' seconds?$")
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

        List<WebElement> sourceElement = commonspec.locateElement(smethod, source, 1);
        List<WebElement> destinationElement = commonspec.locateElement(dmethod, destination, 1);

        builder.dragAndDrop(sourceElement.get(0), destinationElement.get(0)).perform();
    }

    /**
     * Click on an numbered {@code url} previously found element.
     * 
     * @param index
     */
    @When("^I click on the element on index '(\\d+?)'$")
    public void seleniumClick(Integer index) {
        commonspec.getLogger().info("Clicking on element with index {}", index);

        assertThat(commonspec.getPreviousWebElements()).isNotEmpty();
        commonspec.getPreviousWebElements().get(index).click();
    }

    /**
     * Type a {@code text} on an numbered {@code index} previously found element.
     * 
     * @param test
     * @param index
     */
    @When("^I type '(.+?)' on the element on index '(\\d+?)'$")
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
     * Send a {@code strokes} list on an numbered {@code url} previously found element. strokes examples are "HOME, END"
     * or "END, SHIFT + HOME, DELETE". Each element in the stroke list has to be an element from
     * {@link org.openqa.selenium.Keys} (NULL, CANCEL, HELP, BACK_SPACE, TAB, CLEAR, RETURN, ENTER, SHIFT, LEFT_SHIFT,
     * CONTROL, LEFT_CONTROL, ALT, LEFT_ALT, PAUSE, ESCAPE, SPACE, PAGE_UP, PAGE_DOWN, END, HOME, LEFT, ARROW_LEFT, UP,
     * ARROW_UP, RIGHT, ARROW_RIGHT, DOWN, ARROW_DOWN, INSERT, DELETE, SEMICOLON, EQUALS, NUMPAD0, NUMPAD1, NUMPAD2,
     * NUMPAD3, NUMPAD4, NUMPAD5, NUMPAD6, NUMPAD7, NUMPAD8, NUMPAD9, MULTIPLY, ADD, SEPARATOR, SUBTRACT, DECIMAL,
     * DIVIDE, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, META, COMMAND, ZENKAKU_HANKAKU) , a plus sign (+), a
     * comma (,) or spaces ( )
     * 
     * @param strokes
     * @param index
     */
    @When("^I send '(.+?)' on the element on index '(\\d+?)'$")
    public void seleniumKeys(@Transform(ArrayListConverter.class) List<String> strokes, Integer index) {
        commonspec.getLogger().info("Sending keys on element with index {}", index);

        assertThat(commonspec.getPreviousWebElements()).isNotEmpty();
        assertThat(strokes).isNotEmpty();

        for (String stroke : strokes) {
            if (stroke.contains("+")) {
                List<Keys> csl = new ArrayList<Keys>();
                for (String strokeInChord : stroke.split("\\+")) {
                    csl.add(Keys.valueOf(strokeInChord.trim()));
                }                                
                Keys[] csa = csl.toArray(new Keys[csl.size()]);
                commonspec.getPreviousWebElements().get(index).sendKeys(csa);
            } else {
                commonspec.getPreviousWebElements().get(index).sendKeys(Keys.valueOf(stroke));
            }
        }
    }

    /**
     * Choose an @{code option} from a select webelement found previously
     * 
     * @param option
     * @param index
     */
    @When("^I select '(.+?)' on the element on index '(\\d+?)'$")
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
    @When("^I de-select every item on the element on index '(\\d+?)'$")
    public void elementDeSelect(Integer index) {
        commonspec.getLogger().info("Unselecting everything");

        Select sel = null;
        sel = new Select(commonspec.getPreviousWebElements().get(index));

        if (sel.isMultiple()) {
            sel.deselectAll();
        }
    }
}
