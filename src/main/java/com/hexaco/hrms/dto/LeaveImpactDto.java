package com.hexaco.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveImpactDto {
    private String employeeName;
    private String department;
    private int leaveDuration;
    private long departmentEmployees;
    private long alreadyOnLeave;
    private long availableAfterApproval;
    private double availabilityPercentage;
    private String riskLevel;
}
