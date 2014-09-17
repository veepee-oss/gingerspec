package com.stratio.tests.utils;

public class HttpResponse {
    private int statusCode;
    private String response;

    public HttpResponse (Integer statusCode, String response) {
        this.statusCode = statusCode;
        this.response = response;
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

}