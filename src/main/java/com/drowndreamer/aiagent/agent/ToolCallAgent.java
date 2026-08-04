package com.drowndreamer.aiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.drowndreamer.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;


@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent{
    // 可用的工具
    private final ToolCallback[] availableTools;

    // 保存工具调用信息的响应结果（要调用哪些工具）
    private ChatResponse toolCallChatResponse;

    // 模型不再调用工具时生成的最终回答
    private String finalResponse;

    // 工具调用管理者
    private final ToolCallingManager toolCallingManager;

    // 禁止 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
    ChatOptions chatOptions = ToolCallingChatOptions.builder()
            .internalToolExecutionEnabled(false)
            .build();


    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
    }

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动
     */
    @Override
    public boolean think() {
        this.finalResponse = null;
        // 1、校验提示词，拼接用户提示词
        if (StrUtil.isNotBlank(getNextStepPrompt())){
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        // 2、调用 AI 大模型，获取工具调用结果
        // List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            // 记录响应，用于等下的 Act 步骤
            this.toolCallChatResponse = chatResponse;
            // 3、解析工具调用结果，获取要调用的工具
            // 助手消息
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 过去到要调用的工具列表
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            // 输出提示信息
            String result = assistantMessage.getText();
            log.info(getName() + "的思考：" + result);
            log.info(getName() + "选择了 " + toolCallList.size() + " 个工具来调用");
            String toolCallinfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称：%s, 参数：%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallinfo);
            // 如果不需要工具，即不需要进入 Act，返回 false
            if(toolCallList.isEmpty()) {
                // 只有不调用工具时，才需要手动记录助手消息
                getMessageList().add(assistantMessage);
                if (StrUtil.isNotBlank(result)) {
                    this.finalResponse = result;
                    setState(AgentState.FINISHED);
                }
                return false;
            } else {
                // 需要调用工具时，无需记录助手消息，因为调用工具时，即执行 Act 时会记录的
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题" + e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到了错误：" + e.getMessage()));
            return false;
        }
    }

    @Override
    protected String getNoActionResult() {
        return StrUtil.isNotBlank(finalResponse)
                ? finalResponse
                : super.getNoActionResult();
    }

    /**
     * 执行工具调用并处理结果
     *
     * @return 结果
     */
    @Override
    public String act() {
        if(!toolCallChatResponse.hasToolCalls()){
            return "没有工具需要调用";
        }
        // 调用工具
        Prompt  prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 记录上下文消息，conversationHistory 已经包含了历史助手消息和工具调用的返回结果
        setMessageList(toolExecutionResult.conversationHistory());
        ToolResponseMessage toolResponseMessage =(ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        // 判断是否调用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> response.name().equals("doTerminate"));
        if(terminateToolCalled){
            setState(AgentState.FINISHED);
        }
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + "返回的结果：" + response.responseData())
                .collect(Collectors.joining("\n"));
        log.info(results);
        return results;
    }
}

