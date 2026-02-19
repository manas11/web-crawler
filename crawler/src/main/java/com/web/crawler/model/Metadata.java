package com.web.crawler.model;

public class Metadata {

    private String title;
    private String description;
    private String body;

    public Metadata(String title, String description, String body) {
        this.title = title;
        this.description = description;
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getBody() {
        return body;
    }
}
