package com.example.service;

import com.example.model.DocumentChunk;
import com.example.model.RAGRequest;
import com.example.model.RAGResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Core RAG (Retrieval-Augmented Generation) Service
 * Orchestrates retrieval and generation with Groq API
 */
@Service
public class RAGService {

    private static final Logger logger = LoggerFactory.getLogger(RAGService.class);

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private VectorDatabaseService vectorDatabaseService;

    @Autowired
    private GroqService groqService;

    @Value("${rag.retrieval-top-k:5}")
    private int defaultTopK;

    @Value("${rag.max-context-length:4000}")
    private int maxContextLength;

    /**
     * Process a RAG query: retrieve context and generate response
     */
    public RAGResponse query(RAGRequest request) {
        logger.info("Processing RAG query: {}", request.getMessage());

        RAGResponse response = new RAGResponse();

        try {
            // Step 1: Generate embedding for the query
            float[] queryEmbedding = embeddingService.generateEmbedding(request.getMessage());

            // Step 2: Retrieve similar document chunks from vector database
            int topK = request.getTopK() != null ? request.getTopK() : defaultTopK;
            List<DocumentChunk> retrievedChunks = vectorDatabaseService.searchSimilar(queryEmbedding, topK);

            logger.info("Retrieved {} relevant chunks", retrievedChunks.size());

            // Step 3: Build context from retrieved chunks
            String context = buildContext(retrievedChunks);

            // Step 4: Generate response using Groq with context
            String groqResponse;
            if (retrievedChunks.isEmpty()) {
                // No context found, use regular chat
                logger.warn("No relevant context found, using regular chat");
                groqResponse = groqService.chat(request.getMessage());
            } else {
                // Use RAG with context
                groqResponse = chatWithContext(request.getMessage(), context);
            }

            // Step 5: Build response
            response.setResponse(groqResponse);
            response.setRetrievedChunks(retrievedChunks);
            response.getMetadata().put("retrievedChunks", retrievedChunks.size());
            response.getMetadata().put("contextLength", context.length());
            response.getMetadata().put("topK", topK);

        } catch (Exception e) {
            logger.error("Error processing RAG query", e);
            response.setResponse("Error: " + e.getMessage());
            response.getMetadata().put("error", e.getMessage());
        }

        return response;
    }

    /**
     * Build context string from retrieved chunks
     */
    private String buildContext(List<DocumentChunk> chunks) {
        if (chunks.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        int currentLength = 0;

        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = chunks.get(i);
            String chunkText = chunk.getContent();

            // Check if adding this chunk would exceed max context length
            if (currentLength + chunkText.length() > maxContextLength) {
                logger.debug("Reached max context length, stopping at chunk {}", i);
                break;
            }

            // Add chunk with metadata
            context.append("--- Document ").append(i + 1);

            // Add source information if available
            if (chunk.getMetadata().containsKey("fileName")) {
                context.append(" (").append(chunk.getMetadata().get("fileName")).append(")");
            } else if (chunk.getMetadata().containsKey("filePath")) {
                context.append(" (").append(chunk.getMetadata().get("filePath")).append(")");
            }

            context.append(" ---\n");
            context.append(chunkText);
            context.append("\n\n");

            currentLength += chunkText.length();
        }

        return context.toString();
    }

    /**
     * Chat with Groq API using context-enhanced prompt
     */
    private String chatWithContext(String userMessage, String context) {
        try {
            // Build enhanced prompt with context
            String enhancedPrompt = buildEnhancedPrompt(userMessage, context);

            // Call Groq API
            return groqService.chat(enhancedPrompt);

        } catch (Exception e) {
            logger.error("Error calling Groq API with context", e);
            throw new RuntimeException("Failed to generate response", e);
        }
    }

    /**
     * Build enhanced prompt with context
     */
    private String buildEnhancedPrompt(String userMessage, String context) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an expert AI Assistant specialized in information retrieval. ");
        prompt.append(
                "Your goal is to provide accurate, concise, and helpful answers based ONLY on the provided context.\n\n");

        prompt.append("### INSTRUCTIONS:\n");
        prompt.append("1. Answer the user question using the provided CONTEXT below.\n");
        prompt.append(
                "2. If the context contains the answer, cite the document name (e.g., [Document 1]) in your response.\n");
        prompt.append(
                "3. If the context DOES NOT contain sufficient information to answer the question, clearly state that the information is not present in the provided documents, then provide a brief general answer if possible.\n");
        prompt.append("4. Maintain a professional and technical tone.\n\n");

        prompt.append("### CONTEXT:\n");
        prompt.append(context);
        prompt.append("\n");

        prompt.append("### USER QUESTION:\n");
        prompt.append(userMessage);
        prompt.append("\n\n");

        prompt.append("### RESPONSE:");

        return prompt.toString();
    }

    /**
     * Search for relevant documents without generating a response
     */
    public List<DocumentChunk> search(String query, int topK) {
        logger.info("Searching for: {}", query);

        try {
            // Generate embedding for the query
            float[] queryEmbedding = embeddingService.generateEmbedding(query);

            // Retrieve similar chunks
            List<DocumentChunk> results = vectorDatabaseService.searchSimilar(queryEmbedding, topK);

            logger.info("Found {} results", results.size());
            return results;

        } catch (Exception e) {
            logger.error("Error searching", e);
            return new ArrayList<>();
        }
    }
}
