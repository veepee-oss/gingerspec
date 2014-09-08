package com.stratio.tests.utils.matchers;

import java.util.regex.Pattern;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class ExceptionMatcher extends TypeSafeMatcher<Exception> {

    private final String clazz;
    private final Pattern messagePattern;

    @Factory
    public static Matcher<Exception> hasClassAndMessage(String clazz,
            String regex) {
        return hasClassAndMessage(clazz, Pattern.compile(".*?" + regex + ".*?"));
    }

    @Factory
    public static Matcher<Exception> hasClassAndMessage(String clazz,
            Pattern messagePattern) {
        return new ExceptionMatcher(clazz, messagePattern);
    }

    public ExceptionMatcher(String clazz, Pattern messagePattern) {
        this.clazz = clazz;
        this.messagePattern = messagePattern;
    }

    public void describeTo(Description description) {

        String expectedMessage = String.valueOf(messagePattern);
        if (expectedMessage.startsWith(".*?")) {
            expectedMessage = expectedMessage.substring(3);
        }
        if (expectedMessage.endsWith(".*?")) {
            expectedMessage = expectedMessage.substring(0,
                    expectedMessage.length() - 3);
        }

        description.appendText("an exception with class \"").appendText(clazz)
                .appendText("\"").appendText(" and a message like  \"")
                .appendText(expectedMessage).appendText("\"");
    }

    @Override
    protected boolean matchesSafely(Exception item) {
        return item.getClass().getSimpleName().equals(clazz)
                && messagePattern.matcher(item.getMessage()).matches();
    }
}