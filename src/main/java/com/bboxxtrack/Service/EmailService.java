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

    public void sendCalendarInvite(
            String toEmail, String subject,
            String description,
            LocalDateTime start, LocalDateTime end,
            String uid
    ) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        StringBuilder ics = new StringBuilder()
                .append("BEGIN:VCALENDAR\r\n")
                .append("VERSION:2.0\r\n")
                .append("PRODID:-//BBOXXTrack//EN\r\n")
                .append("BEGIN:VEVENT\r\n")
                .append("UID:").append(uid).append("\r\n")
                .append("DTSTAMP:").append(dtf.format(LocalDateTime.now())).append("\r\n")
                .append("DTSTART:").append(dtf.format(start)).append("\r\n")
                .append("DTEND:").append(dtf.format(end)).append("\r\n")
                .append("SUMMARY:").append(subject).append("\r\n")
                .append("DESCRIPTION:").append(description).append("\r\n")
                .append("END:VEVENT\r\n")
                .append("END:VCALENDAR");

        sendEmailWithAttachment(
                toEmail,
                subject,
                "Please find your calendar invite attached.",
                ics.toString().getBytes(StandardCharsets.UTF_8),
                "invite.ics",
                "text/calendar; charset=UTF-8; method=REQUEST"
        );
    }


}
