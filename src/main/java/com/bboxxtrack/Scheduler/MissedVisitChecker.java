package com.bboxxtrack.Scheduler;

import com.bboxxtrack.Service.MaintenanceScheduleService;
import com.bboxxtrack.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class MissedVisitChecker {

    @Autowired private MaintenanceScheduleService scheduleService;
    @Autowired private EmailService emailService;

    // runs everyday at 8 PM
    @Scheduled(cron = "0 0 20 * * *", zone="Africa/Kigali")
    public void checkMissedVisits() {
        LocalDate today = LocalDate.now();
        scheduleService.getAllSchedules().stream()
                .filter(s -> today.equals(s.getScheduleDate()))
                .filter(s -> !"Completed".equalsIgnoreCase(s.getStatus()))
                .forEach(s -> {
                    String body = "Missed maintenance visit for customer " + s.getCustomerId() +
                            " originally scheduled on " + today;
                    emailService.sendEmail("hq@bboxxtrack.com",
                            "Missed Maintenance Visit Alert",
                            body);
                });
    }
}
