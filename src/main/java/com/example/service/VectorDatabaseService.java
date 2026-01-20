package com.example.service;

import com.example.model.DocumentChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.*;

/**
 * Service for interacting with vector database
 * Simplified in-memory implementation for demo
 * TODO: Replace with actual Qdrant integration when Qdrant is running
 */
@Service
public class VectorDatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(VectorDatabaseService.class);

    @Value("${vector.db.host:localhost}")
    private String host;

    @Value("${vector.db.port:6333}")
    private int port;

    @Value("${vector.db.collection:documents}")
    private String collectionName;

    @Value("${vector.db.enabled:false}")
    private boolean enabled;

    @Value("${embedding.dimension:384}")
    private int vectorSize;

    // In-memory storage for demo (replace with actual Qdrant when available)
    private List<StoredEmbedding> embeddingStore;
    private boolean isConnected = false;

    @PostConstruct
    public void initialize() {
        embeddingStore = new ArrayList<>();

        if (!enabled) {
            logger.warn("Vector database is disabled. Using in-memory storage for demo.");
            logger.warn("To enable Qdrant: 1) Start Qdrant server, 2) Set vector.db.enabled=true");
            isConnected = false;
            return;
        }

        // Try to connect to Qdrant
        try {
            logger.info("Attempting to connect to Qdrant at {}:{}", host, port);
            // For now, we'll use in-memory storage
            // TODO: Implement actual Qdrant connection when server is available
            logger.warn("Using in-memory vector storage (Qdrant integration pending)");
            isConnected = true;
        } catch (Exception e) {
            logger.error("Failed to connect to Qdrant. Using in-memory storage.", e);
            isConnected = false;
        }
    }

    /**
     * Store a document chunk with its embedding
     */
    public void storeEmbedding(DocumentChunk chunk) {
        StoredEmbedding stored = new StoredEmbedding();
        stored.id = chunk.getId();
        stored.embedding = chunk.getEmbedding();
        stored.chunk = chunk;

        embeddingStore.add(stored);
        logger.debug("Stored embedding for chunk: {}", chunk.getId());
    }

    /**
     * Store multiple document chunks in batch
     */
    public void storeEmbeddings(List<DocumentChunk> chunks) {
        for (DocumentChunk chunk : chunks) {
            storeEmbedding(chunk);
        }
        logger.info("Stored {} embeddings in batch", chunks.size());
    }

    /**
     * Search for similar document chunks using cosine similarity
     */
    public List<DocumentChunk> searchSimilar(float[] queryEmbedding, int topK) {
        if (embeddingStore.isEmpty()) {
            logger.warn("Vector store is empty. No results to return.");
            return Collections.emptyList();
        }

        // Calculate similarity scores for all embeddings
        List<ScoredChunk> scoredChunks = new ArrayList<>();

        for (StoredEmbedding stored : embeddingStore) {
            double similarity = cosineSimilarity(queryEmbedding, stored.embedding);
            scoredChunks.add(new ScoredChunk(stored.chunk, similarity));
        }

        // Sort by similarity (descending)
        scoredChunks.sort((a, b) -> Double.compare(b.score, a.score));

        // Return top-K results
        List<DocumentChunk> results = new ArrayList<>();
        int limit = Math.min(topK, scoredChunks.size());

        for (int i = 0; i < limit; i++) {
            ScoredChunk scored = scoredChunks.get(i);
            scored.chunk.setScore(scored.score);
            results.add(scored.chunk);
        }

        logger.info("Found {} similar chunks (top-{} from {} total)", results.size(), topK, embeddingStore.size());
        return results;
    }

    /**
     * Calculate cosine similarity between two embeddings
     */
    private double cosineSimilarity(float[] vec1, float[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("Vectors must have same dimension");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Delete all embeddings from the collection
     */
    public void clearCollection() {
        embeddingStore.clear();
        logger.info("Collection '{}' cleared", collectionName);
    }

    /**
     * Get collection statistics
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("connected", isConnected);
        stats.put("host", host);
        stats.put("port", port);
        stats.put("collection", collectionName);
        stats.put("vectorSize", vectorSize);
        stats.put("pointsCount", embeddingStore.size());
        stats.put("storageType", "in-memory");
        stats.put("note", "Using in-memory storage. Start Qdrant and set vector.db.enabled=true for production");

        return stats;
    }

    @PreDestroy
    public void cleanup() {
        embeddingStore.clear();
        logger.info("Vector database service cleaned up");
    }

    public boolean isConnected() {
        return true; // Always return true for in-memory storage
    }

    /**
     * Internal class for storing embeddings
     */
    private static class StoredEmbedding {
        String id;
        float[] embedding;
        DocumentChunk chunk;
    }

    /**
     * Internal class for scored chunks
     */
    private static class ScoredChunk {
        DocumentChunk chunk;
        double score;

        ScoredChunk(DocumentChunk chunk, double score) {
            this.chunk = chunk;
            this.score = score;
        }
    }
}
