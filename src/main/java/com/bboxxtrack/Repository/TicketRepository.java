package com.bboxxtrack.Repository;

import com.bboxxtrack.Model.Ticket;
import com.bboxxtrack.Model.TicketStage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket,Long> {
    List<Ticket> findByCustomerId(Long cid);
    List<Ticket> findByPriority(String priority);
    List<Ticket> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String t, String d);
    List<Ticket> findByStage(TicketStage stage);
    // If you have a `@ManyToOne User assignedTo` field:
    List<Ticket> findAllByAssignedToUserIdIsNull();
}
