package com.bboxxtrack.Controller;


// Simple DTO to bind our assignment form
public class TicketAssignmentDto {
    private Long ticketId;
    private Long technicianId;

    // getters & setters
    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    public Long getTechnicianId() { return technicianId; }
    public void setTechnicianId(Long technicianId) { this.technicianId = technicianId; }
}
