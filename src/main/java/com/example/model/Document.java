package com.example.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a document in the RAG system
 */
public class Document {
    private String id;
    private String content;
    private String source;
    private String filePath;
    private Map<String, Object> metadata;
    private LocalDateTime indexedAt;

    public Document() {
        this.metadata = new HashMap<>();
        this.indexedAt = LocalDateTime.now();
    }

    public Document(String id, String content, String source, String filePath) {
        this();
        this.id = id;
        this.content = content;
        this.source = source;
        this.filePath = filePath;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getIndexedAt() {
        return indexedAt;
    }

    public void setIndexedAt(LocalDateTime indexedAt) {
        this.indexedAt = indexedAt;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id='" + id + '\'' +
                ", source='" + source + '\'' +
                ", filePath='" + filePath + '\'' +
                ", indexedAt=" + indexedAt +
                '}';
    }
}
