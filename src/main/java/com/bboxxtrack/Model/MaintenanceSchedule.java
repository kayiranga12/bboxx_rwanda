package com.bboxxtrack.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "maintenance_schedule")
@Getter @Setter
public class MaintenanceSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // link to Customer
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    private LocalDate scheduleDate;
    private String purpose;
    private String status; // Scheduled, Completed

    // fields collected when completing a visit
    private String visitType;

    @Column(length = 1000)
    private String actionsPerformed;   // comma-separated

    private Integer satisfaction;      // 1–5 stars

    private String photoEvidencePath;  // stored filename

    // who completed it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by_user_id")
    private User completedBy;

    private LocalDateTime completedAt;

    @Column(length = 2000)
    private String reportNotes;

    @Column(name = "recurrence_rule")
    private String recurrenceRule;

    private boolean followUp;

    // no explicit back-reference here; if you wanted follow-ups tracked,
    // you could add a @OneToMany<> to a dedicated FollowUp entity


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDate getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(LocalDate scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getActionsPerformed() {
        return actionsPerformed;
    }

    public void setActionsPerformed(String actionsPerformed) {
        this.actionsPerformed = actionsPerformed;
    }

    public Integer getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(Integer satisfaction) {
        this.satisfaction = satisfaction;
    }

    public String getPhotoEvidencePath() {
        return photoEvidencePath;
    }

    public void setPhotoEvidencePath(String photoEvidencePath) {
        this.photoEvidencePath = photoEvidencePath;
    }

    public User getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(User completedBy) {
        this.completedBy = completedBy;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getReportNotes() {
        return reportNotes;
    }

    public void setReportNotes(String reportNotes) {
        this.reportNotes = reportNotes;
    }

    public String getRecurrenceRule() {
        return recurrenceRule;
    }

    public void setRecurrenceRule(String recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
    }

    public boolean isFollowUp() {
        return followUp;
    }

    public void setFollowUp(boolean followUp) {
        this.followUp = followUp;
    }

}
