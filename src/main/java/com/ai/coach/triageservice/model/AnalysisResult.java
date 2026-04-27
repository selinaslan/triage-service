package com.ai.coach.triageservice.model;

public record AnalysisResult(
        Sentiment sentiment,
        Priority priority,
        String summary,
        boolean requiresHuman,
        String aiSuggestedResponse
) {
}
