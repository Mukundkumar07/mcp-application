# MCP Application - Groq API Integration

A Spring Boot REST API application with Groq AI integration.

## Project Summary

This is a fully functional Spring Boot application configured with Groq API support for AI-powered interactions.

### Technology Stack

- **Java 17**
- **Spring Boot 3.2.1**
- **Maven Build System**
- **Groq API Integration**
- **OkHttp3 for HTTP Client**
- **Gson for JSON Processing**
- **H2 In-Memory Database**
- **Spring Data JPA**

## Dependencies Added

```xml
<!-- OkHttp for HTTP Client -->
<dependency>
  <groupId>com.squareup.okhttp3</groupId>
  <artifactId>okhttp</artifactId>
  <version>4.11.0</version>
</dependency>

<!-- Gson for JSON Processing -->
<dependency>
  <groupId>com.google.code.gson</groupId>
  <artifactId>gson</artifactId>
  <version>2.10.1</version>
</dependency>

<!-- Retrofit for REST Client -->
<dependency>
  <groupId>com.squareup.retrofit2</groupId>
  <artifactId>retrofit</artifactId>
  <version>2.9.0</version>
</dependency>

<dependency>
  <groupId>com.squareup.retrofit2</groupId>
  <artifactId>converter-gson</artifactId>
  <version>2.9.0</version>
</dependency>

<!-- Spring Boot WebFlux -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

## Configuration

**File:** `src/main/resources/application.properties`

```properties
spring.application.name=mcp-application
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
server.port=8080

# Groq API Configuration
groq.api.key=${GROQ_API_KEY:your-groq-api-key-here}
groq.api.base-url=https://api.groq.com/openai/v1
groq.api.model=llama-3.3-70b-versatile
```

## Project Structure

```
mcp-application/
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── App.java                      # Main Spring Boot Application
│   │   │   ├── config/
│   │   │   │   └── GroqConfig.java          # Groq API Configuration
│   │   │   ├── controller/
│   │   │   │   └── HelloController.java     # REST Controller with Groq Chat
│   │   │   └── service/
│   │   │       └── GroqService.java         # Groq API Service
│   │   └── resources/
│   │       └── application.properties       # Application Configuration
│   └── test/
│       └── java/com/example/
│           └── AppTest.java
├── target/
│   └── mcp-application-1.0-SNAPSHOT.jar     # Built JAR file
├── pom.xml                                  # Maven Configuration
└── README.md                                # Documentation
```

## Key Classes

### GroqConfig.java
Configuration class that loads Groq API settings from application properties.

```java
@Configuration
public class GroqConfig {
    @Value("${groq.api.key:}")
    private String apiKey;
    
    @Value("${groq.api.base-url:https://api.groq.com/openai/v1}")
    private String baseUrl;
    
    @Value("${groq.api.model:mixtral-8x7b-32768}")
    private String model;
}
```

### GroqService.java
Service class for interacting with Groq API.

```java
@Service
public class GroqService {
    public String chat(String message) throws IOException {
        // Sends message to Groq API and returns response
        // Uses OkHttp3 for HTTP communication
        // Handles JSON serialization with Gson
    }
}
```

### HelloController.java
REST Controller with the following endpoints:

- **GET** `http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What is AI?"}' - Returns a greeting message
- **GET** `/api/health` - Returns application health status
- **POST** `/api/chat` - Sends a message to Groq AI and returns the response

**Example Request:**
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What is artificial intelligence?"}'
```

**Example Response:**
```json
{
  "response": "Artificial intelligence (AI) refers to computer systems..."
}
```

## Building and Running

### Build the Project
```bash
cd /Users/mukundkumar/MCP-APPLICATION/mcp-application
mvn clean install
```

### Run the Application
```bash
mvn spring-boot:run
```

### Or Run the JAR file
```bash
java -jar target/mcp-application-1.0-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## API Endpoints

### 1. Health Check
```bash
GET /api/health
```
Response: `Application is running!`

### 2. Hello Endpoint
```bash
GET /api/hello
```
Response: `Hello from Spring Boot!`

### 3. Chat with Groq AI - GET Method
```bash
GET /api/chat?message=Your%20question%20here
```

Example:
```bash
curl "http://localhost:8080/api/chat?message=What%20is%20AI"
```

Response:
```json
{
  "response": "Artificial Intelligence (AI) refers to the development of computer systems..."
}
```

### 4. Chat with Groq AI - POST Method
```bash
POST /api/chat
Content-Type: application/json

{
  "message": "Your question here"
}
```

Example:
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Explain machine learning"}'
```

Response:
```json
{
  "response": "Machine learning is a subset of artificial intelligence..."
}
```

## Error Handling

If Groq API key is not configured:
```json
{
  "error": "Groq API key is not configured. Set GROQ_API_KEY environment variable."
}
```

If API communication fails:
```json
{
  "error": "Error communicating with Groq API: [error details]"
}
```

## Development Tools

- **Spring DevTools** - For automatic restart and live reload during development
- **H2 Console** - Access at `http://localhost:8080/h2-console` for database management

## Testing

```bash
mvn test
```

## Groq API Information

- **API Base URL:** https://api.groq.com/openai/v1
- **Model:** llama-3.3-70b-versatile (Updated - stable and performant)
- **Max Tokens:** 1024
- **Temperature:** 0.7
- **Endpoint:** /chat/completions

## Groq API Models Available

The following models are available through Groq's API:
- `llama-3.3-70b-versatile` - **Recommended** - Stable, versatile, and performant
- `llama-3.2-90b-vision-preview` - Vision and text capabilities
- `mixtral-8x7b-32768` - ⚠️ DECOMMISSIONED
- `llama2-70b-4096` - Not available with this API key
- Other models may be available, check Groq docs for current list

For more information about Groq API and available models, visit: https://console.groq.com/docs/speech-text

## Next Steps

1. Test the API endpoints using curl or Postman
2. Add database entities and repositories for persistence
3. Implement additional business logic
4. Add authentication and authorization
5. Configure deployment settings

## Build Status

✅ **Build Successful** - Project compiles without errors
✅ **JAR Created** - `target/mcp-application-1.0-SNAPSHOT.jar`
✅ **Groq Integration** - Ready to use

## Notes

- ✅ **API Key Configured** - The Groq API key is embedded in the properties file for demonstration
- ✅ **Tested and Working** - Both GET and POST endpoints have been tested successfully
- ✅ **Model Verified** - llama-3.3-70b-versatile is active and working
- For production, use environment variables: `GROQ_API_KEY`
- Update the model name in `application.properties` to use different Groq models
- The service uses OpenAI-compatible API format for Groq integration
- If you get a "model decommissioned" error, update the model to `llama-3.3-70b-versatile` or check Groq docs for available models
