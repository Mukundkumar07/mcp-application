package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Groq API Configuration
 */
@Configuration
public class GroqConfig {

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.api.base-url:https://api.groq.com/openai/v1}")
    private String baseUrl;

    @Value("${groq.api.model:mixtral-8x7b-32768}")
    private String model;

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getModel() {
        return model;
    }
}
