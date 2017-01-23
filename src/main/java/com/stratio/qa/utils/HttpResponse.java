package com.stratio.qa.utils;

import com.ning.http.client.cookie.Cookie;

import java.util.List;

public class HttpResponse {
    private int statusCode;
    private String response;
    private List<Cookie> cookies;

    /**
     * Constructor of an HttpResponse.
     *
     * @param statusCode
     * @param response
     */
    public HttpResponse(Integer statusCode, String response, List<Cookie> cookies) {
        this.statusCode = statusCode;
        this.response = response;
        this.setCookies(cookies);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int status) {
        this.statusCode = status;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }

}