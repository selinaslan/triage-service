package com.ai.coach.triageservice.service;

import com.ai.coach.triageservice.model.AnalysisResult;
import com.ai.coach.triageservice.model.SupportTicket;
import com.ai.coach.triageservice.repository.SupportTicketRepository;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TriageService {
    private final TriageSpecialist specialist;
    private final SupportTicketRepository sqlRepository;
    private final EmbeddingStore<TextSegment> vectorStore;
    private final OllamaEmbeddingModel embeddingModel;

    public TriageService(TriageSpecialist specialist, SupportTicketRepository sqlRepository,
                         EmbeddingStore<TextSegment> vectorStore, OllamaEmbeddingModel embeddingModel) {
        this.specialist = specialist;
        this.sqlRepository = sqlRepository;
        this.vectorStore = vectorStore;
        this.embeddingModel = embeddingModel;
    }

    @Transactional
    public AnalysisResult processAndStore(String rawMessage) {
        // 1. Python Pre-processing
        String cleanMessage = cleanWithPython(rawMessage);

        // 2. Find similar past scenarios
        List<SupportTicket> similarTickets = searchTickets(cleanMessage);

        // 3. Create a context form past scenarios
        String context = similarTickets.stream()
                .map(t -> "Problem: " + t.getOriginalMessage() + " | Solution: " + t.getAiSuggestedResponse())
                .collect(Collectors.joining("\n"));

        // 4. AI Analysis
        AnalysisResult analysis = specialist.analyze(cleanMessage, context);

        // 5. SQL Persistence
        SupportTicket ticket = new SupportTicket();
        ticket.setOriginalMessage(cleanMessage);
        ticket.setSentiment(analysis.sentiment());
        ticket.setPriority(analysis.priority());
        ticket.setSummary(analysis.summary());
        ticket.setRequiresHuman(analysis.requiresHuman());
        sqlRepository.save(ticket);

        // 6. Vector Persistence (Searchable Math)
        TextSegment segment = TextSegment.from(cleanMessage);
        var embedding = embeddingModel.embed(segment).content();
        vectorStore.add(embedding, segment);

        return analysis;
    }

    public List<SupportTicket> searchTickets(String query) {

        var queryEmbedding = embeddingModel.embed( query).content();

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(20)
                .minScore(0.5)
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = vectorStore.search(searchRequest);

        List<String> matchedTexts = searchResult.matches().stream()
                .map(match -> match.embedded().text())
                .toList();

        searchResult.matches()
                .forEach(match -> {
                    System.out.println("Score: " + match.score() + " - Text: " + match.embedded().text());
                });
        return sqlRepository.findByOriginalMessageIn(matchedTexts);
    }

    public void storeInVectorDb(SupportTicket ticket) {
        String textToEmbed = String.format("Ticket ID: %s | Summary: %s | Message: %s",
                ticket.getId(), ticket.getSummary(), ticket.getOriginalMessage());

        TextSegment segment = TextSegment.from(textToEmbed,
                Metadata.from("ticket_id", ticket.getId().toString()));

        vectorStore.add(embeddingModel.embed(segment).content(), segment);
    }

    private String cleanWithPython(String rawText) {
        try {
            String scriptPath = new File("python/cleaner.py").getAbsolutePath();

            String pythonCmd = System.getProperty("os.name").toLowerCase().contains("win") ? "python" : "python3";

            ProcessBuilder pb = new ProcessBuilder(pythonCmd, scriptPath, rawText);
            pb.redirectErrorStream(true);
            Process p = pb.start();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String result = in.lines().collect(Collectors.joining("\n"));
                int exitCode = p.waitFor();
                return (exitCode == 0 && result != null && !result.isEmpty()) ? result : rawText;
            }
        } catch (Exception e) {
            System.err.println("Python cleaning failed, using raw text: " + e.getMessage());
            return rawText;
        }
    }
}