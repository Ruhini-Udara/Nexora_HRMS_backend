package com.hexaco.hrms.service;

public interface NotificationService {
    void sendLeaveStatusUpdate(String recipientName, String email, String phoneNo, String leaveType, String status, String remark);
    void sendTrainingStatusUpdate(String recipientName, String email, String trainingTitle, String status, String remark);
    void sendWelfareStatusUpdate(String recipientName, String email, String welfareType, String status, String remark);
    void sendTrainingFinalizedNotification(String recipientName, String email, String trainingTitle, String startDate, String endDate, String time, String location, String instructor);
    void sendTrainingFinalizedNotification(String recipientName, String email, String trainingTitle, String date, String time, String location, String instructor);
    void sendCompanyEventNotification(String email, String title, String description, String date, String time, String type);
    void sendTransferStatusUpdate(String recipientName, String email, String status, String remark);
    void sendResignationStatusUpdate(String recipientName, String email, String status, String remark);
    void sendResignationStatusUpdate(String recipientName, String email, String status, String remark, Long resignationId, String designation, String branch, String epfNumber, String resignationDate, String lastWorkingDate, String reason, String directorRemark);
    void sendTerminationStatusUpdate(String recipientName, String email, String status, String remark);
    void sendDeathApplicationStatusUpdate(String recipientName, String email, String deceasedEmployeeName, String status, String remark);
}
