package com.ai.langchain4j.component;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class RagWorker {
    private static final Logger logger = LoggerFactory.getLogger(RagWorker.class);
    private final VectorStore vectorStore;
    private final Resource pdfResource;

    public RagWorker(VectorStore vectorStore,
                     @Value("classpath:/so_survey_2023.pdf") Resource pdfResource) {
        this.vectorStore = vectorStore;
        this.pdfResource = pdfResource;
    }

    @PostConstruct
    public void init() {
        try {
            TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(pdfResource);
            TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
            vectorStore.add(tokenTextSplitter.apply(tikaDocumentReader.get()));
        } catch (Exception e) {
            // Log the exception and handle it appropriately
//            e.printStackTrace();
            logger.error("Failed to initialize RagWorker: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to initialize RagWorker: " + e.getMessage(), e);
        }
    }
}
