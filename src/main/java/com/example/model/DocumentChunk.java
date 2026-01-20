package com.example.model;

import java.util.Map;
import java.util.HashMap;

/**
 * Represents a chunk of a document with its embedding
 */
public class DocumentChunk {
    private String id;
    private String documentId;
    private String content;
    private int chunkIndex;
    private float[] embedding;
    private Map<String, Object> metadata;
    private double score; // Similarity score when retrieved

    public DocumentChunk() {
        this.metadata = new HashMap<>();
    }

    public DocumentChunk(String id, String documentId, String content, int chunkIndex) {
        this();
        this.id = id;
        this.documentId = documentId;
        this.content = content;
        this.chunkIndex = chunkIndex;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "DocumentChunk{" +
                "id='" + id + '\'' +
                ", documentId='" + documentId + '\'' +
                ", chunkIndex=" + chunkIndex +
                ", score=" + score +
                ", contentLength=" + (content != null ? content.length() : 0) +
                '}';
    }
}
