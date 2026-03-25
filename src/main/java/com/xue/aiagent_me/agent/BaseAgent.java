package com.xue.aiagent_me.agent;

import cn.hutool.core.util.StrUtil;
import com.xue.aiagent_me.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程。
 * <p>
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 * 子类必须实现step方法。
 */
@Data
@Slf4j
public abstract class BaseAgent {
    // 核心属性
    private String name;
    private String systemPrompt;
    private String nextStepPrompt;

    // 状态与执行控制
    private AgentState state = AgentState.IDLE;
    private int currentStep = 0;
    private int maxSteps = 10;

    // LLM与记忆
    private ChatClient chatClient;
    private List<Message> messageList = new ArrayList<>();


    // 循环检测
    private int duplicateThreshold = 2;
    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }

        // 更改状态
        state = AgentState.RUNNING;

        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));

        // 保存结果列表
        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step " + stepNumber + "/" + maxSteps);
                // 单步执行
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }
            // 检查是否超出步骤限制
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);

        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "执行错误" + e.getMessage();

        } finally {
            // 清理资源
            this.cleanup();
        }
    }



    /**
     * 执行单个步骤
     *
     * @return 步骤执行结果
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {
        // 子类可以重写此方法来清理资源
    }

    /**
     * 逐字发送消息以实现打字机效果
     *
     * @param emitter SseEmitter实例
     * @param message 完整的消息内容
     * @throws IOException 如果发送失败
     */
    private void sendWithTypingEffect(SseEmitter emitter, String message) throws IOException {
        // 每个字符之间的延迟时间（毫秒），控制打字速度
        int delay = 50; // 可以根据需要调整这个值

        // 用于构建逐步显示的字符串
        StringBuilder currentMessage = new StringBuilder();

        // 逐个字符添加并发送
        for (char c : message.toCharArray()) {
            currentMessage.append(c);

            // 发送当前累积的消息
            emitter.send(currentMessage.toString());

            try {
                // 等待指定的时间
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("发送中断: " + e.getMessage());
            }
        }
    }

    /**
     * 处理陷入循环的状态
     */
    protected void handleStuckState() {
        String stuckPrompt = "观察到重复响应。请考虑新的策略，避免重复已尝试过的无效路径。";
        this.nextStepPrompt = stuckPrompt + "\n" + (this.nextStepPrompt != null ? this.nextStepPrompt : "");
        log.warn("检测到智能体陷入循环状态。添加额外提示: {}", stuckPrompt);
    }

    /**
     * 检查代理是否陷入循环
     *
     * @return 是否陷入循环
     */
    protected boolean isStuck() {
        if (messageList.size() < 2) {
            return false;
        }

        // 获取最后一条助手消息
        AssistantMessage lastAssistantMessage = null;
        for (int i = messageList.size() - 1; i >= 0; i--) {
            if (messageList.get(i) instanceof AssistantMessage) {
                lastAssistantMessage = (AssistantMessage) messageList.get(i);
                break;
            }
        }

        if (lastAssistantMessage == null || lastAssistantMessage.getText() == null
                || lastAssistantMessage.getText().isEmpty()) {
            return false;
        }

        // 计算重复内容出现次数
        int duplicateCount = 0;
        String lastContent = lastAssistantMessage.getText();

        for (int i = messageList.size() - 2; i >= 0; i--) {
            Message msg = messageList.get(i);
            if (msg instanceof AssistantMessage) {
                AssistantMessage assistantMsg = (AssistantMessage) msg;
                if (lastContent.equals(assistantMsg.getText())) {
                    duplicateCount++;

                    if (duplicateCount >= this.duplicateThreshold) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
