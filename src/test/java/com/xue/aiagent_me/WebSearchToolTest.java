package com.xue.aiagent_me;

import com.xue.aiagent_me.tools.WebSearchTool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class WebSearchToolTest {

    @Value("${search-api.api-key}")
    private String apiKey;

    @Test
    void searchWeb() {
        String result = new WebSearchTool(apiKey).searchWeb("编程导航");
        System.out.println(result);
        assertNotNull(result);
    }



}
