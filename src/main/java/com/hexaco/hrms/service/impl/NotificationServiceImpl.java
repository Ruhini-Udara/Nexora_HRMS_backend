package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendLeaveStatusUpdate(Employee employee, String leaveType, String status, String remark) {
        String employeeName = (employee.getFirstName() != null ? employee.getFirstName() : "") + " " + (employee.getLastName() != null ? employee.getLastName() : "");
        String email = employee.getEmail();
        String phoneNo = employee.getPhoneNo();

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
                employeeName, email, status, employee.getFirstName(), leaveType, status, (remark != null ? remark : "N/A"));

        // Simulate SMS Sending
        if (phoneNo != null && !phoneNo.isEmpty()) {
            log.info("\n" +
                    "📱 [SMS SENT to {}]: Hi {}, your {} request is {}. - Nexora HR",
                    phoneNo, employee.getFirstName(), leaveType, status);
        }
    }
}
