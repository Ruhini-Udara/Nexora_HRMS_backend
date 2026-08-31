package com.hexaco.hrms.rest;

import lombok.Data;

@Data
public class LeaveImportRequest {
    private String employeeCode;
    private Integer year;
    private Integer annualLeaveQuota;
    private Integer casualLeaveQuota;
    private Integer medicalLeaveQuota;
    private Integer annualLeaveUsed;
    private Integer casualLeaveUsed;
    private Integer medicalLeaveUsed;
}
