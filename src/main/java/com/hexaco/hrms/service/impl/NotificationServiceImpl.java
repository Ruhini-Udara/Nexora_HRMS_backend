package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@nexora.com}")
    private String fromEmail;

    @Value("${app.notification.simulation-mode:true}")
    private boolean simulationMode;

    @Override
    public void sendLeaveStatusUpdate(String recipientName, String email, String phoneNo, String leaveType, String status, String remark) {
        
        String subject = "Leave Application Update: " + status;
        String content = String.format(
            "Dear %s,\n\nYour %s request has been %s.\nRemark: %s\n\nBest Regards,\nNexora HRMS System",
            recipientName, leaveType, status, (remark != null && !remark.isEmpty() ? remark : "N/A")
        );

        // 1. ALWAYS Log to console for debugging
        log.info("\n" +
                "╔══════════════════════════════════════════════════════════╗\n" +
                "║ 📧 NOTIFICATION LOG                                                         ║\n" +
                "╠══════════════════════════════════════════════════════════╣\n" +
                "║ To: {} <{}> \n" +
                "║ Subject: {}\n" +
                "║ Mode: {}\n" +
                "╚══════════════════════════════════════════════════════════╝\n",
                recipientName, email, subject, (simulationMode ? "SIMULATION" : "REAL EMAIL"));

        // 2. Real Email Sending
        if (!simulationMode) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(email);
                message.setSubject(subject);
                message.setText(content);
                mailSender.send(message);
                log.info("✅ Real Email successfully sent to {}", email);
            } catch (Exception e) {
                log.error("❌ Failed to send real email to {}: {}", email, e.getMessage());
            }
        } else {
            log.info("ℹ️ [SIMULATION MODE] Email content: \n{}", content);
        }

        // 3. Simulate SMS Sending (keeping it as simulation since SMS APIs are paid)
        if (phoneNo != null && !phoneNo.isEmpty()) {
            log.info("📱 [SMS SIMULATION SENT to {}]: Hi {}, your {} request is {}. - Nexora HR",
                    phoneNo, recipientName, leaveType, status);
        }
    }

    @Override
    public void sendTrainingStatusUpdate(String recipientName, String email, String trainingTitle, String status, String remark) {
        String subject = "Training Application Update: " + status;
        String content = String.format(
            "Dear %s,\n\nYour application for the training \"%s\" has been %s.\nRemark: %s\n\nBest Regards,\nNexora HRMS System",
            recipientName, trainingTitle, status, (remark != null && !remark.isEmpty() ? remark : "N/A")
        );

        log.info("\n" +
                "╔══════════════════════════════════════════════════════════╗\n" +
                "║ 📧 TRAINING NOTIFICATION LOG                                                ║\n" +
                "╠══════════════════════════════════════════════════════════╣\n" +
                "║ To: {} <{}> \n" +
                "║ Subject: {}\n" +
                "║ Training: {}\n" +
                "║ Mode: {}\n" +
                "╚══════════════════════════════════════════════════════════╝\n",
                recipientName, email, subject, trainingTitle, (simulationMode ? "SIMULATION" : "REAL EMAIL"));

        if (!simulationMode) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(email);
                message.setSubject(subject);
                message.setText(content);
                mailSender.send(message);
                log.info("✅ Real Training Email successfully sent to {}", email);
            } catch (Exception e) {
                log.error("❌ Failed to send real training email to {}: {}", email, e.getMessage());
            }
        } else {
            log.info("ℹ️ [SIMULATION MODE] Training Email content: \n{}", content);
        }
    }
}