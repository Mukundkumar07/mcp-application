package com.example.controller;

import com.example.mcp.ExternalResourceMCPServer;
import com.example.mcp.FileSystemMCPServer;
import com.example.model.DocumentChunk;
import com.example.model.RAGRequest;
import com.example.model.RAGResponse;
import com.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST Controller for RAG and MCP operations
 */
@RestController
@RequestMapping("/api/rag")
public class RAGController {

    @Autowired
    private RAGService ragService;

    @Autowired
    private DocumentIngestionService ingestionService;

    @Autowired
    private VectorDatabaseService vectorDatabaseService;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private FileSystemMCPServer fileSystemMCP;

    @Autowired
    private ExternalResourceMCPServer externalResourceMCP;

    /**
     * RAG-enhanced chat endpoint
     * POST /api/rag/chat
     */
    @PostMapping("/chat")
    public ResponseEntity<RAGResponse> chat(@RequestBody RAGRequest request) {
        try {
            RAGResponse response = ragService.query(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            RAGResponse errorResponse = new RAGResponse();
            errorResponse.setResponse("Error: " + e.getMessage());
            errorResponse.getMetadata().put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Search for relevant documents
     * GET /api/rag/search?query=...&topK=5
     */
    @GetMapping("/search")
    public ResponseEntity<List<DocumentChunk>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {
        try {
            List<DocumentChunk> results = ragService.search(query, topK);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    /**
     * Ingest documents from filesystem
     * POST /api/rag/ingest/filesystem
     */
    @PostMapping("/ingest/filesystem")
    public ResponseEntity<DocumentIngestionService.IngestionResult> ingestFilesystem(
            @RequestBody Map<String, String> request) {
        try {
            String path = request.getOrDefault("path", ".");
            String filePattern = request.get("filePattern");

            DocumentIngestionService.IngestionResult result = ingestionService.ingestFromFilesystem(path, filePattern);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            DocumentIngestionService.IngestionResult errorResult = new DocumentIngestionService.IngestionResult();
            errorResult.setSuccess(false);
            errorResult.getErrors().add(e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }

    /**
     * Ingest data from database
     * POST /api/rag/ingest/database
     */
    @PostMapping("/ingest/database")
    public ResponseEntity<DocumentIngestionService.IngestionResult> ingestDatabase(
            @RequestBody Map<String, String> request) {
        try {
            String query = request.get("query");
            String connectionString = request.get("connectionString");

            if (query == null || connectionString == null) {
                DocumentIngestionService.IngestionResult errorResult = new DocumentIngestionService.IngestionResult();
                errorResult.setSuccess(false);
                errorResult.getErrors().add("query and connectionString are required");
                return ResponseEntity.badRequest().body(errorResult);
            }

            DocumentIngestionService.IngestionResult result = ingestionService.ingestFromDatabase(query,
                    connectionString);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            DocumentIngestionService.IngestionResult errorResult = new DocumentIngestionService.IngestionResult();
            errorResult.setSuccess(false);
            errorResult.getErrors().add(e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }

    /**
     * Ingest content from web page
     * POST /api/rag/ingest/web
     */
    @PostMapping("/ingest/web")
    public ResponseEntity<DocumentIngestionService.IngestionResult> ingestWeb(
            @RequestBody Map<String, String> request) {
        try {
            String url = request.get("url");

            if (url == null) {
                DocumentIngestionService.IngestionResult errorResult = new DocumentIngestionService.IngestionResult();
                errorResult.setSuccess(false);
                errorResult.getErrors().add("url is required");
                return ResponseEntity.badRequest().body(errorResult);
            }

            DocumentIngestionService.IngestionResult result = ingestionService.ingestFromWebPage(url);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            DocumentIngestionService.IngestionResult errorResult = new DocumentIngestionService.IngestionResult();
            errorResult.setSuccess(false);
            errorResult.getErrors().add(e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }

    /**
     * List available MCP sources
     * GET /api/rag/mcp/sources
     */
    @GetMapping("/mcp/sources")
    public ResponseEntity<Map<String, Object>> listMCPSources() {
        Map<String, Object> sources = new HashMap<>();
        sources.put("filesystem", Map.of(
                "enabled", fileSystemMCP.isEnabled(),
                "type", "filesystem"));
        sources.put("external", externalResourceMCP.getStatus());

        return ResponseEntity.ok(sources);
    }

    /**
     * List files from filesystem MCP
     * GET /api/rag/mcp/files?path=...
     */
    @GetMapping("/mcp/files")
    public ResponseEntity<?> listFiles(
            @RequestParam(defaultValue = ".") String path,
            @RequestParam(required = false) String pattern) {
        try {
            List<FileSystemMCPServer.FileInfo> files = fileSystemMCP.listFiles(path, pattern);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Fetch from external API via MCP
     * POST /api/rag/mcp/api/fetch
     */
    @PostMapping("/mcp/api/fetch")
    public ResponseEntity<?> fetchFromAPI(@RequestBody Map<String, Object> request) {
        try {
            String url = (String) request.get("url");
            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) request.get("headers");

            ExternalResourceMCPServer.ApiResponse response = externalResourceMCP.fetchFromAPI(url, headers);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get vector database statistics
     * GET /api/rag/stats/vector-db
     */
    @GetMapping("/stats/vector-db")
    public ResponseEntity<Map<String, Object>> getVectorDBStats() {
        return ResponseEntity.ok(vectorDatabaseService.getStats());
    }

    /**
     * Get embedding service statistics
     * GET /api/rag/stats/embeddings
     */
    @GetMapping("/stats/embeddings")
    public ResponseEntity<Map<String, Object>> getEmbeddingStats() {
        return ResponseEntity.ok(embeddingService.getCacheStats());
    }

    /**
     * Clear vector database
     * DELETE /api/rag/clear
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearVectorDB() {
        try {
            vectorDatabaseService.clearCollection();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Vector database cleared"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * Health check for RAG system
     * GET /api/rag/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("vectorDB", vectorDatabaseService.isConnected());
        health.put("fileSystemMCP", fileSystemMCP.isEnabled());
        health.put("externalMCP", externalResourceMCP.getStatus());
        health.put("status", vectorDatabaseService.isConnected() ? "healthy" : "degraded");

        return ResponseEntity.ok(health);
    }
}
