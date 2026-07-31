package com.drowndreamer.aiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

public class WebScrapingTool {

    private static final int MAX_CONTENT_LENGTH = 2_000;

    @Tool(description = "Scrape the content of a web page")
    public String scrapeWebPage(@ToolParam(description = "URL of the web page to scrape") String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            String content = doc.body() == null ? doc.text() : doc.body().text();
            if (content.length() <= MAX_CONTENT_LENGTH) {
                return content;
            }
            return content.substring(0, MAX_CONTENT_LENGTH) + "\n[Content truncated]";
        } catch (IOException e) {
            return "Error scraping web page: " + e.getMessage();
        }
    }
}
