package com.stratio.tests.utils.matchers;

import java.util.regex.Pattern;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * @author Javier Delgado
 * @author Hugo Dominguez
 * 
 */
public class ExceptionMatcher extends TypeSafeMatcher<Exception> {
    public static final int VALUE = 3;
    private final String clazz;
    private final Pattern messagePattern;

    /**
     * Checks if an exception is throw by an expecific class and the message achieve to the regular expresion.
     * 
     * @param clazz
     * @param regex
     * @return
     */
    @Factory
    public static Matcher<Exception> hasClassAndMessage(String clazz, String regex) {
        return hasClassAndMessage(clazz, Pattern.compile(".*?" + regex + ".*?"));
    }

    /**
     * Checks if an exception is throw by an expecific class and the message is similar to messagePattern.
     * 
     * @param clazz
     * @param messagePattern
     * @return
     */
    @Factory
    public static Matcher<Exception> hasClassAndMessage(String clazz, Pattern messagePattern) {
        return new ExceptionMatcher(clazz, messagePattern);
    }

    /**
     * Constructor.
     * 
     * @param clazz
     * @param messagePattern
     */
    public ExceptionMatcher(String clazz, Pattern messagePattern) {
        this.clazz = clazz;
        this.messagePattern = messagePattern;
    }

    /**
     * This message is show when the matcher fails.
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

    /**
     * Checks if the exceptions are equals.
     */
    @Override
    protected boolean matchesSafely(Exception item) {
        return item.getClass().getSimpleName().equals(clazz) && messagePattern.matcher(item.getMessage()).matches();
    }
}