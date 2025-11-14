# CLAUDE.md - LangChain4j Spring Boot Demo Project Guide

## Project Overview

This is a **Developer Assistance Application** that combines Spring Boot, Spring AI, Vaadin, and Ollama to create an AI-powered chat interface with RAG (Retrieval Augmented Generation) capabilities. The application helps software engineers improve their skills through an AI assistant named "Devex" (Developer Experience).

**Project Name:** LangChain4jSpringbootDemo
**Application Name:** Developer Assistance
**Tech Stack:** Java 21, Spring Boot 3.2.4, Vaadin 24.3.8, Spring AI 0.8.1
**AI Integration:** Ollama (llama2 model)
**Build Tool:** Maven

---

## Architecture Overview

### Core Components

1. **Application Layer**
   - `Application.java` - Spring Boot entry point with Vaadin PWA configuration
   - Uses Dark theme (`Lumo.DARK`)
   - Configured as Progressive Web App (PWA)
   - Server-push enabled for real-time updates

2. **View Layer**
   - `MyDeveloperAssistanceView.java` - Main chat interface
   - Route: `""`(root path)
   - Uses Vaadin's MessageInput and MarkdownMessage components
   - Reactive UI with streaming responses

3. **Service Layer**
   - `ChatService.java` - Manages chat sessions and AI interactions
   - `PromptManagementService.java` - Handles conversation history and RAG retrieval
   - Session management with UUID-based chat IDs
   - Streaming responses using Reactor Flux

4. **Configuration Layer**
   - `EmbeddedVectorStoreConfig.java` - Configures in-memory vector store
   - Uses SimpleVectorStore (similar to H2 for databases)

5. **Component Layer**
   - `RagWorker.java` - Initializes RAG system on startup
   - Loads PDF documents into vector store
   - Handles Ollama model pulling

### Data Flow

```
User Input → MyDeveloperAssistanceView
  → ChatService.chat()
  → PromptManagementService.getSystemMessage() (retrieves context via RAG)
  → OllamaChatClient.stream() (sends to Ollama)
  → Flux<ChatResponse> → Streaming UI updates
```

---

## Directory Structure

```
LangChain4jSpringbootDeno/
├── src/
│   ├── main/
│   │   ├── java/com/ai/langchain4j/
│   │   │   ├── Application.java                    # Entry point
│   │   │   ├── views/
│   │   │   │   └── MyDeveloperAssistanceView.java  # Chat UI
│   │   │   ├── service/
│   │   │   │   ├── ChatService.java                # Chat orchestration
│   │   │   │   └── PromptManagementService.java    # History & RAG
│   │   │   ├── config/
│   │   │   │   └── EmbeddedVectorStoreConfig.java  # Vector store config
│   │   │   └── component/
│   │   │       └── RagWorker.java                  # RAG initialization
│   │   └── resources/
│   │       ├── application.properties              # App configuration
│   │       ├── system-qa.st                        # System prompt template
│   │       └── so_survey_2023.pdf                  # Knowledge base document
│   └── test/                                       # (No tests currently)
├── frontend/
│   └── generated/                                  # Vaadin generated files
├── pom.xml                                         # Maven configuration
├── package.json                                    # Frontend dependencies
├── tsconfig.json                                   # TypeScript config
├── info.txt                                        # Build/run commands
└── .gitignore
```

---

## Key Configuration Files

### application.properties
- **Server:** Port 8080 (configurable via `PORT` env var)
- **AI Model:** Ollama llama2 model
- **Ollama Base URL:** `http://localhost:11434` (configurable via `AI_OLLAMA_BASE_URL`)
- **Virtual Threads:** Enabled for better concurrency
- **Vaadin:** Auto-launch browser enabled

### pom.xml
- **Java Version:** 21
- **Spring Boot:** 3.2.4
- **Spring AI:** 0.8.1
- **Vaadin:** 24.3.8
- **Viritin:** 2.8.6 (for enhanced Vaadin components)
- **Key Dependencies:**
  - `spring-ai-ollama-spring-boot-starter` - Ollama integration
  - `spring-ai-tika-document-reader` - PDF document processing
  - `vaadin-spring-boot-starter` - Vaadin integration
  - `spring-boot-devtools` - Hot reload support

### Maven Profiles
- **Default:** Development mode with hot reload
- **Production (`-Pproduction`):** Optimized frontend build
- **Integration Test (`-Pit`):** Runs integration tests

---

## Development Workflows

### Prerequisites
1. **Java 21** (recommended: 22.0.1.fx-zulu via SDKMan)
2. **Maven** (for build automation)
3. **Node.js & npm** (for Vaadin frontend)
4. **Ollama** installed and running locally
5. **llama2 model** pulled in Ollama

