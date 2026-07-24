package com.drowndreamer.aiagent.rag;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.Assert;

@Configuration
public class AliyunEmbeddingConfig {

    @Bean
    @Lazy
    public OpenAiEmbeddingModel aliyunEmbeddingModel(
            @Value("${app.ai.embedding.base-url:}") String baseUrl,
            @Value("${app.ai.embedding.api-key:}") String apiKey,
            @Value("${app.ai.embedding.model:qwen3.7-text-embedding}") String model,
            @Value("${app.ai.embedding.dimensions:1024}") Integer dimensions) {
        Assert.hasText(baseUrl, "Please set ALIYUN_EMBEDDING_BASE_URL");
        Assert.hasText(apiKey, "Please set ALIYUN_EMBEDDING_API_KEY");

        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(removeTrailingSlash(baseUrl))
                .apiKey(apiKey)
                .embeddingsPath("/embeddings")
                .build();

        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .model(model)
                .dimensions(dimensions)
                .encodingFormat("float")
                .build();

        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);
    }

    private String removeTrailingSlash(String value) {
        String result = value.trim();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
