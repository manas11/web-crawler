package com.web.crawler.model;

import java.util.List;

public class CrawlResponse {

    private String url;
    private String title;
    private String description;
    private List<String> topics;

    public CrawlResponse(String url, String title, String description, List<String> topics) {
        this.url = url;
        this.title = title;
        this.description = description;
        this.topics = topics;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTopics() {
        return topics;
    }
}
