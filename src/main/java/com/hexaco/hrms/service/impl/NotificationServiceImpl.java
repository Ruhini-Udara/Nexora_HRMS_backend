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
        log.info("To: {} <{}> Subject: {}", recipientName, email, subject);
    }

    @Override
    public void sendTrainingStatusUpdate(String recipientName, String email, String trainingTitle, String status, String remark) {
        String subject = "Training Application Update: " + status;
        String content = String.format(
            "Dear %s,\n\nYour application for the training \"%s\" has been %s.\nRemark: %s\n\nBest Regards,\nNexora HRMS System",
            recipientName, trainingTitle, status, (remark != null && !remark.isEmpty() ? remark : "N/A")
        );
        log.info("To: {} <{}> Training: {}", recipientName, email, trainingTitle);
    }

    @Override
    public void sendWelfareStatusUpdate(String recipientName, String email, String welfareType, String status, String remark) {
        String subject = "Welfare Request Update: " + status;
        String content = String.format(
            "Dear %s,\n\nYour welfare request for \"%s\" has been %s.\nRemark: %s\n\nBest Regards,\nNexora HRMS System",
            recipientName, welfareType, status, (remark != null && !remark.isEmpty() ? remark : "N/A")
        );
        log.info("To: {} <{}> Welfare: {}", recipientName, email, welfareType);
    }

    @Override
    public void sendTrainingFinalizedNotification(String recipientName, String email, String trainingTitle, String date, String time, String location, String instructor) {
        String subject = "Training Finalized: " + trainingTitle;
        String content = String.format(
            "Dear %s,\n\nThe training session for \"%s\" has been finalized.\n\nDetails:\nDate: %s\nTime: %s\nLocation: %s\nInstructor: %s\n\nPlease mark your calendar. We look forward to your participation.\n\nBest Regards,\nNexora HRMS System",
            recipientName, trainingTitle, date, time, location, (instructor != null ? instructor : "TBD")
        );
        log.info("To: {} <{}> Finalized Training: {}", recipientName, email, trainingTitle);
    }
}