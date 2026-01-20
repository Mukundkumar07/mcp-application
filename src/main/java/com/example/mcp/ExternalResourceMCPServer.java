package com.example.mcp;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * MCP Server for external resources
 * Supports: Databases, REST APIs, Cloud Storage, Web Scraping
 */
@Service
public class ExternalResourceMCPServer {

    private static final Logger logger = LoggerFactory.getLogger(ExternalResourceMCPServer.class);

    @Value("${mcp.database.enabled:false}")
    private boolean databaseEnabled;

    @Value("${mcp.api.enabled:true}")
    private boolean apiEnabled;

    @Value("${mcp.cloud-storage.enabled:false}")
    private boolean cloudStorageEnabled;

    private OkHttpClient httpClient;

    @PostConstruct
    public void initialize() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        logger.info("External Resource MCP Server initialized. Database: {}, API: {}, Cloud: {}",
                databaseEnabled, apiEnabled, cloudStorageEnabled);
    }

    // ==================== DATABASE OPERATIONS ====================

    /**
     * Execute a database query
     * Supports PostgreSQL, MySQL, SQLite
     */
    public List<Map<String, Object>> queryDatabase(String query, String connectionString) throws SQLException {
        if (!databaseEnabled) {
            throw new IllegalStateException("Database MCP is disabled");
        }

        List<Map<String, Object>> results = new ArrayList<>();

        try (java.sql.Connection conn = DriverManager.getConnection(connectionString);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }
        }

        logger.info("Database query returned {} rows", results.size());
        return results;
    }

    /**
     * Get database schema information
     */
    public List<String> getDatabaseTables(String connectionString) throws SQLException {
        if (!databaseEnabled) {
            throw new IllegalStateException("Database MCP is disabled");
        }

        List<String> tables = new ArrayList<>();

        try (java.sql.Connection conn = DriverManager.getConnection(connectionString)) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables(null, null, "%", new String[] { "TABLE" });

            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }

        logger.info("Found {} tables in database", tables.size());
        return tables;
    }

    // ==================== REST API OPERATIONS ====================

    /**
     * Fetch data from a REST API
     */
    public ApiResponse fetchFromAPI(String url, Map<String, String> headers) throws IOException {
        if (!apiEnabled) {
            throw new IllegalStateException("API MCP is disabled");
        }

        Request.Builder requestBuilder = new Request.Builder().url(url);

        // Add headers
        if (headers != null) {
            headers.forEach(requestBuilder::addHeader);
        }

        Request request = requestBuilder.build();

        try (Response response = httpClient.newCall(request).execute()) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatusCode(response.code());
            apiResponse.setHeaders(response.headers().toMultimap());

            if (response.body() != null) {
                apiResponse.setBody(response.body().string());
            }

            logger.info("API request to {} returned status {}", url, response.code());
            return apiResponse;
        }
    }

    /**
     * POST data to a REST API
     */
    public ApiResponse postToAPI(String url, String jsonBody, Map<String, String> headers) throws IOException {
        if (!apiEnabled) {
            throw new IllegalStateException("API MCP is disabled");
        }

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);

        // Add headers
        if (headers != null) {
            headers.forEach(requestBuilder::addHeader);
        }

        Request request = requestBuilder.build();

        try (Response response = httpClient.newCall(request).execute()) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatusCode(response.code());
            apiResponse.setHeaders(response.headers().toMultimap());

            if (response.body() != null) {
                apiResponse.setBody(response.body().string());
            }

            logger.info("API POST to {} returned status {}", url, response.code());
            return apiResponse;
        }
    }

    // ==================== WEB SCRAPING ====================

    /**
     * Scrape content from a web page
     */
    public String scrapeWebPage(String url) throws IOException {
        if (!apiEnabled) {
            throw new IllegalStateException("API MCP is disabled");
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.body() != null) {
                String html = response.body().string();
                // Basic HTML to text conversion (remove tags)
                String text = html.replaceAll("<[^>]*>", " ")
                        .replaceAll("\\s+", " ")
                        .trim();

                logger.info("Scraped {} characters from {}", text.length(), url);
                return text;
            }
        }

        return "";
    }

    // ==================== CLOUD STORAGE (Placeholder) ====================

    /**
     * Read from cloud storage (AWS S3, Google Drive, Dropbox)
     * This is a placeholder - actual implementation would require cloud SDK
     */
    public String readFromCloudStorage(String provider, String path, Map<String, String> credentials) {
        if (!cloudStorageEnabled) {
            throw new IllegalStateException("Cloud storage MCP is disabled");
        }

        logger.warn("Cloud storage integration not yet implemented for provider: {}", provider);
        throw new UnsupportedOperationException(
                "Cloud storage integration requires cloud provider SDK. " +
                        "Please implement for: " + provider);
    }

    /**
     * List files in cloud storage
     */
    public List<String> listCloudFiles(String provider, String path, Map<String, String> credentials) {
        if (!cloudStorageEnabled) {
            throw new IllegalStateException("Cloud storage MCP is disabled");
        }

        logger.warn("Cloud storage integration not yet implemented for provider: {}", provider);
        return new ArrayList<>();
    }

    // ==================== HELPER CLASSES ====================

    /**
     * API Response model
     */
    public static class ApiResponse {
        private int statusCode;
        private Map<String, List<String>> headers;
        private String body;

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public Map<String, List<String>> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, List<String>> headers) {
            this.headers = headers;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        @Override
        public String toString() {
            return "ApiResponse{statusCode=" + statusCode + ", bodyLength=" +
                    (body != null ? body.length() : 0) + "}";
        }
    }

    /**
     * Get service status
     */
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("databaseEnabled", databaseEnabled);
        status.put("apiEnabled", apiEnabled);
        status.put("cloudStorageEnabled", cloudStorageEnabled);
        return status;
    }
}
