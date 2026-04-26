package com.hexaco.hrms.service;

public interface NotificationService {
    void sendLeaveStatusUpdate(String recipientName, String email, String phoneNo, String leaveType, String status, String remark);
    void sendTrainingStatusUpdate(String recipientName, String email, String trainingTitle, String status, String remark);
    void sendTrainingFinalizedNotification(String recipientName, String email, String trainingTitle, String date, String time, String location, String instructor);
}
