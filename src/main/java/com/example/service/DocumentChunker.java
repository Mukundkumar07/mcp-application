package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for intelligently chunking documents into smaller pieces
 * Maintains context with overlapping chunks
 */
@Service
public class DocumentChunker {

    private static final Logger logger = LoggerFactory.getLogger(DocumentChunker.class);

    @Value("${rag.chunk-size:512}")
    private int chunkSize;

    @Value("${rag.chunk-overlap:50}")
    private int chunkOverlap;

    /**
     * Split text into chunks with overlap
     */
    public List<String> chunkText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<String> chunks = new ArrayList<>();

        // Split by sentences first for better context preservation
        String[] sentences = text.split("(?<=[.!?])\\s+");

        StringBuilder currentChunk = new StringBuilder();
        int currentLength = 0;

        for (String sentence : sentences) {
            int sentenceLength = sentence.length();

            // If adding this sentence exceeds chunk size
            if (currentLength + sentenceLength > chunkSize && currentLength > 0) {
                // Save current chunk
                chunks.add(currentChunk.toString().trim());

                // Start new chunk with overlap
                currentChunk = new StringBuilder();
                currentLength = 0;

                // Add overlap from previous chunk
                if (chunkOverlap > 0 && !chunks.isEmpty()) {
                    String previousChunk = chunks.get(chunks.size() - 1);
                    String overlap = getOverlapText(previousChunk, chunkOverlap);
                    currentChunk.append(overlap).append(" ");
                    currentLength = overlap.length();
                }
            }

            currentChunk.append(sentence).append(" ");
            currentLength += sentenceLength;
        }

        // Add the last chunk if it has content
        if (currentLength > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        logger.debug("Split text into {} chunks (chunk size: {}, overlap: {})",
                chunks.size(), chunkSize, chunkOverlap);

        return chunks;
    }

    /**
     * Get overlap text from the end of a chunk
     */
    private String getOverlapText(String text, int overlapSize) {
        if (text.length() <= overlapSize) {
            return text;
        }

        // Try to find a sentence boundary for cleaner overlap
        String overlap = text.substring(text.length() - overlapSize);
        int sentenceStart = overlap.indexOf(". ");

        if (sentenceStart > 0) {
            return overlap.substring(sentenceStart + 2);
        }

        return overlap;
    }

    /**
     * Chunk text by fixed character count (simpler approach)
     */
    public List<String> chunkByCharacters(String text, int size, int overlap) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + size, text.length());
            chunks.add(text.substring(start, end));
            start += (size - overlap);
        }

        return chunks;
    }

    /**
     * Chunk text by paragraphs
     */
    public List<String> chunkByParagraphs(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        String[] paragraphs = text.split("\\n\\n+");
        List<String> chunks = new ArrayList<>();

        for (String paragraph : paragraphs) {
            if (paragraph.trim().length() > 0) {
                // If paragraph is too large, split it further
                if (paragraph.length() > chunkSize) {
                    chunks.addAll(chunkText(paragraph));
                } else {
                    chunks.add(paragraph.trim());
                }
            }
        }

        return chunks;
    }

    /**
     * Get chunk statistics
     */
    public ChunkStats getChunkStats(List<String> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return new ChunkStats(0, 0, 0, 0);
        }

        int totalChunks = chunks.size();
        int totalChars = chunks.stream().mapToInt(String::length).sum();
        int avgChunkSize = totalChars / totalChunks;
        int maxChunkSize = chunks.stream().mapToInt(String::length).max().orElse(0);

        return new ChunkStats(totalChunks, totalChars, avgChunkSize, maxChunkSize);
    }

    /**
     * Statistics about chunked text
     */
    public static class ChunkStats {
        public final int totalChunks;
        public final int totalCharacters;
        public final int averageChunkSize;
        public final int maxChunkSize;

        public ChunkStats(int totalChunks, int totalCharacters, int averageChunkSize, int maxChunkSize) {
            this.totalChunks = totalChunks;
            this.totalCharacters = totalCharacters;
            this.averageChunkSize = averageChunkSize;
            this.maxChunkSize = maxChunkSize;
        }

        @Override
        public String toString() {
            return String.format("ChunkStats{chunks=%d, totalChars=%d, avgSize=%d, maxSize=%d}",
                    totalChunks, totalCharacters, averageChunkSize, maxChunkSize);
        }
    }
}
