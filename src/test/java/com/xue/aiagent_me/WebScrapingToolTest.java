package com.xue.aiagent_me;

import com.xue.aiagent_me.tools.WebScrapingTool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WebScrapingToolTest {
    @Test
    void scrapeWebPage() {
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        String result = webScrapingTool.scrapeWebPage("https://www.codefather.cn");
        System.out.println("result = " + result);
        assertNotNull(result);
    }
}
