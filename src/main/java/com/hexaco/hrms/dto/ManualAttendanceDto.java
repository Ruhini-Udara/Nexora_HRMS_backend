package com.hexaco.hrms.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualAttendanceDto {

    private Long id;

    // Employee info (returned in GET responses)
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String designation;
    private String department;

    // Shift info
    private Long shiftId;
    private String shiftName;

    // Core attendance fields
    private LocalDate attendanceDate;
    private String status;          // PRESENT, ABSENT, LATE, HALF_DAY

    private LocalTime inTime;
    private LocalTime outTime;
    private BigDecimal workHours;
    private BigDecimal overtimeHours;
    private String remarks;

    // Workflow
    private Boolean isCustomEntry;
    private String approvalStatus;
    private String rejectionReason;

    // Audit
    private Long submittedBy;
    private String submittedByName;
    private LocalDateTime submittedAt;
    
    private Long approvedBy;
    private LocalDateTime approvedAt;
    
    private LocalDateTime updatedAt;
}
