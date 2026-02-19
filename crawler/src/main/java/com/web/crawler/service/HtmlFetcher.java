package com.web.crawler.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
public class HtmlFetcher {

    private final OkHttpClient client;

    public HtmlFetcher() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5))
                .build();
    }

    public String fetch(String url) throws IOException {

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "WebCrawler/1.0")
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to fetch page. HTTP Code: " + response.code());
            }

            return response.body().string();
        }
    }
}
