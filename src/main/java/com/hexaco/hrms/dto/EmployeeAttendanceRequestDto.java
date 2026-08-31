package com.hexaco.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAttendanceRequestDto {
    private LocalDate attendanceDate;
    private LocalTime inTime;
    private LocalTime outTime;
    private String reason;
}
