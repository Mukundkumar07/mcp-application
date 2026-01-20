package com.example.service;

import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import ai.djl.inference.Predictor;
import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for generating text embeddings using HuggingFace models via DJL
 * Uses sentence-transformers/all-MiniLM-L6-v2 for lightweight, fast embeddings
 */
@Service
public class EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);

    @Value("${embedding.model.name:sentence-transformers/all-MiniLM-L6-v2}")
    private String modelName;

    @Value("${embedding.dimension:384}")
    private int embeddingDimension;

    @Value("${embedding.cache.enabled:true}")
    private boolean cacheEnabled;

    private ZooModel<String, float[]> model;
    private ConcurrentHashMap<String, float[]> embeddingCache;

    @PostConstruct
    public void initialize() {
        logger.info("Initializing EmbeddingService with model: {}", modelName);
        embeddingCache = new ConcurrentHashMap<>();

        try {
            Criteria<String, float[]> criteria = Criteria.builder()
                    .setTypes(String.class, float[].class)
                    .optModelUrls("djl://ai.djl.huggingface.pytorch/" + modelName)
                    .optEngine("PyTorch")
                    .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
                    .optProgress(new ProgressBar())
                    .build();

            this.model = criteria.loadModel();
            logger.info("Embedding service initialized with real AI model: {}", modelName);
        } catch (Exception e) {
            logger.error("Failed to initialize real embedding model. Falling back to simulation.", e);
        }
    }

    /**
     * Generate embedding for a single text
     */
    public float[] generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new float[embeddingDimension];
        }

        // Check cache first
        if (cacheEnabled && embeddingCache.containsKey(text)) {
            logger.debug("Returning cached embedding for text");
            return embeddingCache.get(text);
        }

        // Generate embedding using real model
        float[] embedding = generateRealEmbedding(text);

        // Cache the result
        if (cacheEnabled && embeddingCache.size() < 10000) { // Limit cache size
            embeddingCache.put(text, embedding);
        }

        return embedding;
    }

    /**
     * Generate embeddings for multiple texts (batch processing)
     */
    public float[][] generateEmbeddings(String[] texts) {
        float[][] embeddings = new float[texts.length][];
        for (int i = 0; i < texts.length; i++) {
            embeddings[i] = generateEmbedding(texts[i]);
        }
        return embeddings;
    }

    /**
     * Generate embedding using the real AI model
     */
    private float[] generateRealEmbedding(String text) {
        if (model == null) {
            return generateSimulatedEmbedding(text);
        }

        try (Predictor<String, float[]> predictor = model.newPredictor()) {
            return predictor.predict(text);
        } catch (TranslateException e) {
            logger.error("Error during embedding inference", e);
            return generateSimulatedEmbedding(text);
        }
    }

    /**
     * Fallback simulated embedding generation using text hashing
     */
    private float[] generateSimulatedEmbedding(String text) {
        logger.warn("Using simulated embedding fallback for text length: {}", text.length());
        float[] embedding = new float[embeddingDimension];

        // Use text hash to generate deterministic but varied embeddings
        int hash = text.hashCode();
        java.util.Random random = new java.util.Random(hash);

        for (int i = 0; i < embeddingDimension; i++) {
            embedding[i] = (random.nextFloat() * 2) - 1; // Range: -1 to 1
        }

        // Normalize the vector
        float norm = 0;
        for (float v : embedding) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);

        for (int i = 0; i < embeddingDimension; i++) {
            embedding[i] /= norm;
        }

        return embedding;
    }

    /**
     * Calculate cosine similarity between two embeddings
     */
    public double cosineSimilarity(float[] embedding1, float[] embedding2) {
        if (embedding1.length != embedding2.length) {
            throw new IllegalArgumentException("Embeddings must have the same dimension");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < embedding1.length; i++) {
            dotProduct += embedding1[i] * embedding2[i];
            norm1 += embedding1[i] * embedding1[i];
            norm2 += embedding2[i] * embedding2[i];
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Clear the embedding cache
     */
    public void clearCache() {
        embeddingCache.clear();
        logger.info("Embedding cache cleared");
    }

    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", embeddingCache.size());
        stats.put("cacheEnabled", cacheEnabled);
        stats.put("modelName", modelName);
        stats.put("embeddingDimension", embeddingDimension);
        return stats;
    }
}
