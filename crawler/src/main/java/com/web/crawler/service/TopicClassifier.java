package com.web.crawler.service;


import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TopicClassifier {

    public List<String> classify(String text) {

        List<String> topics = new ArrayList<>();
        String lower = text.toLowerCase();

        if (lower.contains("toaster") || lower.contains("kitchen") || lower.contains("appliance")) {
            topics.add("Home Appliances");
        }

        if (lower.contains("ai") || lower.contains("technology") || lower.contains("software")) {
            topics.add("Technology");
        }

        if (lower.contains("news") || lower.contains("cnn")) {
            topics.add("News");
        }

        if (topics.isEmpty()) {
            topics.add("General");
        }

        return topics;
    }
}
