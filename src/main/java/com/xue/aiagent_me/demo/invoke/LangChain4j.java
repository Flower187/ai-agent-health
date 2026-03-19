package com.xue.aiagent_me.demo.invoke;


import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;

public class LangChain4j {

    public static void main(String[] args) {

        String apiKey = System.getenv(TestApiKey.API_KEY);
        ChatLanguageModel qwenChatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-max")
                .build();

        String answer =  qwenChatModel.chat("你是我的电子榨菜");
        System.out.println(answer); // Hello World
    }
}
