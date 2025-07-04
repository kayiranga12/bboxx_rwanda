package com.bboxxtrack.Model;

public enum TicketStage {
    CREATED,           // Just created by Support, not yet assigned to a PM
    ASSIGNED_TO_PM,   // Assigned to a Project Manager
    ASSIGNED_TO_TECH,
    COMPLETED,
    IN_PROGRESS,
    ASSIGNED,
    CLOSED
}
