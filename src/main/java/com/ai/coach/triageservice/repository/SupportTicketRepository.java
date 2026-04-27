package com.ai.coach.triageservice.repository;

import com.ai.coach.triageservice.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByOriginalMessageIn(List<String> messages);
}