### Setup Ollama
```bash
# Install Ollama (if not installed)
# Visit: https://ollama.ai/

# Pull the llama2 model
ollama pull llama2

# Start Ollama server (if not running)
ollama serve
```

### Build Commands

```bash
# Clean and install dependencies
mvn clean install

# Run in development mode (with hot reload)
mvn spring-boot:run

# Run with debug logging
mvn spring-boot:run -Dspring-boot.run.arguments=--debug

# Production build
mvn -Pproduction clean package

# Force production build (rebuild frontend)
mvn -Pproduction clean package -Dvaadin.forceProductionBuild

# Run packaged JAR
java -jar target/langchain4jspringbootdemo-0.1-SNAPSHOT.jar
```

### Development Server
- **URL:** http://localhost:8080
- **Auto-reload:** Enabled via spring-boot-devtools
- **Browser:** Opens automatically on startup

---

## Key Conventions for AI Assistants

### Code Style
1. **Package Structure:** Follow existing `com.ai.langchain4j.*` pattern
2. **Naming Conventions:**
   - Services: `*Service.java`
   - Views: `*View.java`
   - Config: `*Config.java`
   - Components: descriptive names (e.g., `RagWorker.java`)
3. **Logging:** Use SLF4J Logger (`LoggerFactory.getLogger()`)
4. **Dependency Injection:** Constructor-based injection (no `@Autowired` on fields)

### Spring AI Patterns
1. **Chat Sessions:**
   - Each session has a unique UUID-based `chatId`
   - Sessions are established via `ChatService.establishChat()`
   - Conversation history is maintained per session

2. **Prompt Management:**
   - System prompts use Mustache templates (`.st` files)
   - Variables: `{current_date}`, `{history}`, `{documents}`
   - Templates located in `src/main/resources/`

3. **RAG Implementation:**
   - Vector store initialized in `RagWorker.@PostConstruct`
   - Documents split using `TokenTextSplitter`
   - Similarity search via `vectorStore.similaritySearch(message)`
   - Results integrated into system prompt

4. **Streaming Responses:**
   - Use `chatClient.stream(prompt)` for reactive responses
   - Returns `Flux<ChatResponse>`
   - UI updates asynchronously via `appendMarkdownAsync()`

### Vaadin UI Patterns
1. **Routes:** Use `@Route("")` for main view
2. **Layout:** Extend `VerticalLayout` for vertical stacking
3. **Components:**
   - `MessageInput` for user input
   - `MarkdownMessage` for rendering chat messages (supports markdown)
   - Avatar colors from `Color.AVATAR_PRESETS[]`

4. **Event Handling:**
   - `messageInput.addSubmitListener()` for submit events
   - Access user input via `event.getValue()`

### Error Handling
1. **RagWorker:** Catches and logs initialization errors without failing startup
2. **Logging Levels:**
   - ERROR: Critical failures
   - WARN: Non-critical issues
   - DEBUG: Development information
   - INFO: General application flow (default)

