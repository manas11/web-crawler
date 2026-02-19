package com.web.crawler.service;

import com.web.crawler.model.CrawlResponse;
import com.web.crawler.model.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Service
public class CrawlerService {

    private static final Logger log = LoggerFactory.getLogger(CrawlerService.class);

    private final HtmlParser htmlParser;
    private final TopicClassifier topicClassifier;

    // Request tracking metrics
    private long lastRequestTimeMs = 0;
    private static final long MIN_REQUEST_INTERVAL_MS = 2000;  // Minimum 2 seconds between requests

    public CrawlerService(HtmlParser htmlParser, TopicClassifier topicClassifier) {
        this.htmlParser = htmlParser;
        this.topicClassifier = topicClassifier;
        log.info("CrawlerService initialized with min request interval: {}ms", MIN_REQUEST_INTERVAL_MS);
    }

    /**
     * Main crawl method that orchestrates the crawling process.
     * Includes URL validation, rate limiting, and comprehensive error logging.
     *
     * @param url the URL to crawl
     * @return CrawlResponse containing extracted metadata and topics
     * @throws IOException if the URL cannot be fetched
     * @throws IllegalArgumentException if the URL format is invalid
     */
    public CrawlResponse crawl(String url) throws IOException {
        long startTimeMs = System.currentTimeMillis();
        log.info("=== CRAWL REQUEST STARTED ===");
        log.info("Target URL: {}", url);

        try {
            // 1. VALIDATION STEP
            validateUrl(url);
            log.debug("URL validation passed");

            // 2. RATE LIMITING STEP
            applyRateLimiting();
            log.debug("Rate limiting applied");

            // 3. PARSING STEP
            log.info("Starting HTML parsing for URL: {}", url);
            Metadata metadata = htmlParser.parseFromUrl(url);
            log.info("HTML parsing completed successfully");

            // 4. TOPIC CLASSIFICATION STEP
            log.info("Classifying content topics");
            String combinedText = (metadata.getTitle() != null ? metadata.getTitle() : "") + " " +
                                 (metadata.getBody() != null ? metadata.getBody() : "");
            List<String> topics = topicClassifier.classify(combinedText);
            log.info("Topics classified: {}", topics);

            // 5. BUILD RESPONSE
            CrawlResponse response = new CrawlResponse(
                    url,
                    metadata.getTitle(),
                    metadata.getDescription(),
                    topics
            );

            long durationMs = System.currentTimeMillis() - startTimeMs;
            log.info("=== CRAWL REQUEST COMPLETED ===");
            log.info("Duration: {}ms | Title: {} | Topics: {}",
                    durationMs, metadata.getTitle(), topics);

            return response;

        } catch (IllegalArgumentException e) {
            log.error("URL validation failed for: {} | Error: {}", url, e.getMessage());
            throw e;
        } catch (IOException e) {
            long durationMs = System.currentTimeMillis() - startTimeMs;
            log.error("Failed to crawl URL: {} | Duration: {}ms | Error: {}",
                    url, durationMs, e.getMessage(), e);

            // Provide specific guidance based on error type
            if (e.getMessage().contains("timeout")) {
                log.error("SUGGESTION: Website is slow to respond. Consider increasing timeout values in application.properties");
            } else if (e.getMessage().contains("Connection")) {
                log.error("SUGGESTION: Website is blocking requests. Try: 1) Increasing delay between requests, 2) Using different user agent, 3) Adding request headers");
            }
            throw e;
        }
    }

    /**
     * Validates the URL format before attempting to fetch.
     *
     * @param url the URL to validate
     * @throws IllegalArgumentException if the URL format is invalid
     */
    private void validateUrl(String url) {
        log.debug("Validating URL: {}", url);

        if (url == null || url.trim().isEmpty()) {
            log.error("URL is null or empty");
            throw new IllegalArgumentException("URL cannot be null or empty");
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            log.error("URL does not start with http/https: {}", url);
            throw new IllegalArgumentException("URL must start with http:// or https://");
        }

        try {
            new URL(url);
            log.debug("URL format is valid");
        } catch (MalformedURLException e) {
            log.error("URL is malformed: {} | Error: {}", url, e.getMessage());
            throw new IllegalArgumentException("Invalid URL format: " + e.getMessage());
        }
    }

    /**
     * Applies rate limiting to avoid overwhelming target websites.
     * Prevents requests that are too close together in time.
     */
    private void applyRateLimiting() {
        long currentTimeMs = System.currentTimeMillis();
        long timeSinceLastRequestMs = currentTimeMs - lastRequestTimeMs;

        if (timeSinceLastRequestMs < MIN_REQUEST_INTERVAL_MS) {
            long delayMs = MIN_REQUEST_INTERVAL_MS - timeSinceLastRequestMs;
            log.info("Rate limiting active: waiting {}ms before next request", delayMs);
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                log.warn("Rate limiting sleep interrupted", e);
                Thread.currentThread().interrupt();
            }
        }

        lastRequestTimeMs = System.currentTimeMillis();
        log.debug("Rate limiting check completed");
    }
}