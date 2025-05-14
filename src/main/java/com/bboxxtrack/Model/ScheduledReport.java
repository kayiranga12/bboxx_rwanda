// src/main/java/com/bboxxtrack/Model/ScheduledReport.java
package com.bboxxtrack.Model;

import jakarta.persistence.*;

@Entity
public class ScheduledReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String cronExpression;   // e.g. "0 0 8 * * *" (every day at 08:00)
    private String templateKey;      // e.g. "completedTasksLast7"
    private String parameters;       // JSON blob or simple text
    private String format;           // "CSV" or "PDF"
    private String recipients;       // comma-separated emails
    private boolean enabled = true;

    // getters + setters omitted for brevity

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(String templateKey) {
        this.templateKey = templateKey;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
