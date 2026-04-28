package com.hexaco.hrms.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverseasLeaveDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String epfNumber;
    private Long leaveTypeId;
    private String leaveTypeName;
    private LocalDate fromDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String reason;
    private String status;
    private String passportNumber;
    private LocalDate passportExpDate;
    private String branch;
    private String contactNumber;
    private String email;
    private String specialRemark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
