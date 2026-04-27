# 🤖 AI-Powered Support Triage Engine

A sophisticated backend microservice that automates customer support workflows using **Large Language Models (LLMs)** and **Vector Databases**. This project demonstrates a production-grade integration of Spring Boot, Python, and local AI models to categorize, prioritize, and search support tickets semantically.

## 📑 Table of Contents
- [High-Level Architecture](#-high-Level-architecture)
- [Key Features](#-key-features)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [Environment Variables](#-environment-variables)
- [Running the Project](#-running-the-project)
- [API Documentation](#-api-documentation)
- [Troubleshooting](#-troubleshooting)

---


## 🏗 High-Level Architecture

The system implements a Hybrid Microservices-lite Architecture, strategically separating concerns:

- Java/Spring Boot Layer: Orchestrates the workflow, manages transactional integrity, and interfaces with PGVector for RAG (Retrieval-Augmented Generation).

- Python Intelligence Layer: Acts as a security and sanitization gateway. Using NER (Named Entity Recognition) and NLP libraries, it strips HTML and masks PII (Personally Identifiable Information) before data reaches the LLM.

- Vector Database (PGVector): Provides the "Corporate Memory" by storing and searching through 100+ historical support cases to assist the AI in decision making.

## 🌟 Key Features
- **Semantic Triage:** Uses **Llama3** to analyze sentiment, urgency, and generate summaries.
- **Polyglot Pipeline:** Java orchestrates the flow while **Python** handles specialized text pre-processing (HTML stripping & PII redaction).
- **Hybrid Search Architecture:**
  - **PGVector:** For high-dimensional semantic search.
  - **PostgreSQL:** For structured relational data storage.
- **OpenAPI Integration:** Fully documented endpoints via Swagger UI.

---

## 🛠 Tech Stack
- **Backend:** Java 21, Spring Boot 3.3.x, LangChain4j.
- **AI/LLM:** Ollama (Llama3 for reasoning, mxbai-embed-large for embeddings).
- **Database:** PostgreSQL with **PGVector** extension.
- **Preprocessing:** Python 3.x (Regex-based cleaning).
- **Documentation:** SpringDoc OpenAPI (Swagger).

---

## 🚀 Getting Started

### 1. Prerequisites
- **Java 21+:** Required for Spring Boot 3.3.x
- **Docker & Docker Compose:** To run PostgreSQL with PGVector.
- **Ollama:** Download from [ollama.com](https://ollama.com) (running llama3 and mxbai-embed-large).
- **Python 3.x:** Ensure `python3` is in your PATH.

### 2. Infrastructure Setup

#### Start PostgreSQL with PGVector

```bash
docker run --name pgvector-container \
  -e POSTGRES_USER=myuser \
  -e POSTGRES_PASSWORD=mypassword \
  -e POSTGRES_DB=triagedb \
  -p 5432:5432 \
  -d ankane/pgvector
```

**Alternative image (PostgreSQL 16):**

```bash
docker run --name postgres-ai \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d pgvector/pgvector:pg16
```

**Enable pgvector Extension:**

```bash
docker exec -it pgvector-container psql -U myuser -d triagedb -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

Or if using the alternative image:

```bash
docker exec -it postgres-ai psql -U postgres -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

**Verify Database is Running:**

```bash
docker ps | grep pgvector
docker exec -it pgvector-container psql -U myuser -d triagedb -c "select 1;"
```

#### Pull & Run Ollama Models

```bash
ollama run llama3
ollama pull mxbai-embed-large
```

**Or to just pull without running:**

```bash
ollama pull llama3
ollama pull mxbai-embed-large
```

**Verification:** Visit [http://localhost:11434](http://localhost:11434) in your browser. It should display "Ollama is running".

**To stop Ollama:** Quit the Ollama app or use `Ctrl+C` if running in terminal.

#### Configure Python Preprocessor

The system uses a Python subprocess for security and data cleaning (PII Redaction).

**Set Permissions (Mac/Linux):**

```bash
chmod +x python/cleaner.py
```

**Verify the Script:**

Test the cleaner manually to ensure Python is configured correctly:

```bash
python3 python/cleaner.py "<html>Support for card 4444-5555-6666-7777</html>"
```

**Expected Output:**
```
Support for card [REDACTED]
```

---

## 🌳 Environment Variables

Create a `.env` file in the project root (optional, for custom configurations):

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/triagedb
SPRING_DATASOURCE_USERNAME=myuser
SPRING_DATASOURCE_PASSWORD=mypassword
OLLAMA_BASE_URL=http://localhost:11434
```

---

## 💻 Running the Project

### 1. Build with Maven

```bash
mvn clean install -DskipTests
```

### 2. Launch Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080` by default.

---

## 📚 API Documentation

Once the application is running, access the Swagger UI for interactive API documentation:

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Spec:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Health Check:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## 🔧 Troubleshooting

### Issue: PostgreSQL connection fails
- Ensure Docker container is running: `docker ps | grep pgvector`
- Verify credentials match in `application.yaml`
- Check if port 5432 is not in use by another service
- Confirm the container name is `postgres-ai` or `pgvector-container`

### Issue: pgvector extension not enabled
- Create the extension manually:
  ```bash
  docker exec -it postgres-ai psql -U postgres -c "CREATE EXTENSION IF NOT EXISTS vector;"
  ```
- Verify the extension exists:
  ```bash
  docker exec -it postgres-ai psql -U postgres -c "\dx"
  ```

### Issue: Ollama model errors
- Verify Ollama is running by visiting [http://localhost:11434](http://localhost:11434)
- Ensure models are pulled: `ollama list`
- Re-pull models if needed: `ollama pull llama3 && ollama pull mxbai-embed-large`

### Issue: Python script fails
- Verify Python 3.x is installed: `python3 --version`
- Check file permissions: `ls -l python/cleaner.py`
- Test manually with the verification command in Section 2

### Issue: Port already in use
- **Port 5432 (PostgreSQL):** Stop the conflicting service or change Docker port mapping
- **Port 8080 (Spring Boot):** Change the port in `application.yaml`:
  ```yaml
  server:
    port: 8081
  ```
- **Port 11434 (Ollama):** Stop the Ollama service and restart it

### Issue: Docker container name conflicts
- List running containers: `docker ps -a`
- Remove old container: `docker rm pgvector-container` or `docker rm postgres-ai`
- Recreate with the docker run command

---

