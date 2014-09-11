package com.stratio.tests.utils;

public class HttpResponse {
    private Integer statusCode;
    private String response;

    public HttpResponse (Integer statusCode, String response) {
        this.statusCode = statusCode;
        this.response = response;
    }
    
    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer status) {
        this.statusCode = status;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

}