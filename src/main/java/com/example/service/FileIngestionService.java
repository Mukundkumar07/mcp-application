package com.example.service;

import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileIngestionService {
    private final OkHttpClient httpClient = new OkHttpClient();
    private final String qdrantUrl = "http://localhost:6333";

    /**
     * Ingest all .txt files from a directory, chunk, embed, and store in Qdrant
     */
    public void ingestDirectory(String dirPath) throws Exception {
        File dir = new File(dirPath);
        for (File file : FileUtils.listFiles(dir, new String[]{"txt"}, true)) {
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            List<String> chunks = chunkText(content, 500); // 500 chars per chunk
            for (String chunk : chunks) {
                float[] embedding = embedText(chunk); // Placeholder for embedding
                storeInQdrant(chunk, embedding, file.getName());
            }
        }
    }

    /**
     * Split text into chunks of maxLength
     */
    private List<String> chunkText(String text, int maxLength) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(text.length(), start + maxLength);
            chunks.add(text.substring(start, end));
            start = end;
        }
        return chunks;
    }

    /**
     * Placeholder: Replace with real embedding API call
     */
    private float[] embedText(String text) {
        // TODO: Call Groq/OpenAI embedding API and return the vector
        // For now, return a dummy vector
        float[] dummy = new float[1536];
        for (int i = 0; i < dummy.length; i++) dummy[i] = 0.1f;
        return dummy;
    }

    /**
     * Store chunk and embedding in Qdrant
     */
    private void storeInQdrant(String chunk, float[] embedding, String fileName) throws Exception {
        // Qdrant upsert REST API
        String id = UUID.randomUUID().toString();
        StringBuilder vectorBuilder = new StringBuilder();
        vectorBuilder.append("[");
        for (int i = 0; i < embedding.length; i++) {
            vectorBuilder.append(embedding[i]);
            if (i < embedding.length - 1) vectorBuilder.append(",");
        }
        vectorBuilder.append("]");

        String json = "{\n" +
                "  \"points\": [\n" +
                "    {\n" +
                "      \"id\": \"" + id + "\",\n" +
                "      \"vector\": " + vectorBuilder.toString() + ",\n" +
                "      \"payload\": {\n" +
                "        \"text\": \"" + chunk.replace("\"", "\\\"") + "\",\n" +
                "        \"file\": \"" + fileName.replace("\"", "\\\"") + "\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(qdrantUrl + "/collections/rag_collection/points?wait=true")
                .post(body)
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to upsert point: " + response);
            }
        }
    }
}
