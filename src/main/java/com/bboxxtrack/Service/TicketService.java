// src/main/java/com/bboxxtrack/Service/TicketService.java
package com.bboxxtrack.Service;

import com.bboxxtrack.Model.Ticket;
import com.bboxxtrack.Model.TicketStage;
import com.bboxxtrack.Repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
