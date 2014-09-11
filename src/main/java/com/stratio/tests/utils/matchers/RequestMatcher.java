package com.stratio.tests.utils.matchers;

import org.hamcrest.Factory;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import com.stratio.tests.utils.HttpResponse;

public class RequestMatcher {

    @Factory
    public static Matcher<HttpResponse> hasStatus(
            final Matcher<Integer> statusMatcher) {
        return new FeatureMatcher<HttpResponse, Integer>(statusMatcher,
                "Requests with code", "code") {
            @Override
            protected Integer featureValueOf(HttpResponse actual) {
                return actual.getStatusCode();
            }
        };
    }

    @Factory
    public static Matcher<HttpResponse> hasMessage(
            final Matcher<String> messageMatcher) {

        return new FeatureMatcher<HttpResponse, String>(messageMatcher,
                "Requests with message", "message") {
            @Override
            protected String featureValueOf(HttpResponse actual) {
                return actual.getResponse();
            }
        };
    }
}