### Configuration Management
1. **Environment Variables:**
   - `PORT` - Server port (default: 8080)
   - `AI_OLLAMA_BASE_URL` - Ollama server URL (default: http://localhost:11434)

2. **Resource Files:**
   - System prompts: `classpath:/system-qa.st`
   - Knowledge base: `classpath:/so_survey_2023.pdf`

---

## Testing Strategy

### Current State
- No test files currently exist in the project
- Testing framework dependencies are available:
  - `spring-boot-starter-test` (JUnit 5, Mockito, AssertJ)
  - `vaadin-testbench-junit5` (Vaadin UI testing)

### Recommended Testing Approach
1. **Unit Tests:**
   - Test services in isolation
   - Mock `OllamaChatClient` and `VectorStore`
   - Test session management in `PromptManagementService`

2. **Integration Tests:**
   - Use `-Pit` profile
   - Test full chat flow
   - Verify RAG document retrieval

3. **UI Tests:**
   - Use Vaadin TestBench
   - Test message submission and rendering
   - Verify streaming updates

---

## Common Tasks

### Adding New Features

1. **New Service:**
   ```java
   @Service
   public class MyService {
       private static final Logger logger = LoggerFactory.getLogger(MyService.class);

       public MyService(/* dependencies */) {
           // Constructor injection
       }
   }
   ```

2. **New View:**
   ```java
   @Route("myview")
   public class MyView extends VerticalLayout {
       public MyView(/* services */) {
           // Initialize components
       }
   }
   ```

3. **New Configuration:**
   ```java
   @Configuration
   public class MyConfig {
       @Bean
       public MyBean myBean() {
           return new MyBean();
       }
   }
   ```

### Modifying System Prompt
- Edit `src/main/resources/system-qa.st`
- Use Mustache syntax: `{variable_name}`
- Available variables: `current_date`, `history`, `documents`

### Changing AI Model
1. Update `application.properties`:
   ```properties
   spring.ai.ollama.chat.options.model=mistral
   ```
2. Ensure model is pulled: `ollama pull mistral`
3. Update `RagWorker.pullModelIfNeeded()` if needed

### Adding Documents to RAG
1. Place PDF in `src/main/resources/`
2. Update `RagWorker.java`:
   ```java
   @Value("classpath:/your-document.pdf") Resource pdfResource
   ```
3. Restart application to reindex

---

## Troubleshooting

### Common Issues

1. **Ollama Connection Failed:**
   - Ensure Ollama is running: `ollama serve`
   - Check URL: `AI_OLLAMA_BASE_URL` environment variable
   - Verify port 11434 is accessible

2. **Model Not Found:**
   - Pull model: `ollama pull llama2`
   - Check model name in `application.properties`

3. **Frontend Build Errors:**
   - Delete `node_modules/` and `package-lock.json`
   - Run `npm install`
   - Clear Vaadin cache: delete `frontend/generated/`

4. **Port Already in Use:**
   - Change port: `export PORT=8081` or update `application.properties`
   - Kill existing process: `lsof -ti:8080 | xargs kill`

5. **Vector Store Initialization Failed:**
   - Check PDF resource exists in `src/main/resources/`
   - Verify Tika dependencies are available
   - Check logs for specific error details

---

## Git Workflow

### Branch Strategy
- **Main Branch:** `main` (likely)
- **Feature Branches:** Create from main for new features
- **Current Branch:** `claude/claude-md-mhy3qc87t4s6a8oo-01DuNZ1tbhdwnDYrRhrG5DdZ`

### Commit Guidelines
1. Clean working directory before commits
2. Write descriptive commit messages
3. Group related changes together
4. Run `mvn clean install` before committing

### Recent Commits
```
57b76f7 - Update on st file.
e0b4ee1 - Working now.
7f60954 - Update.
d65fb7f - Merge branch 'main'
e6acd3f - Update.
```

---

## Performance Considerations

1. **Virtual Threads:** Enabled for improved concurrency
2. **Streaming Responses:** Reduces time-to-first-token
3. **In-Memory Vector Store:** Fast but limited to application memory
4. **Production Build:** Use `-Pproduction` for optimized frontend
5. **Connection Pooling:** Consider for production Ollama deployments

---

## Future Enhancements

1. **Persistent Vector Store:** Replace SimpleVectorStore with persistent solution (e.g., ChromaDB, Pinecone)
2. **Multi-Model Support:** Allow users to select different LLMs
3. **Document Upload:** Enable users to upload their own documents
4. **Authentication:** Add user authentication and session management
5. **Testing:** Add comprehensive unit and integration tests
6. **Model Caching:** Implement caching for frequently accessed embeddings
7. **Metrics:** Add application metrics and monitoring
8. **API Endpoints:** Expose REST API for chat functionality

---

## Resources

### Documentation
- **Spring AI:** https://docs.spring.io/spring-ai/reference/
- **Vaadin:** https://vaadin.com/docs/latest
- **Ollama:** https://ollama.ai/
- **Spring Boot:** https://spring.io/projects/spring-boot

### Key Files to Reference
- `info.txt` - Quick build/run commands
- `pom.xml` - Dependency management
- `application.properties` - Runtime configuration
- `system-qa.st` - AI assistant personality and instructions

---

## Quick Reference

### Start Development
```bash
# Ensure Ollama is running
ollama serve

# Run application
mvn spring-boot:run
```

### Access Application
- **URL:** http://localhost:8080
- **Chat Interface:** Immediately available on home page

### Production Deployment
```bash
# Build production JAR
mvn -Pproduction clean package

# Run JAR
java -jar target/langchain4jspringbootdemo-0.1-SNAPSHOT.jar
```

---

## Important Notes for AI Assistants

1. **Always check if Ollama is running** before making code changes that interact with AI
2. **Respect the existing architecture** - this is a reactive, streaming-based system
3. **Maintain conversation context** - the `PromptManagementService` is critical for chat quality
4. **Test RAG integration** when modifying vector store or document processing
5. **Consider streaming implications** when modifying chat responses
6. **Follow Spring Boot best practices** - constructor injection, proper logging, etc.
7. **Vaadin updates are async** - use proper reactive patterns
8. **Frontend is auto-generated** - don't manually edit `frontend/generated/`

---

**Last Updated:** 2025-11-14
**Version:** 0.1-SNAPSHOT
**Maintainer:** AI Assistant (Claude)
