// src/main/java/com/bboxxtrack/Service/TicketService.java
package com.bboxxtrack.Service;

import com.bboxxtrack.Model.Ticket;
import com.bboxxtrack.Model.TicketStage;
import com.bboxxtrack.Repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {
    @Autowired private TicketRepository repo;

    public Ticket save(Ticket t) {
        return repo.save(t);
    }

    public Ticket get(Long id) {
        return repo.findById(id).orElse(null);
    }

    public List<Ticket> all() {
        return repo.findAll();
    }

    public List<Ticket> search(String q) {
        return repo.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(q, q);
    }

    public List<Ticket> filterByPriority(String p) {
        return repo.findByPriority(p);
    }

    public List<Ticket> filterByStage(TicketStage stage) {
        return repo.findByStage(stage);
    }

    public List<Ticket> findAllByAssignedToUserIdIsNull() {
        return repo.findAllByAssignedToUserIdIsNull();
    }

    // Add this method to find ticket by ID
    public Ticket findById(Long ticketId) {
        return repo.findById(ticketId).orElse(null);
    }

    // Alternative with Optional return type (recommended)
    public Optional<Ticket> findByIdOptional(Long ticketId) {
        return repo.findById(ticketId);
    }
    // Get active tickets (not completed) for tracker form
    public List<Ticket> getActiveTicketsForTechnician(Long technicianId) {
        // Get tickets that are ASSIGNED or IN_PROGRESS
        List<TicketStage> activeStages = Arrays.asList(TicketStage.ASSIGNED, TicketStage.IN_PROGRESS);
        return repo.findByAssignedToUserIdAndStageIn(technicianId, activeStages);
    }

}
