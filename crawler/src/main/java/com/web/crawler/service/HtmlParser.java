package com.web.crawler.service;


import com.web.crawler.model.Metadata;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class HtmlParser {

    public Metadata parse(String html) {

        Document doc = Jsoup.parse(html);

        String title = doc.title();
        String description = doc.select("meta[name=description]").attr("content");
        String body = doc.body() != null ? doc.body().text() : "";

        return new Metadata(title, description, body);
    }
}
