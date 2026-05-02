package com.hexaco.hrms.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AttendanceDailySummaryDto {
    private String employeeCode;
    private String employeeName;
    private String department;
    private LocalDate attendanceDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Long fingerprintUserId;
    private String source;
}
