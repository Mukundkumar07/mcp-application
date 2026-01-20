package com.example.model;

import java.util.List;
import java.util.Map;

/**
 * Request model for RAG queries
 */
public class RAGRequest {
    private String message;
    private List<String> sources;
    private Integer topK;
    private Map<String, String> filters;

    public RAGRequest() {
    }

    public RAGRequest(String message) {
        this.message = message;
        this.topK = 5; // Default
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public Integer getTopK() {
        return topK != null ? topK : 5;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public Map<String, String> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, String> filters) {
        this.filters = filters;
    }

    @Override
    public String toString() {
        return "RAGRequest{" +
                "message='" + message + '\'' +
                ", sources=" + sources +
                ", topK=" + topK +
                '}';
    }
}
