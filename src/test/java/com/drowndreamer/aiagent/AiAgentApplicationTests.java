package com.drowndreamer.aiagent;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "OPENAI_API_KEY=test-openai-key",
        "DASHSCOPE_API_KEY=test-dashscope-key",
        "DASHSCOPE_WORKSPACE_ID=test-workspace-id",
        "ALIYUN_EMBEDDING_API_KEY=test-embedding-key",
        "ALIYUN_KNOWLEDGE_BASE_NAME=test-knowledge-base"
})
@TestExecutionListeners(DependencyInjectionTestExecutionListener.class)
class AiAgentApplicationTests {

    @Autowired
    @Qualifier("openAiChatModel")
    private ChatModel chatModel;

    @Autowired
    @Qualifier("cloudKnowledgeRetriever")
    private DocumentRetriever cloudKnowledgeRetriever;

    @Autowired
    @Qualifier("cloudRetrievalAugmentationAdvisor")
    private RetrievalAugmentationAdvisor cloudRetrievalAugmentationAdvisor;

    @Test
    void contextLoads() {
        assertNotNull(chatModel);
        assertNotNull(cloudKnowledgeRetriever);
        assertNotNull(cloudRetrievalAugmentationAdvisor);
    }

}
