// src/main/java/com/bboxxtrack/Service/SLAService.java
package com.bboxxtrack.Service;

import com.bboxxtrack.Model.Ticket;
import com.bboxxtrack.Model.TicketStage;     // ← import the enum
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class SLAService {

    private final TicketService ticketService;

    public SLAService(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /** Number of “open” tickets = those still in CREATED or ASSIGNED_TO_PM or ASSIGNED_TO_TECH, etc. **/
    public long getOpenCount() {
        return ticketService.all().stream()
                .filter(t -> t.getStage() != TicketStage.CLOSED) // ← treat “not CLOSED” as “open”
                .count();
    }

    /** Average response time in hours (first response) **/
    public double getAvgResponseHours() {
        List<Ticket> all = ticketService.all();
        return all.stream()
                .filter(t -> t.getFirstResponseAt() != null)
                .mapToDouble(t -> {
                    Duration d = Duration.between(t.getCreatedAt(), t.getFirstResponseAt());
                    return d.toMinutes() / 60.0;
                })
                .average()
                .orElse(0);
    }

    /** Average resolution time in hours **/
    public double getAvgResolutionHours() {
        List<Ticket> all = ticketService.all();
        return all.stream()
                .filter(t -> t.getClosedAt() != null)
                .mapToDouble(t -> {
                    Duration d = Duration.between(t.getCreatedAt(), t.getClosedAt());
                    return d.toMinutes() / 60.0;
                })
                .average()
                .orElse(0);
    }

    /** How many tickets took >48h to resolve **/
    public long getBreachesOver48() {
        return ticketService.all().stream()
                .filter(t -> t.getClosedAt() != null)
                .filter(t -> Duration.between(t.getCreatedAt(), t.getClosedAt())
                        .toHours() > 48)
                .count();
    }
}
