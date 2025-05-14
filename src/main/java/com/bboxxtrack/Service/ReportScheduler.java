// src/main/java/com/bboxxtrack/Service/ReportScheduler.java
package com.bboxxtrack.Service;

import com.bboxxtrack.Model.ScheduledReport;
import com.bboxxtrack.Repository.ScheduledReportRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

@Component
public class ReportScheduler {

    @Autowired private ScheduledReportRepository repo;
    @Autowired private TaskScheduler scheduler;
    @Autowired private ReportGenerationService genSvc;
    @Autowired private EmailService emailSvc;

    @PostConstruct
    public void scheduleAll() {
        for (ScheduledReport rpt : repo.findByEnabledTrue()) {
            scheduler.schedule(() -> runReport(rpt), new CronTrigger(rpt.getCronExpression()));
        }
    }

    private void runReport(ScheduledReport rpt) {
        try {
            byte[] data = genSvc.generate(rpt);
            String subj = "Automated Report: "+rpt.getName();
            String body = "Please find attached: "+rpt.getName();
            String mime = rpt.getFormat().equalsIgnoreCase("PDF")
                    ? "application/pdf"
                    : "text/csv";
            for (String to : rpt.getRecipients().split("\\s*,\\s*")) {
                emailSvc.sendEmailWithAttachment(
                        to, subj, body, data,
                        rpt.getName()+"."+rpt.getFormat().toLowerCase(),
                        mime
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
