package com.web.crawler.controller;

import com.web.crawler.model.CrawlRequest;
import com.web.crawler.model.CrawlResponse;
import com.web.crawler.service.CrawlerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CrawlController {

    private final CrawlerService crawlerService;

    public CrawlController(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    @PostMapping("/crawl")
    public CrawlResponse crawl(@RequestBody CrawlRequest request) {
        return crawlerService.crawl(request.getUrl());
    }
}