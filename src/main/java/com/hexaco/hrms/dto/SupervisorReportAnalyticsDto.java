package com.hexaco.hrms.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupervisorReportAnalyticsDto {

    private int totalTeamMembers;
    private BigDecimal totalOvertimeHours;
    private String trendGrowth; // e.g. "+2.4%"

    private List<AttendanceTrendItemDto> attendanceTrends;
    private LeaveDistributionDto leaveDistribution;
    private List<OvertimeDeptDto> overtimeInsights;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttendanceTrendItemDto {
        private String label;
        private double presentPercentage;
        private int presentCount;
        private int totalCount;
        private int heightPercent;
        private boolean isCurrent;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LeaveDistributionDto {
        private int sick;
        private int annual;
        private int casual;
        private int total;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OvertimeDeptDto {
        private String department;
        private double hours;
        private int percentage;
    }
}
