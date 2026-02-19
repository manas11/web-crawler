package com.web.crawler.model;

public class CrawlRequest {

    private String url;

    public CrawlRequest() {
    }

    public CrawlRequest(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
