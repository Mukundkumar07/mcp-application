package com.example.service;

import com.example.config.GroqConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

/**
 * Service for Groq API interactions
 */
@Service
public class GroqService {

    private final GroqConfig groqConfig;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public GroqService(GroqConfig groqConfig) {
        this.groqConfig = groqConfig;
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
    }

    /**
     * Send a message to Groq API and get a response
     *
     * @param message the message to send to Groq
     * @return the response from Groq API
     */
    public String chat(String message) throws IOException {
        if (groqConfig.getApiKey() == null || groqConfig.getApiKey().isEmpty()) {
            throw new IllegalStateException("Groq API key is not configured. Set GROQ_API_KEY environment variable.");
        }

        // Build request payload
        JsonObject requestBody = buildChatRequest(message);

        RequestBody body = RequestBody.create(requestBody.toString(), JSON);

        // Build HTTP request
        Request request = new Request.Builder()
            .url(groqConfig.getBaseUrl() + "/chat/completions")
            .addHeader("Authorization", "Bearer " + groqConfig.getApiKey())
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build();

        // Execute request
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                assert response.body() != null;
                String errorBody = response.body().string();
                throw new IOException("Groq API error: " + response.code() + " " + response.message() + " - " + errorBody);
            }

            assert response.body() != null;
            String responseBody = response.body().string();
            // Log the raw response for debugging
            System.out.println("Groq raw response: " + responseBody);
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            
            // Extract the message content from the response
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices != null && choices.size() > 0) {
                return choices.get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
            }
            
            throw new IOException("Unexpected response format from Groq API");
        }
    }

    /**
     * Build the chat request JSON object
     */
    private JsonObject buildChatRequest(String message) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", groqConfig.getModel());
        requestBody.addProperty("max_tokens", 1024);
        requestBody.addProperty("temperature", 0.7);
        
        JsonObject messageObj = new JsonObject();
        messageObj.addProperty("role", "user");
        messageObj.addProperty("content", message);
        
        JsonArray messages = new JsonArray();
        messages.add(messageObj);
        
        requestBody.add("messages", messages);
        
        return requestBody;
    }
}

