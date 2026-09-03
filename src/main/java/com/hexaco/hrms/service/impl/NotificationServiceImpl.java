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
    public void sendLeaveStatusUpdate(String recipientName, String email, String phoneNo, String leaveType,
            String status, String remark) {

        String subject = "Leave Application Update: " + status;
        String content = String.format(
                "Dear %s,\n\nYour %s request has been %s.\nRemark: %s\n\nBest Regards,\nHRMATE",
                recipientName, leaveType, status, (remark != null && !remark.isEmpty() ? remark : "N/A"));

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
    public void sendTrainingStatusUpdate(String recipientName, String email, String trainingTitle, String status,
            String remark) {
        String subject = "Training Application Update: " + status;
        String content = String.format(
                "Dear %s,\n\nYour application for the training \"%s\" has been %s.\nRemark: %s\n\nBest Regards,\nHRMATE",
                recipientName, trainingTitle, status, (remark != null && !remark.isEmpty() ? remark : "N/A"));

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

    @Override
    public void sendWelfareStatusUpdate(String recipientName, String email, String welfareType, String status,
            String remark) {
        String subject = "Welfare Request Update: " + status;
        String content = String.format(
                "Dear %s,\n\nYour welfare request for \"%s\" has been %s.\nRemark: %s\n\nBest Regards,\nHRMATE",
                recipientName, welfareType, status, (remark != null && !remark.isEmpty() ? remark : "N/A"));

        log.info("\n" +
                "╔══════════════════════════════════════════════════════════╗\n" +
                "║ 📧 WELFARE NOTIFICATION LOG                                                 ║\n" +
                "╠══════════════════════════════════════════════════════════╣\n" +
                "║ To: {} <{}> \n" +
                "║ Subject: {}\n" +
                "║ Welfare Type: {}\n" +
                "║ Mode: {}\n" +
                "╚══════════════════════════════════════════════════════════╝\n",
                recipientName, email, subject, welfareType, (simulationMode ? "SIMULATION" : "REAL EMAIL"));

        if (!simulationMode) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(email);
                message.setSubject(subject);
                message.setText(content);
                mailSender.send(message);
                log.info("✅ Real Welfare Email successfully sent to {}", email);
            } catch (Exception e) {
                log.error("❌ Failed to send real welfare email to {}: {}", email, e.getMessage());
            }
        } else {
            log.info("ℹ️ [SIMULATION MODE] Welfare Email content: \n{}", content);
        }
    }

    @Override
    public void sendTrainingFinalizedNotification(String recipientName, String email, String trainingTitle, String startDate,
            String endDate, String time, String location, String instructor) {
        String subject = "Training Finalized: " + trainingTitle;

        String dateDetails;
        if (endDate != null && !endDate.trim().isEmpty() && !endDate.equalsIgnoreCase("TBD")) {
            dateDetails = "Start Date: " + (startDate != null ? startDate : "TBD") + "\n" +
                          "End Date: " + endDate;
        } else {
            dateDetails = "Date: " + (startDate != null ? startDate : "TBD");
        }

        String content = String.format(
                "Dear %s,\n\nThe training session for \"%s\" has been finalized.\n\n" +
                        "Details:\n" +
                        "%s\n" +
                        "Time: %s\n" +
                        "Location: %s\n" +
                        "Instructor: %s\n\n" +
                        "Please mark your calendar. We look forward to your participation.\n\n" +
                        "Best Regards,\nHRMATE",
                recipientName, trainingTitle, dateDetails, time, location, (instructor != null ? instructor : "TBD"));

        log.info("\n" +
                "╔══════════════════════════════════════════════════════════╗\n" +
                "║ 📧 FINALIZED TRAINING NOTIFICATION LOG                                      ║\n" +
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
                log.info("✅ Finalized Training Email successfully sent to {}", email);
            } catch (Exception e) {
                log.error("❌ Failed to send finalized training email to {}: {}", email, e.getMessage());
            }
        } else {
            log.info("ℹ️ [SIMULATION MODE] Finalized Training Email content: \n{}", content);
        }
    }

    @Override
    public void sendTrainingFinalizedNotification(String recipientName, String email, String trainingTitle, String date,
            String time, String location, String instructor) {
        sendTrainingFinalizedNotification(recipientName, email, trainingTitle, date, date, time, location, instructor);
    }

    @Override
    public void sendCompanyEventNotification(String email, String title, String description, String date, String time,
            String type) {
        String subject = "New Company Event: " + title;
        String content = String.format(
                "Dear Employee,\n\nA new company event has been scheduled.\n\n" +
                        "Details:\n" +
                        "Title: %s\n" +
                        "Type: %s\n" +
                        "Date: %s\n" +
                        "Time: %s\n" +
                        "Description: %s\n\n" +
                        "Best Regards,\nHRMATE",
                title, type, date, time, (description != null && !description.isEmpty() ? description : "N/A"));

        log.info("\n" +
                "╔══════════════════════════════════════════════════════════╗\n" +
                "║ 📧 COMPANY EVENT NOTIFICATION LOG                                           ║\n" +
                "╠══════════════════════════════════════════════════════════╣\n" +
                "║ To: <{}> \n" +
                "║ Subject: {}\n" +
                "║ Mode: {}\n" +
                "╚══════════════════════════════════════════════════════════╝\n",
                email, subject, (simulationMode ? "SIMULATION" : "REAL EMAIL"));

        if (!simulationMode) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(email);
                message.setSubject(subject);
                message.setText(content);
                mailSender.send(message);
                log.info("✅ Company Event Email successfully sent to {}", email);
            } catch (Exception e) {
                log.error("❌ Failed to send company event email to {}: {}", email, e.getMessage());
            }
        } else {
            log.info("ℹ️ [SIMULATION MODE] Company Event Email content: \n{}", content);
        }
    }

    @Override
    public void sendTransferStatusUpdate(String recipientName, String email, String status, String remark) {
        String subject = "Transfer Request Update: " + status;
        String content = String.format(
                "Dear %s,\n\nYour transfer request has been %s.\nRemark: %s\n\nBest Regards,\nHR Mate",
                recipientName, status, (remark != null && !remark.isEmpty() ? remark : "N/A"));

        log.info("\n" +
                "╔══════════════════════════════════════════════════════════╗\n" +
                "║ 📧 TRANSFER NOTIFICATION LOG                                                ║\n" +
                "╠══════════════════════════════════════════════════════════╣\n" +
                "║ To: {} <{}> \n" +
                "║ Subject: {}\n" +
                "║ Mode: {}\n" +
                "╚══════════════════════════════════════════════════════════╝\n",
                recipientName, email, subject, (simulationMode ? "SIMULATION" : "REAL EMAIL"));

        if (!simulationMode) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(email);
                message.setSubject(subject);
                message.setText(content);
                mailSender.send(message);
                log.info("✅ Real Transfer Email successfully sent to {}", email);
            } catch (Exception e) {
                log.error("❌ Failed to send real transfer email to {}: {}", email, e.getMessage());
            }
        } else {
            log.info("ℹ️ [SIMULATION MODE] Transfer Email content: \n{}", content);
        }
    }

    @Override
    public void sendResignationStatusUpdate(String recipientName, String email, String status, String remark) {
        String subject = "Resignation Request Update: " + status;
        String content = String.format(
                "Dear %s,\n\nYour resignation request has been %s.\nRemark: %s\n\nBest Regards,\nHR Mate",
                recipientName, status, (remark != null && !remark.isEmpty() ? remark : "N/A"));

        log.info("\n" +
                "╔══════════════════════════════════════════════════════════╗\n" +
                "║ 📧 RESIGNATION NOTIFICATION LOG                                             ║\n" +
                "╠══════════════════════════════════════════════════════════╣\n" +
                "║ To: {} <{}> \n" +
                "║ Subject: {}\n" +
                "║ Mode: {}\n" +
                "╚══════════════════════════════════════════════════════════╝\n",
                recipientName, email, subject, (simulationMode ? "SIMULATION" : "REAL EMAIL"));

        if (!simulationMode) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(email);
                message.setSubject(subject);
                message.setText(content);
                mailSender.send(message);
                log.info("✅ Real Resignation Email successfully sent to {}", email);
            } catch (Exception e) {
                log.error("❌ Failed to send real resignation email to {}: {}", email, e.getMessage());
            }
        } else {
            log.info("ℹ️ [SIMULATION MODE] Resignation Email content: \n{}", content);
        }
    }

    @Override
    public void sendTerminationStatusUpdate(String recipientName, String email, String status, String remark) {
        String subject = "Important Notice Regarding Your Employment";
        
        String customMessage = "Your termination status has been updated to: " + status + ".";
        // Apply requested specific message for rejected/terminated states if applicable
        if ("REJECTED".equalsIgnoreCase(status) || "TERMINATED".equalsIgnoreCase(status) || "APPROVED".equalsIgnoreCase(status)) {
             customMessage = "We are sorry to inform you that you have been terminated by board after inquiry.";
        }
        
        String content = String.format(
                "Dear %s,\n\n%s\nRemark: %s\n\nBest Regards,\nHR Mate",
                recipientName, customMessage, (remark != null && !remark.isEmpty() ? remark : "N/A"));

        log.info("\n" +
                "╔══════════════════════════════════════════════════════════╗\n" +
                "║ 📧 TERMINATION NOTIFICATION LOG                                             ║\n" +
                "╠══════════════════════════════════════════════════════════╣\n" +
                "║ To: {} <{}> \n" +
                "║ Subject: {}\n" +
                "║ Mode: {}\n" +
                "╚══════════════════════════════════════════════════════════╝\n",
                recipientName, email, subject, (simulationMode ? "SIMULATION" : "REAL EMAIL"));

        if (!simulationMode) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(email);
                message.setSubject(subject);
                message.setText(content);
                mailSender.send(message);
                log.info("✅ Real Termination Email successfully sent to {}", email);
            } catch (Exception e) {
                log.error("❌ Failed to send real termination email to {}: {}", email, e.getMessage());
            }
        } else {
            log.info("ℹ️ [SIMULATION MODE] Termination Email content: \n{}", content);
        }
    }

    @Override
    public void sendDeathApplicationStatusUpdate(String recipientName, String email, String deceasedEmployeeName, String status, String remark) {
        String subject = "Death Application Update: " + status;
        
        String customMessage = String.format("The death application for %s has been %s.", deceasedEmployeeName, status);
        if ("APPROVED".equalsIgnoreCase(status) || "Board Approved".equalsIgnoreCase(status)) {
            customMessage = String.format("The death application for %s has been successfully processed and approved.", deceasedEmployeeName);
        } else if ("REJECTED".equalsIgnoreCase(status) || "Board Rejected".equalsIgnoreCase(status)) {
            customMessage = String.format("We are sorry to inform you that the death application for %s has been rejected.", deceasedEmployeeName);
        }
        
        String content = String.format(
                "Dear %s,\n\n%s\nRemark: %s\n\nBest Regards,\nHR Mate",
                recipientName, customMessage, (remark != null && !remark.isEmpty() ? remark : "N/A"));

        log.info("\n" +
                "╔══════════════════════════════════════════════════════════╗\n" +
                "║ 📧 DEATH APPLICATION NOTIFICATION LOG                                       ║\n" +
                "╠══════════════════════════════════════════════════════════╣\n" +
                "║ To: {} <{}> \n" +
                "║ Subject: {}\n" +
                "║ Mode: {}\n" +
                "╚══════════════════════════════════════════════════════════╝\n",
                recipientName, email, subject, (simulationMode ? "SIMULATION" : "REAL EMAIL"));

        if (!simulationMode) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(email);
                message.setSubject(subject);
                message.setText(content);
                mailSender.send(message);
                log.info("✅ Real Death Application Email successfully sent to {}", email);
            } catch (Exception e) {
                log.error("❌ Failed to send real death application email to {}: {}", email, e.getMessage());
            }
        } else {
            log.info("ℹ️ [SIMULATION MODE] Death Application Email content: \n{}", content);
        }
    }
}