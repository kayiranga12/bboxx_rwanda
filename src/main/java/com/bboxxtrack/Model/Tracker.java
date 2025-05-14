package com.bboxxtrack.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tracker")
public class Tracker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String gpsCoordinates;
    private String statusUpdate;
    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }

    // getters + setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getGpsCoordinates() { return gpsCoordinates; }
    public void setGpsCoordinates(String gpsCoordinates) { this.gpsCoordinates = gpsCoordinates; }

    public String getStatusUpdate() { return statusUpdate; }
    public void setStatusUpdate(String statusUpdate) { this.statusUpdate = statusUpdate; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
