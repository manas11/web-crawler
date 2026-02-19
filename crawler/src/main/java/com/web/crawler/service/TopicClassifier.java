package com.web.crawler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TopicClassifier {

    private static final Logger log = LoggerFactory.getLogger(TopicClassifier.class);

    /**
     * Classifies content into topics based on keyword matching.
     *
     * @param text the text content to classify
     * @return list of topics the content belongs to
     */
    public List<String> classify(String text) {
        log.debug("Starting topic classification for text with {} characters",
                text != null ? text.length() : 0);

        List<String> topics = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            log.warn("Text is null or empty, assigning default 'General' topic");
            topics.add("General");
            return topics;
        }

        String lower = text.toLowerCase();

        // Check for Home Appliances keywords
        if (lower.contains("toaster") || lower.contains("kitchen") || lower.contains("appliance")) {
            log.debug("Matched 'Home Appliances' keywords");
            topics.add("Home Appliances");
        }

        // Check for Technology keywords
        if (lower.contains("ai") || lower.contains("technology") || lower.contains("software")) {
            log.debug("Matched 'Technology' keywords");
            topics.add("Technology");
        }

        // Check for News keywords
        if (lower.contains("news") || lower.contains("cnn")) {
            log.debug("Matched 'News' keywords");
            topics.add("News");
        }

        // Assign default topic if nothing matched
        if (topics.isEmpty()) {
            log.debug("No specific topics matched, assigning default 'General'");
            topics.add("General");
        }

        log.info("Topic classification completed. Assigned topics: {}", topics);
        return topics;
    }
}