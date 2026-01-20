# RAG-MCP Application

A Spring Boot application implementing **Retrieval-Augmented Generation (RAG)** with **Model Context Protocol (MCP)** integration for accessing filesystems, databases, REST APIs, and web content using Groq's LLM API.

## 🌟 Features

### RAG Capabilities
- ✅ **Context-Enhanced AI Responses** - Retrieves relevant documents before generating responses
- ✅ **Vector Similarity Search** - In-memory cosine similarity search (upgradable to Qdrant)
- ✅ **Smart Document Chunking** - Sentence-based chunking with configurable overlap
- ✅ **Multi-Source Ingestion** - Filesystem, databases, web pages, and APIs

### MCP Integration
- ✅ **Filesystem Access** - Secure file reading with Apache Tika (PDF, DOCX, TXT, MD, etc.)
- ✅ **Database Queries** - PostgreSQL, MySQL, SQLite support
- ✅ **REST API Integration** - GET/POST requests with custom headers
- ✅ **Web Scraping** - Extract text from web pages

### AI Integration
- ✅ **Groq API** - Fast inference with llama-3.3-70b-versatile
- ✅ **Embedding Generation** - 384-dimensional vectors (simulated, ready for real models)
- ✅ **Caching** - Embedding cache for performance

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Groq API key (already configured)

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/Mukundkumar07/mcp-application.git
cd mcp-application
```

2. **Build the project**
```bash
mvn clean install
```

3. **Run the application**
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📖 API Documentation

### RAG Chat
```bash
POST http://localhost:8080/api/rag/chat
Content-Type: application/json

{
  "message": "What are the main features?",
  "topK": 5
}
```

### Document Ingestion

**From Filesystem:**
```bash
POST http://localhost:8080/api/rag/ingest/filesystem
Content-Type: application/json

{
  "path": "my-docs",
  "filePattern": ".*\\.md"
}
```

**From Database:**
```bash
POST http://localhost:8080/api/rag/ingest/database
Content-Type: application/json

{
  "query": "SELECT * FROM articles",
  "connectionString": "jdbc:postgresql://localhost:5432/mydb?user=user&password=pass"
}
```

**From Web:**
```bash
POST http://localhost:8080/api/rag/ingest/web
Content-Type: application/json

{
  "url": "https://example.com/article"
}
```

### Search Documents
```bash
GET http://localhost:8080/api/rag/search?query=machine learning&topK=10
```

### System Management
```bash
# Health check
GET http://localhost:8080/api/rag/health

# Vector DB stats
GET http://localhost:8080/api/rag/stats/vector-db

# Clear database
DELETE http://localhost:8080/api/rag/clear
```

## ⚙️ Configuration

Edit `src/main/resources/application.properties`:

```properties
# Groq API
groq.api.key=your-groq-api-key
groq.api.model=llama-3.3-70b-versatile

# Vector Database
vector.db.enabled=false  # Set to true when using Qdrant
vector.db.host=localhost
vector.db.port=6333

# MCP Filesystem
mcp.filesystem.enabled=true
mcp.filesystem.root-path=/path/to/documents
mcp.filesystem.allowed-extensions=pdf,txt,md,docx,java,py,json,xml,html,css,js

# MCP External Resources
mcp.database.enabled=false  # Enable for database access
mcp.api.enabled=true
mcp.cloud-storage.enabled=false

# RAG Settings
rag.chunk-size=512
rag.chunk-overlap=50
rag.retrieval-top-k=5
rag.max-context-length=4000
```

## 🏗️ Architecture

```
┌─────────────┐
│    User     │
└──────┬──────┘
       │ HTTP
       ▼
┌─────────────────────┐
│   RAG Controller    │
└──────┬──────────────┘
       │
       ├──────────────────────┐
       │                      │
       ▼                      ▼
┌─────────────┐      ┌──────────────────┐
│ RAG Service │      │ Ingestion Service│
└──────┬──────┘      └────────┬─────────┘
       │                      │
       ├──────┬───────────────┼──────────┐
       │      │               │          │
       ▼      ▼               ▼          ▼
  ┌────────┐ ┌──────┐  ┌──────────┐ ┌─────────┐
  │Embedding│ │Vector│  │Filesystem│ │External │
  │ Service│ │  DB  │  │   MCP    │ │   MCP   │
  └────────┘ └──┬───┘  └────┬─────┘ └────┬────┘
              │             │            │
              ▼             ▼            ▼
         ┌─────────┐  ┌─────────┐  ┌─────────┐
         │ Groq API│  │  Files  │  │DB/API/Web│
         └─────────┘  └─────────┘  └─────────┘
```

## 📁 Project Structure

```
mcp-application/
├── src/main/java/com/example/
│   ├── controller/
│   │   ├── HelloController.java       # Original Groq endpoints
│   │   └── RAGController.java         # RAG & MCP endpoints
│   ├── service/
│   │   ├── GroqService.java           # Groq API integration
│   │   ├── EmbeddingService.java      # Text embeddings
│   │   ├── VectorDatabaseService.java # Vector storage
│   │   ├── DocumentChunker.java       # Text chunking
│   │   ├── RAGService.java            # RAG orchestration
│   │   └── DocumentIngestionService.java
│   ├── mcp/
│   │   ├── FileSystemMCPServer.java   # Filesystem access
│   │   └── ExternalResourceMCPServer.java # DB/API/Web
│   ├── model/
│   │   ├── Document.java
│   │   ├── DocumentChunk.java
│   │   ├── RAGRequest.java
│   │   └── RAGResponse.java
│   └── config/
│       └── GroqConfig.java
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

## 🧪 Testing

### 1. Create test documents
```bash
mkdir -p /path/to/documents/test
echo "Spring Boot is a Java framework." > /path/to/documents/test/spring.txt
echo "RAG combines retrieval and generation." > /path/to/documents/test/rag.txt
```

### 2. Ingest documents
```bash
curl -X POST http://localhost:8080/api/rag/ingest/filesystem \
  -H "Content-Type: application/json" \
  -d '{"path": "test"}'
```

### 3. Query with RAG
```bash
curl -X POST http://localhost:8080/api/rag/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What is Spring Boot?", "topK": 3}'
```

## 🔧 Advanced Setup

### Enable Qdrant Vector Database
```bash
# Start Qdrant
docker run -p 6333:6333 qdrant/qdrant

# Update application.properties
vector.db.enabled=true
```

### Enable Database Access
```properties
mcp.database.enabled=true
```

## 📚 Dependencies

- **Spring Boot 3.2.1** - Application framework
- **Groq API** - LLM inference
- **Apache Tika 2.9.1** - Document parsing (PDF, DOCX, etc.)
- **OkHttp 4.12.0** - HTTP client
- **Qdrant Client 1.7.0** - Vector database (optional)
- **DJL 0.25.0** - Deep learning library for embeddings

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📝 License

This project is open source and available under the MIT License.

## 👤 Author

**Mukund Kumar**
- GitHub: [@Mukundkumar07](https://github.com/Mukundkumar07)

## 🙏 Acknowledgments

- [Groq](https://groq.com/) for fast LLM inference
- [Apache Tika](https://tika.apache.org/) for document processing
- [Model Context Protocol](https://modelcontextprotocol.io/) for standardized context access

---

**Built with ❤️ using Spring Boot, RAG, and MCP**
