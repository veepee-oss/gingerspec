package com.stratio.assertions;

import java.util.List;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.internal.Strings;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SeleniumAssert extends AbstractAssert<SeleniumAssert, Object> {
	/**
	 * Constructor with WebElement.
	 * 
	 * @param actual
	 */
	public SeleniumAssert(WebElement actual) {
		super(actual, SeleniumAssert.class);
	}

	/**
	 * Constructor with a list of WebElements.
	 * 
	 * @param actual
	 */
	public SeleniumAssert(List<WebElement> actual) {
		super(actual, SeleniumAssert.class);
	}

	/**
	 * Constructor with WebDriver.
	 * 
	 * @param actual
	 */
	public SeleniumAssert(WebDriver actual) {
		super(actual, SeleniumAssert.class);
	}

	/**
	 * Checks a selenium WebElement.
	 * 
	 * @param actual
	 * @return SeleniumAssert
	 */
	public static SeleniumAssert assertThat(WebElement actual) {
		return new SeleniumAssert(actual);
	}

	/**
	 * Checks a selenium a list of WebElements.
	 * 
	 * @param actual
	 * @return SeleniumAssert
	 */
	public static SeleniumAssert assertThat(List<WebElement> actual) {
		return new SeleniumAssert(actual);
	}

	/**
	 * Checks a selenium WebDriver.
	 * 
	 * @param actual
	 * @return SeleniumAssert
	 */
	public static SeleniumAssert assertThat(WebDriver actual) {
		return new SeleniumAssert(actual);
	}

	/**
	 * Checks if a webDriver or WebElement has values.
	 * 
	 * @param values
	 * @return SeleniumAssert
	 */
	public SeleniumAssert contains(CharSequence... values) {
		if (actual instanceof WebDriver) {
			Strings.instance().assertContains(info,
					((WebDriver) actual).getPageSource(), values);
		} else if (actual instanceof WebElement) {
			Strings.instance().assertContains(info,
					((WebElement) actual).getText(), values);
		}
		return this;
	}
}
