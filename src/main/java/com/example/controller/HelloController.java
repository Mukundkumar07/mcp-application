package com.example.controller;

import com.example.service.GroqService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample REST Controller with Groq API integration
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    private final GroqService groqService;

    public HelloController(GroqService groqService) {
        this.groqService = groqService;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Spring Boot!";
    }

    @GetMapping("/health")
    public String health() {
        return "Application is running!";
    }

    @GetMapping("/chat")
    public Map<String, String> chatGet(@RequestParam(value = "message", required = false) String message) {
        return processChatMessage(message);
    }

    /**
     * POST /chat with query param: returns plain string answer
     */
    @PostMapping("/chat")
    public org.springframework.http.ResponseEntity<String> aiResponse(@RequestParam("query") String query) {
        try {
            String response = groqService.chat(query);
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * MCP-compatible endpoint: accepts { "prompt": "..." } and returns { "response": "..." }
     */
    @PostMapping("/mcp")
    public Map<String, Object> mcpChat(@RequestBody Map<String, Object> mcpRequest) {
        Map<String, Object> result = new HashMap<>();
        try {
            String prompt = mcpRequest.getOrDefault("prompt", "").toString();
            if (prompt.isEmpty()) {
                result.put("error", "Prompt is required in the MCP request body.");
                return result;
            }
            String answer = groqService.chat(prompt);
            result.put("response", answer);
            result.put("status", "success");
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("status", "error");
        }
        return result;
    }

    /**
     * MCP-compatible GET endpoint: accepts ?query=... and returns { "response": "..." }
     */
    @GetMapping("/mcp")
    public Map<String, Object> mcpChatGet(@RequestParam(value = "query", required = false) String query) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (query == null || query.isEmpty()) {
                result.put("error", "Query parameter 'query' is required.");
                result.put("status", "error");
                return result;
            }
            String answer = groqService.chat(query);
            result.put("response", answer);
            result.put("status", "success");
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("status", "error");
        }
        return result;
    }

    private Map<String, String> processChatMessage(String message) {
        try {
            if (message == null || message.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Message is required. Use ?message=<your_message> or POST with {\"message\": \"<your_message>\"}");
                return error;
            }

            String response = groqService.chat(message);
            
            Map<String, String> result = new HashMap<>();
            result.put("response", response);
            return result;
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error communicating with Groq API: " + e.getMessage());
            return error;
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }
}
