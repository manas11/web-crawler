package com.web.crawler.model;

public class ErrorResponse {

    private String url;
    private String error;

    public ErrorResponse(String url, String error) {
        this.url = url;
        this.error = error;
    }

    public String getUrl() { return url; }
    public String getError() { return error; }
}