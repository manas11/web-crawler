package com.web.crawler.service;

import com.web.crawler.model.CrawlResponse;
import com.web.crawler.model.Metadata;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class CrawlerService {

    private final HtmlFetcher htmlFetcher;
    private final HtmlParser htmlParser;
    private final TopicClassifier topicClassifier;

    public CrawlerService(HtmlFetcher htmlFetcher,
                          HtmlParser htmlParser,
                          TopicClassifier topicClassifier) {
        this.htmlFetcher = htmlFetcher;
        this.htmlParser = htmlParser;
        this.topicClassifier = topicClassifier;
    }

    public CrawlResponse crawl(String url) {

        try {
            String html = htmlFetcher.fetch(url);

            Metadata metadata = htmlParser.parse(html);

            List<String> topics =
                    topicClassifier.classify(metadata.getTitle() + " " + metadata.getBody());

            return new CrawlResponse(
                    url,
                    metadata.getTitle(),
                    metadata.getDescription(),
                    topics
            );

        } catch (IOException e) {
            throw new RuntimeException("Error while crawling URL: " + url, e);
        }
    }
}
