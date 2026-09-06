package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.service.NotificationService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
    public void sendTrainingFinalizedNotification(String recipientName, String email, String trainingTitle, String date,
            String time, String location, String instructor) {
        String subject = "Training Finalized: " + trainingTitle;
        String content = String.format(
                "Dear %s,\n\nThe training session for \"%s\" has been finalized.\n\n" +
                        "Details:\n" +
                        "Date: %s\n" +
                        "Time: %s\n" +
                        "Location: %s\n" +
                        "Instructor: %s\n\n" +
                        "Please mark your calendar. We look forward to your participation.\n\n" +
                        "Best Regards,\nHRMATE",
                recipientName, trainingTitle, date, time, location, (instructor != null ? instructor : "TBD"));

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
        String subject;
        String content;

        if ("RETURNED".equalsIgnoreCase(status)) {
            subject = "Transfer Request Returned for Amendment";
            content = String.format(
                    "Dear %s,\n\n" +
                    "Your transfer request has been returned by HR for amendments due to the following reason:\n" +
                    "Reason: %s\n\n" +
                    "Please check and amend the required changes in the system, then resubmit your request for approval.\n\n" +
                    "Best Regards,\nHR Mate",
                    recipientName,
                    (remark != null && !remark.isEmpty() ? remark : "N/A"));
        } else if ("REJECTED".equalsIgnoreCase(status) || "Board Rejected".equalsIgnoreCase(status)) {
            subject = "Transfer Request Rejected";
            content = String.format(
                    "Dear %s,\n\n" +
                    "We regret to inform you that your transfer request has been rejected by the Director / Board.\n\n" +
                    "Status: REJECTED\n" +
                    "Reason / Remark: %s\n\n" +
                    "If you have any questions or require further clarification, please contact HR Operations.\n\n" +
                    "Best Regards,\nHR Mate",
                    recipientName,
                    (remark != null && !remark.isEmpty() ? remark : "None specified"));
        } else if ("APPROVED".equalsIgnoreCase(status) || "Board Approved".equalsIgnoreCase(status)) {
            subject = "Transfer Request Approved";
            content = String.format(
                    "Dear %s,\n\n" +
                    "Congratulations! Your transfer request has been approved.\n\n" +
                    "Status: APPROVED\n" +
                    "Remark: %s\n\n" +
                    "Best Regards,\nHR Mate",
                    recipientName,
                    (remark != null && !remark.isEmpty() ? remark : "N/A"));
        } else {
            subject = "Transfer Request Update: " + status;
            content = String.format(
                    "Dear %s,\n\nYour transfer request status has been updated to %s.\nRemark: %s\n\nBest Regards,\nHR Mate",
                    recipientName, status, (remark != null && !remark.isEmpty() ? remark : "N/A"));
        }

        String trimmedEmail = (email != null) ? email.trim() : null;
        if (trimmedEmail == null || trimmedEmail.isEmpty()) {
            log.warn("⚠️ Cannot send transfer status email: Employee email is null or empty for {}", recipientName);
            return;
        }

        String sender = (fromEmail != null && !fromEmail.trim().isEmpty()) ? fromEmail.trim() : "hrmsnexora@gmail.com";

        log.info("\n" +
                "╔══════════════════════════════════════════════════════════╗\n" +
                "║ 📧 TRANSFER NOTIFICATION LOG                             ║\n" +
                "╠══════════════════════════════════════════════════════════╣\n" +
                "║ To: {} <{}> \n" +
                "║ Subject: {}\n" +
                "║ Mode: {}\n" +
                "╚══════════════════════════════════════════════════════════╝\n",
                recipientName, trimmedEmail, subject, (simulationMode ? "SIMULATION" : "REAL EMAIL"));

        if (!simulationMode) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(sender);
                message.setTo(trimmedEmail);
                message.setSubject(subject);
                message.setText(content);
                mailSender.send(message);
                log.info("✅ Real Transfer Email successfully sent to {}", trimmedEmail);
            } catch (Exception e) {
                log.error("❌ Failed to send real transfer email to {}: {}", trimmedEmail, e.getMessage(), e);
            }
        } else {
            log.info("ℹ️ [SIMULATION MODE] Transfer Email content: \n{}", content);
        }
    }

    @Override
    public void sendResignationStatusUpdate(String recipientName, String email, String status, String remark) {
        sendResignationStatusUpdate(recipientName, email, status, remark, null, null, null, null, null, null, null, null);
    }

    @Override
    public void sendResignationStatusUpdate(
            String recipientName,
            String email,
            String status,
            String remark,
            Long resignationId,
            String designation,
            String branch,
            String epfNumber,
            String resignationDate,
            String lastWorkingDate,
            String reason,
            String directorRemark
    ) {
        String trimmedEmail = (email != null) ? email.trim() : null;
        if (trimmedEmail == null || trimmedEmail.isEmpty()) {
            log.warn("⚠️ Cannot send resignation status email: Employee email is null or empty for {}", recipientName);
            return;
        }

        String sender = (fromEmail != null && !fromEmail.trim().isEmpty()) ? fromEmail.trim() : "hrmsnexora@gmail.com";
        boolean isApproved = "Board Approved".equalsIgnoreCase(status) || "APPROVED".equalsIgnoreCase(status);
        String subject;
        String content;

        if (isApproved) {
            subject = "Formal Acceptance of Resignation - HR MATE";
            content = String.format(
                    "Dear %s,\n\n" +
                    "Your resignation request (Ref: RES-%s) has been officially approved by the Board of Directors.\n\n" +
                    "Please find attached your formal Resignation Acceptance Letter.\n" +
                    "Confirmed Last Day of Service: %s\n" +
                    "%s\n\n" +
                    "We sincerely thank you for your valuable service and wish you all the best in your future endeavours.\n\n" +
                    "Best Regards,\n" +
                    "Director — Human Resources\nHR MATE",
                    recipientName,
                    (resignationId != null ? resignationId : ""),
                    (lastWorkingDate != null && !lastWorkingDate.isEmpty() ? lastWorkingDate : (resignationDate != null ? resignationDate : "As Scheduled")),
                    (directorRemark != null && !directorRemark.trim().isEmpty() ? "Director Note: " + directorRemark : "")
            );
        } else if ("RETURNED".equalsIgnoreCase(status)) {
            subject = "Resignation Request Returned for Amendment";
            content = String.format(
                    "Dear %s,\n\n" +
                    "Your resignation request has been returned by HR for amendments due to the following reason:\n" +
                    "Reason: %s\n\n" +
                    "Please check and amend the required changes in the system, then resubmit your request for approval.\n\n" +
                    "Best Regards,\nHR Mate",
                    recipientName,
                    (remark != null && !remark.isEmpty() ? remark : "N/A")
            );
        } else if ("REJECTED".equalsIgnoreCase(status) || "Board Rejected".equalsIgnoreCase(status)) {
            subject = "Resignation Request Rejected";
            String rejectReasonText = (remark != null && !remark.isEmpty()) ? remark : ((directorRemark != null && !directorRemark.isEmpty()) ? directorRemark : "None specified");
            content = String.format(
                    "Dear %s,\n\n" +
                    "We regret to inform you that your resignation request has been rejected by the Director / Board.\n\n" +
                    "Status: REJECTED\n" +
                    "Reason / Remark: %s\n\n" +
                    "If you have any questions or require further clarification, please contact HR Operations.\n\n" +
                    "Best Regards,\nHR Mate",
                    recipientName,
                    rejectReasonText
            );
        } else {
            subject = "Resignation Request Update: " + status;
            content = String.format(
                    "Dear %s,\n\nYour resignation request has been %s.\nRemark: %s\n\nBest Regards,\nHR Mate",
                    recipientName, status, (remark != null && !remark.isEmpty() ? remark : "N/A")
            );
        }

        byte[] pdfBytes = null;
        if (isApproved) {
            pdfBytes = generateResignationAcceptanceLetterPdf(
                    resignationId,
                    recipientName,
                    designation,
                    branch,
                    epfNumber,
                    resignationDate,
                    lastWorkingDate,
                    reason,
                    directorRemark
            );
        }

        log.info("\n" +
                "╔══════════════════════════════════════════════════════════╗\n" +
                "║ 📧 RESIGNATION NOTIFICATION LOG                             ║\n" +
                "╠══════════════════════════════════════════════════════════╣\n" +
                "║ To: {} <{}> \n" +
                "║ Subject: {}\n" +
                "║ Attachment: {}\n" +
                "║ Mode: {}\n" +
                "╚══════════════════════════════════════════════════════════╝\n",
                recipientName, trimmedEmail, subject,
                (pdfBytes != null ? "Resignation_Acceptance_Letter_RES-" + resignationId + ".pdf (" + pdfBytes.length + " bytes)" : "None"),
                (simulationMode ? "SIMULATION" : "REAL EMAIL"));

        if (!simulationMode) {
            try {
                if (pdfBytes != null && pdfBytes.length > 0) {
                    MimeMessage mimeMessage = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                    helper.setFrom(sender);
                    helper.setTo(trimmedEmail);
                    helper.setSubject(subject);
                    helper.setText(content);
                    helper.addAttachment("Resignation_Acceptance_Letter_RES-" + (resignationId != null ? resignationId : "Doc") + ".pdf",
                            new ByteArrayResource(pdfBytes));
                    mailSender.send(mimeMessage);
                    log.info("✅ Real Resignation Email with Acceptance Letter Attachment successfully sent to {}", trimmedEmail);
                } else {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom(sender);
                    message.setTo(trimmedEmail);
                    message.setSubject(subject);
                    message.setText(content);
                    mailSender.send(message);
                    log.info("✅ Real Resignation Email successfully sent to {}", trimmedEmail);
                }
            } catch (Exception e) {
                log.error("❌ Failed to send real resignation email to {}: {}", trimmedEmail, e.getMessage(), e);
            }
        } else {
            log.info("ℹ️ [SIMULATION MODE] Resignation Email content: \n{}", content);
            if (pdfBytes != null) {
                log.info("ℹ️ [SIMULATION MODE] Generated Resignation Acceptance Letter PDF attached ({} bytes).", pdfBytes.length);
            }
        }
    }

    private byte[] generateResignationAcceptanceLetterPdf(
            Long resignationId,
            String employeeName,
            String designation,
            String branch,
            String epfNumber,
            String resignationDate,
            String lastWorkingDate,
            String reason,
            String directorRemark
    ) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 54, 54, 54, 54);
            PdfWriter.getInstance(document, out);
            document.open();

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
            Font italicFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.DARK_GRAY);

            // Header / Letterhead
            Paragraph company = new Paragraph("HR MATE", headerFont);
            company.setAlignment(Element.ALIGN_CENTER);
            document.add(company);

            Paragraph subtitle = new Paragraph("Human Resources Management System\n", subHeaderFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            // Separator line
            Paragraph line = new Paragraph(new Chunk(new LineSeparator(1f, 100f, Color.LIGHT_GRAY, Element.ALIGN_CENTER, -2)));
            document.add(line);
            document.add(new Paragraph(" \n"));

            // Date and Ref
            String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            Paragraph dateRef = new Paragraph();
            dateRef.setAlignment(Element.ALIGN_RIGHT);
            dateRef.add(new Chunk(todayStr + "\n", regularFont));
            dateRef.add(new Chunk("Ref: RES-" + (resignationId != null ? resignationId : "") + "\n\n", boldFont));
            document.add(dateRef);

            // Recipient Address Block
            Paragraph address = new Paragraph();
            address.add(new Chunk((employeeName != null ? employeeName : "") + "\n", boldFont));
            address.add(new Chunk((designation != null && !designation.isEmpty() ? designation : "Employee") + "\n", regularFont));
            if (branch != null && !branch.isEmpty()) {
                address.add(new Chunk(branch + "\n", regularFont));
            }
            if (epfNumber != null && !epfNumber.isEmpty()) {
                address.add(new Chunk("EPF No: " + epfNumber + "\n", regularFont));
            }
            address.add(new Chunk("\n", regularFont));
            document.add(address);

            // Subject line
            Paragraph subject = new Paragraph("Re: Acceptance of Resignation\n\n", titleFont);
            document.add(subject);

            // Salutation
            String firstName = (employeeName != null && !employeeName.isEmpty()) ? employeeName.split(" ")[0] : "Employee";
            Paragraph salutation = new Paragraph("Dear " + firstName + ",\n\n", regularFont);
            document.add(salutation);

            // Body Paragraph 1
            Paragraph p1 = new Paragraph();
            p1.setLeading(18f);
            p1.add(new Chunk("We write with reference to your resignation letter dated ", regularFont));
            p1.add(new Chunk(resignationDate != null ? resignationDate : "N/A", boldFont));
            p1.add(new Chunk(". After due consideration by the Board of Directors, we hereby formally accept your resignation from the position of ", regularFont));
            p1.add(new Chunk(designation != null && !designation.isEmpty() ? designation : "Employee", boldFont));
            if (branch != null && !branch.isEmpty()) {
                p1.add(new Chunk(", " + branch, regularFont));
            }
            p1.add(new Chunk(".\n\n", regularFont));
            document.add(p1);

            // Body Paragraph 2
            Paragraph p2 = new Paragraph();
            p2.setLeading(18f);
            p2.add(new Chunk("Your reason for resignation has been noted as: ", regularFont));
            p2.add(new Chunk((reason != null && !reason.isEmpty() ? reason : "Personal Reasons") + ".\n\n", boldFont));
            document.add(p2);

            // Body Paragraph 3
            Paragraph p3 = new Paragraph();
            p3.setLeading(18f);
            p3.add(new Chunk("Your last day of service is confirmed as ", regularFont));
            p3.add(new Chunk(lastWorkingDate != null ? lastWorkingDate : (resignationDate != null ? resignationDate : "N/A"), boldFont));
            p3.add(new Chunk(". We kindly request that you ensure a proper handover of all responsibilities, assets, and documentation before your departure.\n\n", regularFont));
            document.add(p3);

            // Body Paragraph 4
            Paragraph p4 = new Paragraph(
                    "Please be advised that your payroll will be finalised and closed as of the above effective date. Your employee account and system access privileges will accordingly be deactivated on the same date.\n\n",
                    regularFont
            );
            p4.setLeading(18f);
            document.add(p4);

            // Director remark if present
            if (directorRemark != null && !directorRemark.trim().isEmpty()) {
                Paragraph pRemark = new Paragraph();
                pRemark.setLeading(18f);
                pRemark.add(new Chunk("Note from the Director: ", boldFont));
                pRemark.add(new Chunk(directorRemark + "\n\n", italicFont));
                document.add(pRemark);
            }

            // Thank you
            Paragraph pThanks = new Paragraph(
                    "We take this opportunity to sincerely thank you for your valuable contributions to the organisation during your tenure. We extend our best wishes to you for your future endeavours.\n\n",
                    regularFont
            );
            pThanks.setLeading(18f);
            document.add(pThanks);

            // Closing & Signature
            Paragraph pClose = new Paragraph("Yours faithfully,\n\n\n\n", regularFont);
            document.add(pClose);

            Paragraph signLine = new Paragraph("_________________________\n", regularFont);
            Paragraph signTitle = new Paragraph("Director — Human Resources", boldFont);
            document.add(signLine);
            document.add(signTitle);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("❌ Error generating resignation PDF: {}", e.getMessage(), e);
            return null;
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