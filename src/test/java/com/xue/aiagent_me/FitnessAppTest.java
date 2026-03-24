package com.xue.aiagent_me;

import com.xue.aiagent_me.advisor.ProhibitedWordAdvisor;
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
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮对话
        System.out.println("第一轮对话：==================================================");
        String aiReply = fitnessApp.doChat("你好，我叫小薛子，今天我要开始锻炼了", chatId);
        Assertions.assertNotNull(aiReply);
        // 第二轮对话
        System.out.println("第二轮对话：==================================================");
        aiReply = fitnessApp.doChat("怎么样可以变得更健康", chatId);
        Assertions.assertNotNull(aiReply);
        // 第三轮对话
        System.out.println("第三轮对话：==================================================");
        aiReply = fitnessApp.doChat("我的名字是什么，你还记得吗？", chatId);
        Assertions.assertNotNull(aiReply);
    }

    /**
     * 测试违禁词校验功能
     * 期望在输入包含违禁词的消息时抛出ProhibitedWordAdvisor.ProhibitedWordException异常
     * 违禁词从resources/prohibited-words.txt文件读取
     */
    @Test
    void testProhibitedWordAdvisor() {
        String chatId = UUID.randomUUID().toString();

        // 测试正常消息能正常回复
        String normalMessage = "你好，我是程序员，请给我一些健康建议";
        String answer = fitnessApp.doChat(normalMessage, chatId);
        Assertions.assertNotNull(answer);

        // 测试包含违禁词的消息会被拦截
        // 需确保prohibited-words.txt文件中包含"赌博"这个词
        String prohibitedMessage = "赌博对身体有危害吗";

        // 期望抛出ProhibitedWordException异常
        ProhibitedWordAdvisor.ProhibitedWordException exception = Assertions.assertThrows(
                ProhibitedWordAdvisor.ProhibitedWordException.class,
                () -> fitnessApp.doChat(prohibitedMessage, chatId));

        // 验证异常消息
        Assertions.assertTrue(exception.getMessage().contains("违禁词"));

        // 测试其他违禁词
        String prohibitedMessage2 = "我想了解一些色情内容";
        exception = Assertions.assertThrows(
                ProhibitedWordAdvisor.ProhibitedWordException.class,
                () -> fitnessApp.doChat(prohibitedMessage2, chatId));
        Assertions.assertTrue(exception.getMessage().contains("违禁词"));
    }

    //测试结构化输出
    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是程序员cq，我想通过健身缓解颈椎和脖子酸疼，但我不知道该怎么做";

        FitnessApp.FitnessReport fitnessReport = fitnessApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(fitnessReport);
    }



    //查询重写和翻译（文档过滤和检索）
    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是发福了，怎么办？";
        String answer =  fitnessApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }


    @Test
    void doChatWithTools() {
        // 测试联网搜索问题的答案
        testMessage("周末想带女朋友去深圳健身，推荐几个适合情侣的小众打卡地");

        // 测试网页抓取：恋爱案例分析
        testMessage("最近训练受伤了，看看编程导航网站（codefather.cn）的其他同学是怎么运动康复的？");

        // 测试资源下载：图片下载
        testMessage("直接下载一张适合做手机壁纸的健身房图片为文件");

        // 测试终端操作：执行代码
        testMessage("执行 windows 的 dir 命令来查看当前路径下的所有文件");

        // 测试文件操作：保存用户档案
        testMessage("保存我的健身档案为文件");

        // 测试 PDF 生成
        testMessage("生成一份‘七夕健身计划’PDF，包含健身中心预订、活动流程和训练清单");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = fitnessApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }




    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        String message = "我居住在浙江义乌区，请帮我找到5公里内合适的健身地点";
        String answer = fitnessApp.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(answer);
    }

}