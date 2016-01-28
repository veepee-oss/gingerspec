package com.stratio.tests.utils.matchers;

import java.util.List;
import java.util.regex.Pattern;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class ListLastElementExceptionMatcher extends TypeSafeMatcher<List<Exception>> {
    public static final int VALUE = 3;
    private final String clazz;
    private final Pattern messagePattern;

    /**
     * Checks if the last element has the class and the message(Regular expression).
     * 
     * @param clazz
     * @param regex
     * @return matcher
     */
    @Factory
    public static Matcher<List<Exception>> lastElementHasClassAndMessage(String clazz, String regex) {
        return lastElementHasClassAndMessage(clazz, Pattern.compile(".*?" + regex + ".*?"));
    }

    /**
     * Checks if the last element has the class and the message(Pattern).
     * 
     * @param clazz
     * @param messagePattern
     * @return matcher
     */
    @Factory
    public static Matcher<List<Exception>> lastElementHasClassAndMessage(String clazz, Pattern messagePattern) {
        return new ListLastElementExceptionMatcher(clazz, messagePattern);
    }

    public ListLastElementExceptionMatcher(String clazz, Pattern messagePattern) {
        this.clazz = clazz;
        this.messagePattern = messagePattern;
    }

    /**
     * Message showed in the case that a matcher fails.
     * 
     * @param description
     */
    public void describeTo(Description description) {

        String expectedMessage = String.valueOf(messagePattern);
        if (expectedMessage.startsWith(".*?")) {
            expectedMessage = expectedMessage.substring(VALUE);
        }
        if (expectedMessage.endsWith(".*?")) {
            expectedMessage = expectedMessage.substring(0, expectedMessage.length() - VALUE);
        }

        description.appendText("an exception with class \"").appendText(clazz).appendText("\"")
                .appendText(" and a message like  \"").appendText(expectedMessage).appendText("\"");
    }

    @Override
    protected boolean matchesSafely(List<Exception> items) {
        Exception item = items.get(items.size() - 1);
        return item.getClass().getSimpleName().equals(clazz) && messagePattern.matcher(item.getMessage()).matches();
    }
}