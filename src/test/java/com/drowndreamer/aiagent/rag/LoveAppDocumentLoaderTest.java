package com.drowndreamer.aiagent.rag;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoveAppDocumentLoaderTest {

    private final LoveAppDocumentLoader loveAppDocumentLoader =
            new LoveAppDocumentLoader(new PathMatchingResourcePatternResolver());

    @Test
    void loadMarkdowns() {
        var documents = loveAppDocumentLoader.loadMarkdowns();
        assertFalse(documents.isEmpty(), "应该至少加载一个 Markdown 文档");
        assertTrue(documents.stream()
                .allMatch(document -> document.getMetadata().containsKey("filename")));
    }
}
