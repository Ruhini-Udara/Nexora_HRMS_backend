package com.hexaco.hrms.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class PayrollLeaveDto {
    private Long leaveId;
    private Long employeeId;
    private String employeeName;
    private String epfNumber;
    private String leaveType;
    private LocalDate fromDate;
    private LocalDate endDate;
    private int totalDays;
    private String status;
}
