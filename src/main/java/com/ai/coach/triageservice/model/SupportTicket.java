package com.ai.coach.triageservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "support_tickets")
public class SupportTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String originalMessage;

    @Enumerated(EnumType.STRING)
    private Sentiment sentiment;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private String summary;

    @Column(name = "REQUIRES_HUMAN")
    private boolean requiresHuman;

    @Column(columnDefinition = "TEXT")
    private String aiSuggestedResponse;

    private LocalDateTime createdAt = LocalDateTime.now();
}