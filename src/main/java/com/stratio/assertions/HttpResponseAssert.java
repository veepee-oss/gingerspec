package com.stratio.assertions;

import org.apache.http.util.EntityUtils;
import org.assertj.core.api.AbstractAssert;

import com.stratio.tests.utils.HttpResponse;
import org.json.JSONArray;


public class HttpResponseAssert extends
		AbstractAssert<HttpResponseAssert, HttpResponse> {

	/**
	 * Generic constructor.
	 * 
	 * @param actual
	 */
	public HttpResponseAssert(HttpResponse actual) {
		super(actual, HttpResponseAssert.class);
	}

	/**
	 * Checks the actual "http" response.
	 * 
	 * @param actual
	 * @return HttpResponseAssert
	 */
	public static HttpResponseAssert assertThat(HttpResponse actual) {
		return new HttpResponseAssert(actual);
	}

	/**
	 * Checks if a HttpResponse has an specific status.
	 * 
	 * @param status
	 * @return HttpResponseAssert
	 */
	public HttpResponseAssert hasStatusCode(Integer status) {
		if (actual.getStatusCode() != status) {
			failWithMessage(
					"Expected response status code to be <%s> but was <%s>",
					status, actual.getStatusCode());
		}
		return this;
	}

	/**
	 * Checks if a HttpResponse has not an specific status.
	 * 
	 * @param status
	 * @return HttpResponseAssert
	 */
	public HttpResponseAssert doesNotHaveStatusCode(Integer status) {
		if (actual.getStatusCode() == status) {
			failWithMessage(
					"Expected response status code not to be <%s> but was <%s>",
					status, actual.getStatusCode());
		}
		return this;
	}

	/**
	 * Checks if a HttpResponse has a specific message.
	 * 
	 * @param message
	 * @return HttpResponseAssert
	 */
	public HttpResponseAssert hasMessage(String message) {

		if (!actual.getResponse().contains(message)) {
			failWithMessage(
					"Expected response message to contain <%s> but was <%s>",
					message, actual.getResponse());
		}
		return this;
	}

	/**
	 * Checks if a HttpResponse has not a specific message.
	 * 
	 * @param message
	 * @return HttpResponseAssert
	 */
	public HttpResponseAssert doesNotHaveMessage(String message) {

		if (actual.getResponse().contains(message)) {
			failWithMessage(
					"Expected response message not to contain <%s> but was <%s>",
					message, actual.getResponse());
		}
		return this;
	}

	/**
	 * Checks if a HttpResponse has a specific message and has a specific
	 * status.
	 * 
	 * @param status
	 * @param message
	 * @return HttpResponseAssert
	 */
	public HttpResponseAssert hasStatusCodeAndMessage(Integer status,
			String message) {

		String msg = "";
		if (actual.getStatusCode() != status) {
			msg += String.format(
					"Expected response status code to be <%s> but was <%s>",
					status, actual.getStatusCode());
		}
		if (!actual.getResponse().contains(message)) {
			String nl = "";
			if (!"".equals(msg)) {
				nl = String.format("%s%26s", System.lineSeparator(), " ");
			}
			msg += String.format(
					"%sExpected response message to contain <%s> but was <%s>",
					nl, message, actual.getResponse());
		}
		if (!"".equals(msg)) {
			failWithMessage(msg);
		}
		return this;
	}

	/**
	 * Checks if a HttpResponse has not a specific message and has not a
	 * specific status.
	 * 
	 * @param status
	 * @param message
	 * @return HttpResponseAssert
	 */
	public HttpResponseAssert doesNotHaveStatusCodeNorMessage(Integer status,
			String message) {

		String msg = "";
		if (actual.getStatusCode() == status) {
			msg += String
					.format("Expected response status code not to be <%s> but was <%s>",
							status, actual.getStatusCode());
		}
		if (actual.getResponse().contains(message)) {
			String nl = "";
			if (!"".equals(msg)) {
				nl = String.format("%s%26s", System.lineSeparator(), " ");
			}
			msg += String
					.format("%sExpected response message not to contain <%s> but was <%s>",
							nl, message, actual.getResponse());
		}
		if (!"".equals(msg)) {
			failWithMessage(msg);
		}
		return this;
	}
	
	/**
	 * Checks if a HttpResponse has a specific message and has a specific length
	 * 
	 * @param status
	 * @param length
	 * @return HttpResponseAssert
	 */
	public HttpResponseAssert hasStatusCodeAndLength(Integer status,
			Integer length) {

		String msg = "";
		if (actual.getStatusCode() != status) {
			msg += String.format(
					"Expected response status code to be <%s> but was <%s>",
					status, actual.getStatusCode());
		}
		
		JSONArray bodyJson  = new JSONArray(actual.getResponse());
		
		if (bodyJson.length() != length) {
		    String nl = "";
		    if (!"".equals(msg)) {
			nl = String.format("%s%26s", System.lineSeparator(), " ");
		    }
		    msg += String.format(
				"%sExpected response length to be <%s> but was <%s>",
				nl, length, bodyJson.length());
		}
		
		if (!"".equals(msg)) {
			failWithMessage(msg);
		}
		return this;
	}
}
