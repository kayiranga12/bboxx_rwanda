package com.bboxxtrack.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendEmailWithAttachment(
            String to, String subject, String text,
            byte[] attachment, String filename, String contentType
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            helper.addAttachment(filename, new ByteArrayResource(attachment), contentType);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    public void sendCalendarInvite(String toEmail, String subject, String description,
                                   LocalDateTime startTime, LocalDateTime endTime, String eventId) {
        // Placeholder for sending calendar invite
        System.out.println("Sending calendar invite to " + toEmail + " for event: " + subject);
        System.out.println("Description: " + description);
        System.out.println("Start: " + startTime + ", End: " + endTime + ", Event ID: " + eventId);
        // In production, integrate with a mail server (e.g., JavaMailSender) to send .ics files
    }

    public void sendTicketNotification(Object ticket) {
        // Placeholder for ticket notification
        System.out.println("Sending ticket notification for ticket: " + ticket);
    }
}
