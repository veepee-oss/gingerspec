/*
 * Copyright (c) 2021, Veepee
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose
 * with or without fee is hereby  granted, provided that the above copyright notice
 * and this permission notice appear in all copies.
 *
 * THE SOFTWARE  IS PROVIDED "AS IS"  AND THE AUTHOR DISCLAIMS  ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING  ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS.  IN NO  EVENT  SHALL THE  AUTHOR  BE LIABLE  FOR  ANY SPECIAL,  DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 * OF USE, DATA  OR PROFITS, WHETHER IN AN ACTION OF  CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR  IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
*/

package com.privalia.qa.conditions;

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