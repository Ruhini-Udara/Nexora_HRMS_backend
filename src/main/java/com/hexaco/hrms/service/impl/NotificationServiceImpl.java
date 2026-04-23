package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendLeaveStatusUpdate(String recipientName, String email, String phoneNo, String leaveType, String status, String remark) {
        
        // Simulate Email Sending
        log.info("\n" +
                "╔══════════════════════════════════════════════════════════╗\n" +
                "║ 📧 AUTOMATED EMAIL NOTIFICATION                                             ║\n" +
                "╠══════════════════════════════════════════════════════════╣\n" +
                "║ To: {} <{}> \n" +
                "║ Subject: Leave Application Update: {}\n" +
                "║ \n" +
                "║ Dear {},\n" +
                "║ \n" +
                "║ Your {} request has been {}.\n" +
                "║ Remark: {}\n" +
                "║ \n" +
                "║ Best Regards,\n" +
                "║ Nexora HRMS System\n" +
                "╚══════════════════════════════════════════════════════════╝\n",
                recipientName, email, status, recipientName, leaveType, status, (remark != null ? remark : "N/A"));

        // Simulate SMS Sending
        if (phoneNo != null && !phoneNo.isEmpty()) {
            log.info("\n" +
                    "📱 [SMS SENT to {}]: Hi {}, your {} request is {}. - Nexora HR",
                    phoneNo, recipientName, leaveType, status);
        }
    }
}