package com.stratio.qa.conditions;

import org.assertj.core.api.Condition;
import org.openqa.selenium.WebElement;

/**
 * Exception list class(Singleton).
 */
public enum Conditions {
    INSTANCE;

    private final Condition<WebElement> textFieldCondition = new TextFieldCondition().getCondition();

    public Condition<WebElement> getTextFieldCondition() {
        return textFieldCondition;
    }

}