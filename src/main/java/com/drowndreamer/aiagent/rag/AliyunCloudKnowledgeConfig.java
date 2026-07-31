package com.drowndreamer.aiagent.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration
public class AliyunCloudKnowledgeConfig {

    @Bean
    public DashScopeApi dashScopeApi(
            @Value("${spring.ai.dashscope.api-key:}") String apiKey,
            @Value("${spring.ai.dashscope.workspace-id:}") String workspaceId) {
        Assert.hasText(apiKey, "Please set DASHSCOPE_API_KEY");
        Assert.hasText(workspaceId, "Please set DASHSCOPE_WORKSPACE_ID");

        return DashScopeApi.builder()
                .apiKey(apiKey)
                .workSpaceId(workspaceId)
                .build();
    }

    @Bean("cloudKnowledgeRetriever")
    public DocumentRetriever cloudKnowledgeRetriever(
            DashScopeApi dashScopeApi,
            @Value("${app.ai.knowledge-base.name:}") String knowledgeBaseName) {
        Assert.hasText(knowledgeBaseName, "Please set ALIYUN_KNOWLEDGE_BASE_NAME");

        DashScopeDocumentRetrieverOptions options = DashScopeDocumentRetrieverOptions.builder()
                .indexName(knowledgeBaseName)
                .denseSimilarityTopK(20)
                .sparseSimilarityTopK(20)
                .enableReranking(true)
                .rerankModelName("qwen3-rerank")
                .rerankTopN(5)
                .rerankMinScore(0.01F)
                .build();

        return new DashScopeDocumentRetriever(dashScopeApi, options);
    }

    @Bean("cloudRetrievalAugmentationAdvisor")
    public RetrievalAugmentationAdvisor cloudRetrievalAugmentationAdvisor(
            @Qualifier("cloudKnowledgeRetriever") DocumentRetriever documentRetriever) {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(false)
                        .build())
                .build();
    }
}
