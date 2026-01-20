package com.example.service;

import com.example.mcp.ExternalResourceMCPServer;
import com.example.mcp.FileSystemMCPServer;
import com.example.model.Document;
import com.example.model.DocumentChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Service for ingesting documents from various MCP sources
 * Handles chunking, embedding generation, and vector storage
 */
@Service
public class DocumentIngestionService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentIngestionService.class);

    @Autowired
    private FileSystemMCPServer fileSystemMCP;

    @Autowired
    private ExternalResourceMCPServer externalResourceMCP;

    @Autowired
    private DocumentChunker documentChunker;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private VectorDatabaseService vectorDatabaseService;

    /**
     * Ingest documents from filesystem
     */
    public IngestionResult ingestFromFilesystem(String path, String filePattern) {
        logger.info("Starting filesystem ingestion from path: {}", path);

        IngestionResult result = new IngestionResult();
        result.setSource("filesystem");
        result.setStartTime(System.currentTimeMillis());

        try {
            // List files from filesystem MCP
            List<FileSystemMCPServer.FileInfo> files = fileSystemMCP.listFiles(path, filePattern);
            result.setTotalFiles(files.size());

            int processedFiles = 0;
            int totalChunks = 0;

            for (FileSystemMCPServer.FileInfo fileInfo : files) {
                try {
                    // Read file content
                    String content = fileSystemMCP.readFile(fileInfo.getPath());

                    // Create document
                    Document document = new Document();
                    document.setId(UUID.randomUUID().toString());
                    document.setContent(content);
                    document.setSource("filesystem");
                    document.setFilePath(fileInfo.getPath());
                    document.getMetadata().put("fileName", fileInfo.getName());
                    document.getMetadata().put("fileSize", fileInfo.getSize());
                    document.getMetadata().put("mimeType", fileInfo.getMimeType());

                    // Chunk and store
                    int chunks = processDocument(document);
                    totalChunks += chunks;
                    processedFiles++;

                    logger.debug("Processed file: {} ({} chunks)", fileInfo.getName(), chunks);

                } catch (Exception e) {
                    logger.error("Error processing file: {}", fileInfo.getPath(), e);
                    result.getErrors().add("File: " + fileInfo.getPath() + " - " + e.getMessage());
                }
            }

            result.setProcessedFiles(processedFiles);
            result.setTotalChunks(totalChunks);
            result.setSuccess(true);

        } catch (Exception e) {
            logger.error("Filesystem ingestion failed", e);
            result.setSuccess(false);
            result.getErrors().add("Ingestion failed: " + e.getMessage());
        }

        result.setEndTime(System.currentTimeMillis());
        logger.info("Filesystem ingestion completed: {} files, {} chunks in {}ms",
                result.getProcessedFiles(), result.getTotalChunks(), result.getDuration());

        return result;
    }

    /**
     * Ingest data from database query
     */
    public IngestionResult ingestFromDatabase(String query, String connectionString) {
        logger.info("Starting database ingestion");

        IngestionResult result = new IngestionResult();
        result.setSource("database");
        result.setStartTime(System.currentTimeMillis());

        try {
            // Query database via MCP
            List<Map<String, Object>> rows = externalResourceMCP.queryDatabase(query, connectionString);
            result.setTotalFiles(rows.size());

            int processedRows = 0;
            int totalChunks = 0;

            for (Map<String, Object> row : rows) {
                try {
                    // Convert row to text
                    String content = convertRowToText(row);

                    // Create document
                    Document document = new Document();
                    document.setId(UUID.randomUUID().toString());
                    document.setContent(content);
                    document.setSource("database");
                    document.setFilePath("query_result_" + processedRows);
                    document.getMetadata().putAll(convertToStringMap(row));

                    // Chunk and store
                    int chunks = processDocument(document);
                    totalChunks += chunks;
                    processedRows++;

                } catch (Exception e) {
                    logger.error("Error processing database row", e);
                    result.getErrors().add("Row " + processedRows + ": " + e.getMessage());
                }
            }

            result.setProcessedFiles(processedRows);
            result.setTotalChunks(totalChunks);
            result.setSuccess(true);

        } catch (Exception e) {
            logger.error("Database ingestion failed", e);
            result.setSuccess(false);
            result.getErrors().add("Ingestion failed: " + e.getMessage());
        }

        result.setEndTime(System.currentTimeMillis());
        logger.info("Database ingestion completed: {} rows, {} chunks",
                result.getProcessedFiles(), result.getTotalChunks());

        return result;
    }

    /**
     * Ingest content from a web page
     */
    public IngestionResult ingestFromWebPage(String url) {
        logger.info("Starting web page ingestion from: {}", url);

        IngestionResult result = new IngestionResult();
        result.setSource("web");
        result.setStartTime(System.currentTimeMillis());

        try {
            // Scrape web page via MCP
            String content = externalResourceMCP.scrapeWebPage(url);

            // Create document
            Document document = new Document();
            document.setId(UUID.randomUUID().toString());
            document.setContent(content);
            document.setSource("web");
            document.setFilePath(url);
            document.getMetadata().put("url", url);

            // Chunk and store
            int chunks = processDocument(document);

            result.setTotalFiles(1);
            result.setProcessedFiles(1);
            result.setTotalChunks(chunks);
            result.setSuccess(true);

        } catch (Exception e) {
            logger.error("Web page ingestion failed", e);
            result.setSuccess(false);
            result.getErrors().add("Ingestion failed: " + e.getMessage());
        }

        result.setEndTime(System.currentTimeMillis());
        logger.info("Web page ingestion completed: {} chunks", result.getTotalChunks());

        return result;
    }

    /**
     * Process a single document: chunk, embed, and store
     */
    private int processDocument(Document document) throws ExecutionException, InterruptedException {
        // Chunk the document
        List<String> chunks = documentChunker.chunkText(document.getContent());

        // Create document chunks with embeddings
        List<DocumentChunk> documentChunks = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i);

            // Generate embedding
            float[] embedding = embeddingService.generateEmbedding(chunkText);

            // Create chunk
            DocumentChunk chunk = new DocumentChunk();
            chunk.setId(document.getId() + "_chunk_" + i);
            chunk.setDocumentId(document.getId());
            chunk.setContent(chunkText);
            chunk.setChunkIndex(i);
            chunk.setEmbedding(embedding);

            // Copy metadata from document
            chunk.setMetadata(new HashMap<>(document.getMetadata()));
            chunk.getMetadata().put("source", document.getSource());
            chunk.getMetadata().put("filePath", document.getFilePath());

            documentChunks.add(chunk);
        }

        // Store in vector database
        if (!documentChunks.isEmpty()) {
            vectorDatabaseService.storeEmbeddings(documentChunks);
        }

        return documentChunks.size();
    }

    /**
     * Convert database row to text
     */
    private String convertRowToText(Map<String, Object> row) {
        StringBuilder text = new StringBuilder();
        row.forEach((key, value) -> {
            text.append(key).append(": ").append(value).append("\n");
        });
        return text.toString();
    }

    /**
     * Convert map to string map
     */
    private Map<String, Object> convertToStringMap(Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>();
        map.forEach((key, value) -> {
            if (value != null) {
                result.put(key, value.toString());
            }
        });
        return result;
    }

    /**
     * Ingestion result model
     */
    public static class IngestionResult {
        private String source;
        private int totalFiles;
        private int processedFiles;
        private int totalChunks;
        private boolean success;
        private long startTime;
        private long endTime;
        private List<String> errors = new ArrayList<>();

        public long getDuration() {
            return endTime - startTime;
        }

        // Getters and Setters
        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public int getTotalFiles() {
            return totalFiles;
        }

        public void setTotalFiles(int totalFiles) {
            this.totalFiles = totalFiles;
        }

        public int getProcessedFiles() {
            return processedFiles;
        }

        public void setProcessedFiles(int processedFiles) {
            this.processedFiles = processedFiles;
        }

        public int getTotalChunks() {
            return totalChunks;
        }

        public void setTotalChunks(int totalChunks) {
            this.totalChunks = totalChunks;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getEndTime() {
            return endTime;
        }

        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }

        @Override
        public String toString() {
            return "IngestionResult{source='" + source + "', files=" + processedFiles +
                    "/" + totalFiles + ", chunks=" + totalChunks + ", success=" + success +
                    ", duration=" + getDuration() + "ms}";
        }
    }
}
