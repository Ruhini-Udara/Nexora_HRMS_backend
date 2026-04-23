package com.hexaco.hrms.service;

import com.hexaco.hrms.models.Employee;

public interface NotificationService {
    void sendLeaveStatusUpdate(Employee employee, String leaveType, String status, String remark);
}
