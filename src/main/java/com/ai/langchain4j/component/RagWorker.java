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
import java.io.BufferedReader;
import java.io.InputStreamReader;

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
            // Ensure the model is pulled
            pullModelIfNeeded("llama2");

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

    private void pullModelIfNeeded(String modelName) throws Exception {
        // Logic to check if the model is available and pull it if necessary
        boolean modelExists = checkIfModelExists(modelName);
        if (!modelExists) {
            pullModel(modelName);
        }
    }

    private boolean checkIfModelExists(String modelName) {
        // Implement the logic to check if the model is available
        // This might involve checking a local directory or calling an API
        return false; // Placeholder return
    }

    private void pullModel(String modelName) throws Exception {
        // Implement the logic to pull the model using Ollama
        ProcessBuilder processBuilder = new ProcessBuilder("ollama", "pull", modelName);
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorMsg = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                errorMsg.append(line);
            }
            throw new RuntimeException("Failed to pull model: " + modelName + ". Error: " + errorMsg);
        }
    }
}
