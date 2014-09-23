package com.stratio.assertions;

import org.assertj.core.api.AbstractAssert;

import com.stratio.tests.utils.HttpResponse;

public class HttpResponseAssert extends AbstractAssert<HttpResponseAssert, HttpResponse> {

    public HttpResponseAssert(HttpResponse actual) {
        super(actual, HttpResponseAssert.class);
    }

    public static HttpResponseAssert assertThat(HttpResponse actual) {
        return new HttpResponseAssert(actual);
    }

    public HttpResponseAssert hasStatusCode(Integer status) {
        if (actual.getStatusCode() != status) {
            failWithMessage("Expected response status code to be <%s> but was <%s>", status, actual.getStatusCode());
        }
        return this;
    }

    public HttpResponseAssert doesNotHaveStatusCode(Integer status) {
        if (actual.getStatusCode() == status) {
            failWithMessage("Expected response status code not to be <%s> but was <%s>", status, actual.getStatusCode());
        }
        return this;
    }

    public HttpResponseAssert hasMessage(String message) {

        if (!actual.getResponse().contains(message)) {
            failWithMessage("Expected response message to contain <%s> but was <%s>", message, actual.getResponse());
        }
        return this;
    }

    public HttpResponseAssert doesNotHaveMessage(String message) {

        if (actual.getResponse().contains(message)) {
            failWithMessage("Expected response message not to contain <%s> but was <%s>", message, actual.getResponse());
        }
        return this;
    }

    public HttpResponseAssert hasStatusCodeAndMessage(Integer status, String message) {

        String msg = "";
        if (actual.getStatusCode() != status) {
            msg += String.format("Expected response status code to be <%s> but was <%s>", status,
                    actual.getStatusCode());
        }
        if (!actual.getResponse().contains(message)) {
            String nl = "";
            if (!"".equals(msg)) {
                nl = String.format("%s%26s", System.lineSeparator(), " ");
            }
            msg += String.format("%sExpected response message to contain <%s> but was <%s>", nl, message,
                    actual.getResponse());
        }
        if (!"".equals(msg)) {
            failWithMessage(msg);
        }
        return this;
    }

    public HttpResponseAssert doesNotHaveStatusCodeNorMessage(Integer status, String message) {

        String msg = "";
        if (actual.getStatusCode() == status) {
            msg += String.format("Expected response status code not to be <%s> but was <%s>", status,
                    actual.getStatusCode());
        }
        if (actual.getResponse().contains(message)) {
            String nl = "";
            if (!"".equals(msg)) {
                nl = String.format("%s%26s", System.lineSeparator(), " ");
            }
            msg += String.format("%sExpected response message not to contain <%s> but was <%s>", nl, message,
                    actual.getResponse());
        }
        if (!"".equals(msg)) {
            failWithMessage(msg);
        }
        return this;
    }

}
