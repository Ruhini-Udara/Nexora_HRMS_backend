package com.hexaco.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectorDashboardDto {
    private long pendingApprovalsCount;
    private long urgentApprovalsCount;
    private String companyAttendancePercentage;
    private long totalEmployeesCount;
}
