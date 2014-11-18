package com.stratio.tests.utils.matchers;

import org.hamcrest.Factory;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import com.stratio.tests.utils.HttpResponse;

/**
 * @author Javier Delgado
 * @author Hugo Dominguez
 * 
 */
public final class HTTPRequestMatcher {
    /**
     * Default constructor.
     */
    private HTTPRequestMatcher() {
    }
    /**
     * Checks if a httpResponse has an specific status.
     * @param statusMatcher
     * @return Matcher<HttpResponse>
     */
    @Factory
    public static Matcher<HttpResponse> hasStatus(final Matcher<Integer> statusMatcher) {
        return new FeatureMatcher<HttpResponse, Integer>(statusMatcher, "Requests with code", "code") {
            @Override
            protected Integer featureValueOf(HttpResponse actual) {
                return actual.getStatusCode();
            }
        };
    }
    /**
     * Checks if a httpResponse has a specific message. 
     * @param messageMatcher
     * @return
     */
    @Factory
    public static Matcher<HttpResponse> hasMessage(final Matcher<String> messageMatcher) {

        return new FeatureMatcher<HttpResponse, String>(messageMatcher, "Requests with message", "message") {
            @Override
            protected String featureValueOf(HttpResponse actual) {
                return actual.getResponse();
            }
        };
    }
}