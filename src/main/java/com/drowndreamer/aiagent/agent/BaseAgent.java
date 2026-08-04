package com.drowndreamer.aiagent.agent;


import cn.hutool.core.util.StrUtil;
import com.drowndreamer.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 抽象基础代理类
 * 定义流程，状态转换管理 、Agent loop
 * 子类必须实现 step 方法
 */
@Data
@Slf4j
public abstract class BaseAgent {
    // 核心属性
    private String name;

    // 提示词
    private String systemPrompt;
    private String nextStepPrompt;

    // 代理状态
    private AgentState state = AgentState.IDLE;

    // 执行步骤控制
    private int currentStep = 0;
    private int maxStep = 5;

    // LLM 大模型
    private ChatClient chatClient;

    // Memory 记忆（需要自主维护上下文）
    private List<Message> messageList = new ArrayList<>();


    /**
     *  运行代理
     * @param userPrompt
     * @return
     */
    public String run(String userPrompt) {
        if (state != AgentState.IDLE || StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Can not run agent");
        }
        // 执行，更改状态
        state = AgentState.RUNNING;
        // 记录消息上下文，一开始是用户的提示词
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> results = new ArrayList<>();
        // 执行循环
        try {
            for(int i = 0; i < maxStep && state != AgentState.FINISHED; i++){
                int setNumber = i + 1;
                currentStep = setNumber;
                log.info("Executing step {}/{}",  currentStep, maxStep);
                // 单步执行
                String stepResult = step();
                String result = "Step" + setNumber + ": " + stepResult;
                results.add(result);
            }
            // 检查是否超出步骤限制
            if(currentStep >= maxStep){
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxStep + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            throw new RuntimeException(e);
        } finally {
            this.cleanup();
        }
    }

    public SseEmitter runStream(String userPrompt) {
        SseEmitter sseEmitter = new SseEmitter(300000L);
        // 这里要使用异步处理，避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            try {
                if (state != AgentState.IDLE || StrUtil.isBlank(userPrompt)) {
                    sseEmitter.send("错误，无法从该状态运行代理 或者 不能使用空提示词运行代理：" + state);
                    sseEmitter.complete();
                    return;
                }
            } catch (Exception e) {
                sseEmitter.completeWithError(e);
            }
            // 执行，更改状态
            state = AgentState.RUNNING;
            // 记录消息上下文，一开始是用户的提示词
            messageList.add(new UserMessage(userPrompt));
            // 保存结果列表
            List<String> results = new ArrayList<>();
            // 执行循环
            try {
                for(int i = 0; i < maxStep && state != AgentState.FINISHED; i++){
                    int setNumber = i + 1;
                    currentStep = setNumber;
                    log.info("Executing step {}/{}",  currentStep, maxStep);
                    // 单步执行
                    String stepResult = step();
                    String result = "Step" + setNumber + ": " + stepResult;
                    results.add(result);

                    sseEmitter.send(result);
                }
                // 检查是否超出步骤限制
                if(currentStep >= maxStep){
                    state = AgentState.FINISHED;
                    results.add("Terminated: Reached max steps (" + maxStep + ")");
                    sseEmitter.send("达到最大步骤，结束调用");
                }
                // 正常完成
                sseEmitter.complete();

            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("Error executing agent", e);
                try {
                    sseEmitter.send("执行错误" + e.getMessage());
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
                throw new RuntimeException(e);
            } finally {
                this.cleanup();
            }

        });
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("Timeout SSE");
        });
        sseEmitter.onCompletion(() -> {
            if(state == AgentState.RUNNING){
                state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE Emitter completed");
        });
        return sseEmitter;
    }

    /**
     * 定义单个步骤
     * @return
     */
    public abstract String step();


    protected void cleanup() {
        // 子类可以重写此方法来清理资源

    }
}
