package com.bboxxtrack.Model;

import jakarta.validation.constraints.NotNull;

/**
 * Simple DTO for binding the “Assign Ticket” form.
 */
public class TicketAssignmentDto {

    @NotNull(message = "Please select a ticket")
    private Long ticketId;

    @NotNull(message = "Please select a technician")
    private Long technicianId;

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }
}
