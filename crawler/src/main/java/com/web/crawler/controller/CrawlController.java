package com.web.crawler.controller;

import com.web.crawler.model.CrawlRequest;
import com.web.crawler.model.CrawlResponse;
import com.web.crawler.service.CrawlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class CrawlController {

    private static final Logger log = LoggerFactory.getLogger(CrawlController.class);

    private final CrawlerService crawlerService;

    public CrawlController(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    /**
     * REST endpoint to crawl a website.
     * Accepts a POST request with a URL in the request body.
     *
     * @param request CrawlRequest containing the URL to crawl
     * @return CrawlResponse with extracted metadata and topics
     * @throws IOException if crawling fails
     */
    @PostMapping("/crawl")
    public CrawlResponse crawl(@RequestBody CrawlRequest request) throws IOException {
        long startTimeMs = System.currentTimeMillis();
        String url = request.getUrl();

        log.info("HTTP POST /api/crawl received");
        log.info("Request URL: {}", url);

        try {
            CrawlResponse response = crawlerService.crawl(url);

            long durationMs = System.currentTimeMillis() - startTimeMs;
            log.info("HTTP response sent successfully in {}ms", durationMs);

            return response;

        } catch (IllegalArgumentException e) {
            log.error("Invalid URL provided: {} | Error: {}", url, e.getMessage());
            throw e;
        } catch (IOException e) {
            long durationMs = System.currentTimeMillis() - startTimeMs;
            log.error("Failed to process request in {}ms | URL: {} | Error: {}",
                    durationMs, url, e.getMessage());
            throw e;
        }
    }
}