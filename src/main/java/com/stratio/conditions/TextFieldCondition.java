package com.stratio.conditions;

import org.assertj.core.api.Condition;
import org.openqa.selenium.WebElement;

public class TextFieldCondition {
    
    private Condition<WebElement> cond;
    
    public TextFieldCondition() {
	cond = new Condition<WebElement>("textField") {
	    @Override
	    public boolean matches(WebElement value) {
		switch (value.getTagName()) {
			case "input":
			    return ("text".equals(value.getAttribute("type")) || "input".equals(value.getAttribute("type")));
			case "textarea":
			    return ("text".equals(value.getAttribute("type")) || "textarea".equals(value.getAttribute("type")));
			default:
			    return false;			       
		}
	    }
	};
    }
    
    public Condition<WebElement> getCondition() {
	return cond;
    }
    
}