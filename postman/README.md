# Postman Collection - Triage Service API

This directory contains a Postman collection for testing all endpoints of the AI-Powered Support Triage Engine.

## 📥 How to Import the Collection

### Option 1: Direct Import in Postman
1. Open **Postman** application
2. Click **Import** button (top-left)
3. Select **Upload Files** tab
4. Choose `Triage-Service.postman_collection.json`
5. Click **Import**

### Option 2: Using Postman Link
1. Copy the file path: `postman/Triage-Service.postman_collection.json`
2. Open Postman
3. Click **Import** → **Folder/Files** → Select this file

## 📋 Collection Structure

The collection is organized into 4 main folders:

### 1. **Health & Status**
- Health Check endpoint to verify the application is running

### 2. **Triage Endpoints** (Core Analysis)
- **Analyze Ticket** - Analyze basic support ticket
- **Analyze Ticket - Positive** - Test positive sentiment detection
- **Analyze Ticket - With PII** - Test PII redaction (sensitive data)
- **Analyze Ticket - HTML Content** - Test HTML stripping

**Endpoint:** `POST /api/triage/analyze`

**Request Body:** Plain text string

**Response:** `AnalysisResult` object with:
```json
{
  "sentiment": "FRUSTRATED",
  "priority": "URGENT",
  "summary": "Customer unable to reset password"
}
```

### 3. **Search Endpoints** (Vector Search & Retrieval)
- **Semantic Search - Raw** - Get raw text results
- **Semantic Search - Login Problems** - Search for authentication issues
- **Semantic Search - Payment Issues** - Search for payment problems
- **Hybrid Ticket Retrieval** - Get full ticket entities
- **Hybrid Ticket Retrieval - Urgent Issues** - Search with full details

**Endpoints:**
- `GET /api/triage/search?query=<query>` - Returns list of text strings
- `GET /api/triage/tickets?query=<query>` - Returns full SupportTicket objects

**Response (Hybrid):** `List<SupportTicket>`
```json
[
  {
    "id": 1,
    "originalMessage": "...",
    "sentiment": "ANGRY",
    "priority": "URGENT",
    "summary": "...",
    "createdAt": "2026-04-17T10:30:00"
  }
]
```

### 4. **API Documentation**
- Links to Swagger UI and OpenAPI specs

## 🚀 Testing Workflow

### Step 1: Verify Application is Running
1. Open the **Health Check** request
2. Click **Send**
3. You should see `HTTP 200 OK` with status: `"UP"`

### Step 2: Test Ticket Analysis
1. Go to **Triage Endpoints** folder
2. Open any **Analyze Ticket** request
3. Click **Send**
4. Check the response for sentiment, priority, and summary

**Try different test cases:**
- Basic ticket → Expected: Various sentiments based on content
- Positive feedback → Expected: HAPPY sentiment
- PII data → Expected: Sensitive data should be redacted
- HTML content → Expected: HTML tags should be stripped

### Step 3: Test Semantic Search
1. Go to **Search Endpoints** folder
2. Open **Semantic Search - Raw** request
3. Modify the `query` parameter (optional)
4. Click **Send**
5. You'll get a list of semantically similar ticket texts

### Step 4: Test Hybrid Retrieval
1. Open **Hybrid Ticket Retrieval** request
2. Click **Send**
3. You'll get full SupportTicket entities with metadata

## 📝 Customizing Requests

### Changing Query Parameters
1. Click on a request (e.g., "Semantic Search - Raw")
2. In the **Params** tab, modify the `query` value
3. Click **Send**

Example custom queries:
```
- "billing problem"
- "account locked"
- "feature request"
- "error message"
```

### Changing Request Body
1. Click on an **Analyze Ticket** request
2. In the **Body** section, modify the text
3. Click **Send**

Example tickets to test:
```
✓ "My account was hacked! Help immediately!"
✓ "Just wanted to say thank you for the great support!"
✓ "I'm confused about how to use this feature"
✓ "The system is completely broken and I can't work"
```

## 🔗 Environment Variables (Optional)

The collection includes two variables:
- `base_url` = `http://localhost:8080`
- `api_path` = `/api/triage`

You can modify these to test against different environments:
1. Click on the collection name
2. Go to the **Variables** tab
3. Change the values as needed

## ⚠️ Prerequisites

Before running requests, ensure:
1. ✅ Spring Boot application is running: `mvn spring-boot:run`
2. ✅ PostgreSQL with pgvector is running: `docker ps | grep pgvector`
3. ✅ Ollama models are loaded: `ollama list` (should show llama3 and mxbai-embed-large)
4. ✅ Python preprocessor is executable: `chmod +x python/cleaner.py`

## 🐛 Troubleshooting

### "Connection refused" error
- Verify the application is running on port 8080
- Check `http://localhost:8080/actuator/health` in browser

### Empty search results
- First, analyze a few tickets using the "Analyze Ticket" endpoint
- Wait a moment for embeddings to be stored
- Then try search endpoints

### 500 Internal Server Error
- Check application logs for detailed error messages
- Verify PostgreSQL connection
- Verify Ollama models are pulled and running

## 📚 API Response Examples

### Analyze Endpoint Success
```
Status: 200 OK
{
  "sentiment": "FRUSTRATED",
  "priority": "URGENT",
  "summary": "User unable to reset password after multiple attempts"
}
```

### Search Endpoint Success
```
Status: 200 OK
[
  "My password reset isn't working either",
  "I've tried resetting my password 3 times",
  "Can't access my account password options"
]
```

### Hybrid Retrieval Success
```
Status: 200 OK
[
  {
    "id": 5,
    "originalMessage": "I can't reset my password...",
    "sentiment": "FRUSTRATED",
    "priority": "URGENT",
    "summary": "User unable to reset password",
    "createdAt": "2026-04-17T09:15:32"
  }
]
```

## 💡 Tips

1. **Test PII Redaction**: Use the "Analyze Ticket - With PII" request to verify sensitive data is redacted
2. **Test HTML Cleaning**: Use the "HTML Content" request to verify HTML tags are stripped
3. **Experiment with Queries**: Try different search queries to see semantic matching in action
4. **Monitor Logs**: Check Spring Boot console logs while testing to understand the processing pipeline
5. **Use Swagger UI**: For interactive documentation, visit `http://localhost:8080/swagger-ui.html`

---

**Last Updated:** April 2026
**Version:** 1.0

