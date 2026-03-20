package com.xue.aiagent_me;

import com.xue.aiagent_me.app.FitnessApp;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
public class FitnessAppTest {

    @Resource
    private FitnessApp fitnessApp;

    @Test
    void doChat(){
        String chatId = UUID.randomUUID().toString();
        // 第一轮对话
        System.out.println("第一轮对话");
        String aiReply = fitnessApp.doChat("你好，我叫小薛子，今天我要开始锻炼了", chatId);
        Assertions.assertNotNull(aiReply);
        // 第二轮对话
        System.out.println("第二轮对话");
        aiReply = fitnessApp.doChat("怎么样可以变得更健康", chatId);
        Assertions.assertNotNull(aiReply);
        // 第三轮对话
        System.out.println("第三轮对话");
        aiReply = fitnessApp.doChat("我的名字是什么，你还记得吗？", chatId);
        Assertions.assertNotNull(aiReply);
    }
}
