package com.web.crawler.service;

import com.web.crawler.model.Metadata;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Component
public class HtmlParser {

    private static final Logger log = LoggerFactory.getLogger(HtmlParser.class);

    // Configurable timeout values (can be set in application.properties)
    @Value("${crawler.timeout.connect:10000}")
    private int connectTimeoutMs;

    @Value("${crawler.timeout.read:15000}")
    private int readTimeoutMs;

    @Value("${crawler.max.body.length:5000}")
    private int maxBodyLength;

    @Value("${crawler.retry.attempts:3}")
    private int maxRetryAttempts;

    @Value("${crawler.retry.delay.ms:2000}")
    private long retryDelayMs;

    // Standard user agent to avoid being blocked
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/120.0.0.0 Safari/537.36";

    /**
     * Parses HTML content from a given URL with retry mechanism and detailed logging.
     *
     * @param url the URL to parse
     * @return Metadata object containing title, description, and body text
     * @throws IOException if all retry attempts fail
     */
    public Metadata parseFromUrl(String url) throws IOException {
        log.info("Starting HTML parsing for URL: {}", url);
        log.debug("Configured timeouts - Connect: {}ms, Read: {}ms, Max retries: {}",
                connectTimeoutMs, readTimeoutMs, maxRetryAttempts);

        IOException lastException = null;

        // Retry loop to handle transient failures
        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                log.debug("Attempt {}/{} to fetch URL: {}", attempt, maxRetryAttempts, url);

                // Fetch document with configured timeouts
                Document doc = Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .timeout(connectTimeoutMs + readTimeoutMs)  // Total timeout
                        .followRedirects(true)
                        .ignoreHttpErrors(false)  // Throw exception on HTTP errors
                        .get();

                log.info("Successfully fetched URL: {} (attempt {})", url, attempt);

                // Extract metadata
                String title = doc.title();
                log.debug("Page title: {}", title);

                String description = doc.select("meta[name=description]").attr("content");
                log.debug("Meta description extracted: {} characters",
                        description != null ? description.length() : 0);

                String body = doc.body() != null ? doc.body().text() : "";
                log.debug("Body text extracted: {} characters (before truncation)", body.length());

                // Truncate body if too long
                if (body.length() > maxBodyLength) {
                    log.warn("Body text exceeds max length ({}). Truncating from {} to {} characters",
                            maxBodyLength, body.length(), maxBodyLength);
                    body = body.substring(0, maxBodyLength);
                }

                log.info("HTML parsing completed successfully for: {}", url);
                return new Metadata(title, description, body);

            } catch (SocketTimeoutException e) {
                // Timeout specific error
                lastException = e;
                log.warn("Socket timeout on attempt {}/{} for URL: {} (timeout: {}ms)",
                        attempt, maxRetryAttempts, url, (connectTimeoutMs + readTimeoutMs), e);

                if (attempt < maxRetryAttempts) {
                    log.info("Waiting {}ms before retry...", retryDelayMs);
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        log.error("Retry delay interrupted", ie);
                        Thread.currentThread().interrupt();
                    }
                }

            } catch (ConnectException e) {
                // Connection refused or blocked
                lastException = e;
                log.warn("Connection error on attempt {}/{} for URL: {} - Website may be blocking requests",
                        attempt, maxRetryAttempts, url, e);

                if (attempt < maxRetryAttempts) {
                    long delayMs = retryDelayMs * attempt;  // Exponential backoff
                    log.info("Exponential backoff: waiting {}ms before retry...", delayMs);
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        log.error("Retry delay interrupted", ie);
                        Thread.currentThread().interrupt();
                    }
                }

            } catch (IOException e) {
                // Other IO errors
                lastException = e;
                log.warn("IO error on attempt {}/{} for URL: {} - {}",
                        attempt, maxRetryAttempts, url, e.getMessage(), e);

                if (attempt < maxRetryAttempts) {
                    log.info("Waiting {}ms before retry...", retryDelayMs);
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        log.error("Retry delay interrupted", ie);
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        // All retries exhausted
        log.error("Failed to fetch URL after {} attempts: {}", maxRetryAttempts, url);
        log.error("Last error: {}", lastException != null ? lastException.getMessage() : "Unknown");
        throw lastException != null ? lastException :
                new IOException("Failed to fetch URL: " + url + " after " + maxRetryAttempts + " attempts");
    }
}