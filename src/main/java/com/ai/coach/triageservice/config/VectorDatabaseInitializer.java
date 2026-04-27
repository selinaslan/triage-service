package com.ai.coach.triageservice.config;

import com.ai.coach.triageservice.repository.SupportTicketRepository;
import com.ai.coach.triageservice.service.TriageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class VectorDatabaseInitializer implements CommandLineRunner {

    private final SupportTicketRepository sqlRepository;
    private final TriageService triageService;

    public VectorDatabaseInitializer(SupportTicketRepository sqlRepository,
                                     TriageService triageService) {
        this.sqlRepository = sqlRepository;
        this.triageService = triageService;
    }

    @Override
    public void run(String... args) {

        long ticketCount = sqlRepository.count();
        System.out.println("Startup Sync: Found " + ticketCount + " tickets in SQL.");

        if (ticketCount > 0) {
            sqlRepository.findAll().forEach(triageService::storeInVectorDb);
            System.out.println("All tickets have been synchronized to Vector DB!");
        }
    }
}
