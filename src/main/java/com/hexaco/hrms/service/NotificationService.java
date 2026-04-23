package com.hexaco.hrms.service;

public interface NotificationService {
    void sendLeaveStatusUpdate(String recipientName, String email, String phoneNo, String leaveType, String status, String remark);
}
