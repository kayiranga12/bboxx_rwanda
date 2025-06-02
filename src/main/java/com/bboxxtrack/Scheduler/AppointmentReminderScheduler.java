package com.bboxxtrack.Scheduler;

import com.bboxxtrack.Service.MaintenanceScheduleService;
import com.bboxxtrack.Service.EmailService;
import com.bboxxtrack.Service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AppointmentReminderScheduler {

    @Autowired private MaintenanceScheduleService scheduleService;
    @Autowired private EmailService emailService;
    @Autowired private SmsService smsService;

    // Every day at 8:00 AM Kigali time, remind tomorrow’s appointments
    @Scheduled(cron = "0 0 8 * * *", zone = "Africa/Kigali")
    public void remindTomorrow() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        scheduleService.getAllSchedules().stream()
                .filter(s -> tomorrow.equals(s.getScheduleDate()))
                .forEach(s -> {
                    String msg = String.format(
                            "Reminder: You have a maintenance visit scheduled for %s. Reply “C” to confirm.",
                            tomorrow
                    );
                    // static placeholders—lookup real customer contact in a real app
                    emailService.sendEmail("customer@example.com", "Upcoming Maintenance Reminder", msg);
                    smsService.sendSms("+2507XXXXXXXX", msg);
                });
    }
}
