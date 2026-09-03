package com.hexaco.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLeaveOverviewDto {

    private LeaveDetailsMapDto leaveDetails;
    private NextPlannedVacationDto nextPlannedVacation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaveDetailsMapDto {
        private LeaveTypeBalanceDto annual;
        private LeaveTypeBalanceDto medical;
        private LeaveTypeBalanceDto casual;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaveTypeBalanceDto {
        private Integer entitled;
        private Integer used;
        private Integer remaining;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextPlannedVacationDto {
        private String leaveType;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer leaveDays;
        private Long daysUntil;
        private String status;
    }
}
