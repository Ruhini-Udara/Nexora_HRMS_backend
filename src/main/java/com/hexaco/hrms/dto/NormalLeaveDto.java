package com.hexaco.hrms.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormalLeaveDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String epfNumber;
    private String department;
    private Long leaveTypeId;
    private String leaveTypeName;
    private LocalDate fromDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String reason;
    private String status;
    private String branch;
    private String contactNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Leave balance remaining (quota - used) for the current year
    private Integer annualLeaveRemaining;
    private Integer sickLeaveRemaining;   // mapped from medicalLeaveQuota - medicalLeaveUsed
    private Integer casualLeaveRemaining;
}
