package com.xue.aiagent_me;

import com.xue.aiagent_me.advisor.MyLoggerAdvisor;
import com.xue.aiagent_me.tools.WeatherTool;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class WeatherToolTest {

    @Resource
    private ChatModel dashscopeChatModel;

    @Test
    void getWeather1() {
        //  通过 builder.defaultTools() + .tools() 注册工具
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(
                        new MyLoggerAdvisor()
                )
                .defaultTools(
                        new WeatherTool()
                )
                .build();

        String aiReply = chatClient.prompt("深圳今天天气怎么样?")
                .tools(new WeatherTool())
                .call()
                .content();
        assertNotNull(aiReply);
    }
    @Test
    void getWeather2() {
        // 在这次对话中提供工具     直接通过 .tools() 注册工具
        String aiReply = ChatClient.create(dashscopeChatModel)
                .prompt("深圳今天天气怎么样?")
                .advisors(new MyLoggerAdvisor())
                .tools(new WeatherTool())
                .call()
                .content();

        assertNotNull(aiReply);
    }
    @Test
    void getWeather3() {
        ToolCallingChatOptions toolCallingChatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(
                        ToolCallbacks.from(new WeatherTool()) // 错误1：只配回调，未指定工具

                )
                .build();
        String aiReply = ChatClient.create(dashscopeChatModel)
                .prompt(new Prompt("深圳今天天气怎么样?", toolCallingChatOptions)) // 错误2：Prompt 传参覆盖
                .advisors(new MyLoggerAdvisor())
                .tools(new WeatherTool())// 错误3：优先级问题，配置失效
                .call()
                .content();
        assertNotNull(aiReply);
    }
}
