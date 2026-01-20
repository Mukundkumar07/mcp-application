package com.example.model;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Response model for RAG queries
 */
public class RAGResponse {
    private String response;
    private List<DocumentChunk> retrievedChunks;
    private Map<String, Object> metadata;

    public RAGResponse() {
        this.metadata = new HashMap<>();
    }

    public RAGResponse(String response, List<DocumentChunk> retrievedChunks) {
        this();
        this.response = response;
        this.retrievedChunks = retrievedChunks;
    }

    // Getters and Setters
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<DocumentChunk> getRetrievedChunks() {
        return retrievedChunks;
    }

    public void setRetrievedChunks(List<DocumentChunk> retrievedChunks) {
        this.retrievedChunks = retrievedChunks;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "RAGResponse{" +
                "responseLength=" + (response != null ? response.length() : 0) +
                ", retrievedChunks=" + (retrievedChunks != null ? retrievedChunks.size() : 0) +
                ", metadata=" + metadata +
                '}';
    }
}
