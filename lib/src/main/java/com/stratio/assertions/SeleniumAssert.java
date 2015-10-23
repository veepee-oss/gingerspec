package com.stratio.assertions;

import java.util.List;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Condition;
import org.assertj.core.internal.Strings;
import org.assertj.core.internal.Integers;
import org.assertj.core.internal.Conditions;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.stratio.specs.CommonG;
import com.stratio.tests.utils.PreviousWebElements;

public class SeleniumAssert extends AbstractAssert<SeleniumAssert, Object> {
    
    	private CommonG commonspec;
    
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

	public SeleniumAssert(CommonG commong, WebDriver actual) {
	    super(actual, SeleniumAssert.class);
	    this.commonspec = commong;
	}
	
	public SeleniumAssert(CommonG commong, WebElement actual) {
	    super(actual, SeleniumAssert.class);
	    this.commonspec = commong;
	}
	
	public SeleniumAssert(CommonG commong, List<WebElement> actual) {
	    super(actual, SeleniumAssert.class);
	    this.commonspec = commong;
	}
	
	public SeleniumAssert(CommonG commong, PreviousWebElements actual) {
	    super(actual, SeleniumAssert.class);
	    this.commonspec = commong;
	}
	
	
	public CommonG getCommonspec() {
	    return this.commonspec;
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

	
	public static SeleniumAssert assertThat(CommonG commong, WebDriver actual) {
	    return new SeleniumAssert(commong, actual);
	}
	
	public static SeleniumAssert assertThat(CommonG commong, PreviousWebElements actual) {
	    return new SeleniumAssert(commong, actual);
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
	

	public SeleniumAssert isTextField(Condition<WebElement> cond) {
	    if (actual instanceof List) {
		Conditions.instance().equals(cond);
	    }
	    return this;
	}
	
	
	public SeleniumAssert hasAtLeast(Integer size) {
	    if (actual instanceof List) {
		Integers.instance().assertGreaterThanOrEqualTo(info, ((List<WebElement>) actual).size(), size);
	    }
	    return this;
	}
	
	
//	public SeleniumAssert hasSize(Integer size) {
//	    if (actual instanceof List) {
//		Integers.instance().assertEqual(info, ((List<WebElement>) actual).size(), size);
//	    }
//	    return this;
//	}
	
	public SeleniumAssert hasSize(Integer size) {
	    if (actual instanceof PreviousWebElements) {
		Integers.instance().assertEqual(info, ((PreviousWebElements) actual).getPreviousWebElements().size(), size);
	    }
	    return this;
	}
	

}
