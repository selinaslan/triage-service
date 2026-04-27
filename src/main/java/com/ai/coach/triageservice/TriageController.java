package com.ai.coach.triageservice;

import com.ai.coach.triageservice.model.AnalysisResult;
import com.ai.coach.triageservice.model.SupportTicket;
import com.ai.coach.triageservice.service.TriageService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/triage")
@Tag(name = "AI Triage Engine", description = "Endpoints for AI-powered support ticket analysis")
public class TriageController {

    private final TriageService service;
    private final OllamaEmbeddingModel embeddingModel; // Added this
    private final EmbeddingStore<TextSegment> vectorStore; // Added this

    public TriageController(TriageService service, OllamaEmbeddingModel embeddingModel, EmbeddingStore<TextSegment> vectorStore) {
        this.service = service;
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
    }


    @PostMapping("/analyze")
    @Operation(summary = "Analyze customer sentiment and priority",
            description = "Sends text to Llama3 to extract structured JSON data.")
    public AnalysisResult analyze(@RequestBody String userMessage) {
        return service.processAndStore(userMessage);
    }


    @Operation(
            summary = "Raw Semantic Search",
            description = "Converts the query into a vector and retrieves the top 3 mathematically similar text segments from the Vector Store."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved similar strings")
    @GetMapping("/search")
    public List<String> searchSimilar(@RequestParam String query) {
        // 1. Turn the user's search query into math
        var queryEmbedding = embeddingModel.embed(query).content();

        // 2. Find the top 3 "mathematically closest" tickets in Postgres
        var related = vectorStore.findRelevant(queryEmbedding, 3);

        return related.stream()
                .map(match -> match.embedded().text())
                .toList();
    }

    @Operation(
            summary = "Hybrid Ticket Retrieval",
            description = "Performs a semantic search and 'hydrates' the results by fetching full SupportTicket entities from the relational database."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved full ticket entities",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SupportTicket.class)))
    )
    @GetMapping("/tickets")
    public List<SupportTicket> search(@RequestParam String query) {
        return service.searchTickets(query);
    }
}
