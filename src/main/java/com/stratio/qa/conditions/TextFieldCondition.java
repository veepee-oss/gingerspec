/*
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.qa.conditions;

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