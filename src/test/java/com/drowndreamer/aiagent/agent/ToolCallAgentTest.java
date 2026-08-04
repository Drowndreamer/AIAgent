package com.drowndreamer.aiagent.agent;

import com.drowndreamer.aiagent.agent.model.AgentState;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ToolCallAgentTest {

    @Test
    void shouldReturnFinalResponseWhenNoMoreToolsAreNeeded() {
        ToolCallAgent agent = new ToolCallAgent(new ToolCallback[0]) {
            @Override
            public boolean think() {
                setFinalResponse("这是模型生成的最终回答");
                setState(AgentState.FINISHED);
                return false;
            }
        };

        assertEquals("这是模型生成的最终回答", agent.step());
        assertEquals(AgentState.FINISHED, agent.getState());
    }
}
