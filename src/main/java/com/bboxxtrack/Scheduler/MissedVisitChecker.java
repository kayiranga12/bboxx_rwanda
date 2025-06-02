// src/main/java/com/bboxxtrack/Scheduler/MissedVisitChecker.java
package com.bboxxtrack.Scheduler;

import com.bboxxtrack.Model.MaintenanceSchedule;
import com.bboxxtrack.Service.MaintenanceScheduleService;
import com.bboxxtrack.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class MissedVisitChecker {

    @Autowired
    private MaintenanceScheduleService scheduleService;

    @Autowired
    private EmailService emailService;

    // runs every day at 8 PM Kigali time
    @Scheduled(cron = "0 0 20 * * *", zone = "Africa/Kigali")
    public void checkMissedVisits() {
        LocalDate today = LocalDate.now();
        scheduleService.getAllSchedules().stream()
                // only today’s visits
                .filter(s -> today.equals(s.getScheduleDate()))
                // not yet completed
                .filter(s -> !"Completed".equalsIgnoreCase(s.getStatus()))
                .forEach(this::alertMissedVisit);
    }

    private void alertMissedVisit(MaintenanceSchedule s) {
        // get the linked customer (make sure MaintenanceSchedule has a `private Customer customer;` mapping)
        String custInfo = s.getCustomer() != null
                ? String.valueOf(s.getCustomer().getId())
                : "unknown";
        String body = "Missed maintenance visit for customer "
                + custInfo
                + " originally scheduled on "
                + s.getScheduleDate();
        emailService.sendEmail(
                "hq@bboxxtrack.com",
                "Missed Maintenance Visit Alert",
                body
        );
    }
}
