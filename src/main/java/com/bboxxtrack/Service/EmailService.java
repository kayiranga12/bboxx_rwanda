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
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public String generateWelcomeEmailTemplate(String username, String email, String password) {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Welcome to BBOXXTrack</title>
</head>
<body style="margin: 0; padding: 0; font-family: 'Arial', sans-serif; background-color: #f8f9fa;">
    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff;">
        
        <!-- Header -->
        <div style="background: linear-gradient(135deg, #1B365D 0%, #0F1F35 100%); padding: 30px 20px; text-align: center;">
            <h1 style="color: #ffffff; font-size: 28px; margin: 0; font-weight: 900;">
                BBOXX<span style="color: #FF6B35; font-weight: 400;">Track</span>
            </h1>
            <p style="color: rgba(255, 255, 255, 0.8); margin: 10px 0 0 0; font-size: 14px;">
                Solar Energy Management Platform
            </p>
        </div>
        
        <!-- Main Content -->
        <div style="padding: 40px 30px;">
            <div style="text-align: center; margin-bottom: 30px;">
                <div style="width: 60px; height: 60px; background: rgba(255, 107, 53, 0.1); border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; margin-bottom: 20px;">
                    <span style="font-size: 24px;">🎉</span>
                </div>
                <h2 style="color: #1B365D; font-size: 24px; margin: 0 0 10px 0; font-weight: 600;">
                    Welcome to BBOXXTrack!
                </h2>
                <p style="color: #666; font-size: 16px; margin: 0; line-height: 1.5;">
                    Your account has been successfully created
                </p>
            </div>
            
            <div style="background: #f8f9fa; border-radius: 12px; padding: 25px; margin-bottom: 30px;">
                <h3 style="color: #1B365D; font-size: 18px; margin: 0 0 20px 0; font-weight: 600;">
                    Your Login Credentials
                </h3>
                
                <div style="margin-bottom: 15px;">
                    <label style="display: block; color: #666; font-size: 12px; font-weight: 600; margin-bottom: 5px; text-transform: uppercase; letter-spacing: 0.5px;">
                        Username
                    </label>
                    <div style="background: #ffffff; border: 2px solid #e1e5e9; border-radius: 8px; padding: 12px; font-family: 'Courier New', monospace; font-size: 14px; color: #1B365D; font-weight: 600;">
                        """ + username + """
                    </div>
                </div>
                
                <div style="margin-bottom: 15px;">
                    <label style="display: block; color: #666; font-size: 12px; font-weight: 600; margin-bottom: 5px; text-transform: uppercase; letter-spacing: 0.5px;">
                        Login Email
                    </label>
                    <div style="background: #ffffff; border: 2px solid #4CAF50; border-radius: 8px; padding: 12px; font-family: 'Courier New', monospace; font-size: 14px; color: #4CAF50; font-weight: 600;">
                        """ + email + """
                    </div>
                </div>
                
                <div style="margin-bottom: 20px;">
                    <label style="display: block; color: #666; font-size: 12px; font-weight: 600; margin-bottom: 5px; text-transform: uppercase; letter-spacing: 0.5px;">
                        Generated Password
                    </label>
                    <div style="background: #ffffff; border: 2px solid #FF6B35; border-radius: 8px; padding: 12px; font-family: 'Courier New', monospace; font-size: 14px; color: #FF6B35; font-weight: 600;">
                        """ + password + """
                    </div>
                </div>
                
                <div style="background: rgba(76, 175, 80, 0.1); border-left: 4px solid #4CAF50; padding: 15px; border-radius: 0 8px 8px 0; margin-bottom: 15px;">
                    <p style="margin: 0; font-size: 13px; color: #666; line-height: 1.4;">
                        <strong style="color: #4CAF50;">Login Instructions:</strong> Use your <strong>email address</strong> as your login username, not the username shown above.
                    </p>
                </div>
                
                <div style="background: rgba(255, 107, 53, 0.1); border-left: 4px solid #FF6B35; padding: 15px; border-radius: 0 8px 8px 0;">
                    <p style="margin: 0; font-size: 13px; color: #666; line-height: 1.4;">
                        <strong style="color: #FF6B35;">Security Note:</strong> This password has been automatically generated for your security.
                    </p>
                </div>
            </div>
            
            <!-- Login Button -->
            <div style="text-align: center; margin-bottom: 30px;">
                <a href="http://localhost:8081/login" style="display: inline-block; background: linear-gradient(135deg, #FF6B35, #E55A2E); color: #ffffff; text-decoration: none; padding: 15px 30px; border-radius: 8px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 15px rgba(255, 107, 53, 0.3);">
                    Access Your Dashboard
                </a>
            </div>
            
            <!-- Quick Login Guide -->
            <div style="background: rgba(27, 54, 93, 0.05); border-radius: 12px; padding: 20px; margin-bottom: 30px;">
                <h4 style="color: #1B365D; font-size: 16px; margin: 0 0 15px 0; font-weight: 600; text-align: center;">
                    📋 Quick Login Guide
                </h4>
                <div style="text-align: left;">
                    <p style="margin: 0 0 8px 0; font-size: 14px; color: #666;">
                        <strong>1.</strong> Go to: <span style="color: #FF6B35; font-weight: 600;">http://localhost:8081/login</span>
                    </p>
                    <p style="margin: 0 0 8px 0; font-size: 14px; color: #666;">
                        <strong>2.</strong> Enter your <strong>email address</strong> in the username field
                    </p>
                    <p style="margin: 0; font-size: 14px; color: #666;">
                        <strong>3.</strong> Enter the generated password above
                    </p>
                </div>
            </div>
            
            <!-- Features Section -->
            <div style="border-top: 1px solid #e1e5e9; padding-top: 30px;">
                <h3 style="color: #1B365D; font-size: 18px; margin: 0 0 20px 0; font-weight: 600; text-align: center;">
                    What you can do with BBOXXTrack
                </h3>
                
                <div style="display: table; width: 100%;">
                    <div style="display: table-row;">
                        <div style="display: table-cell; vertical-align: top; padding-right: 15px; width: 33.33%;">
                            <div style="text-align: center; padding: 20px 10px;">
                                <div style="width: 40px; height: 40px; background: rgba(76, 175, 80, 0.1); border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; margin-bottom: 10px;">
                                    <span style="font-size: 18px;">⚡</span>
                                </div>
                                <h4 style="color: #1B365D; font-size: 14px; margin: 0 0 5px 0; font-weight: 600;">
                                    Solar Projects
                                </h4>
                                <p style="color: #666; font-size: 12px; margin: 0; line-height: 1.4;">
                                    Monitor installations
                                </p>
                            </div>
                        </div>
                        <div style="display: table-cell; vertical-align: top; padding-right: 15px; width: 33.33%;">
                            <div style="text-align: center; padding: 20px 10px;">
                                <div style="width: 40px; height: 40px; background: rgba(255, 107, 53, 0.1); border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; margin-bottom: 10px;">
                                    <span style="font-size: 18px;">📋</span>
                                </div>
                                <h4 style="color: #1B365D; font-size: 14px; margin: 0 0 5px 0; font-weight: 600;">
                                    Task Management
                                </h4>
                                <p style="color: #666; font-size: 12px; margin: 0; line-height: 1.4;">
                                    Organize field work
                                </p>
                            </div>
                        </div>
                        <div style="display: table-cell; vertical-align: top; width: 33.33%;">
                            <div style="text-align: center; padding: 20px 10px;">
                                <div style="width: 40px; height: 40px; background: rgba(27, 54, 93, 0.1); border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; margin-bottom: 10px;">
                                    <span style="font-size: 18px;">📊</span>
                                </div>
                                <h4 style="color: #1B365D; font-size: 14px; margin: 0 0 5px 0; font-weight: 600;">
                                    Analytics
                                </h4>
                                <p style="color: #666; font-size: 12px; margin: 0; line-height: 1.4;">
                                    Track performance
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        
        <!-- Footer -->
        <div style="background: #f8f9fa; padding: 25px 30px; text-align: center; border-top: 1px solid #e1e5e9;">
            <p style="margin: 0 0 10px 0; font-size: 13px; color: #666;">
                Need help? Contact our support team
            </p>
            <p style="margin: 0; font-size: 12px; color: #999;">
                © 2025 BBOXXTrack. Solar Energy Management Platform.
            </p>
        </div>
    </div>
</body>
</html>
""";
    }

}
