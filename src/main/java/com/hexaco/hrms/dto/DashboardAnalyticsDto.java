package com.hexaco.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAnalyticsDto {
    private int presentToday;
    private int lateToday;
    private long onLeaveToday;
    
    private long pendingOverseas;
    private long pendingMaternity;
    private long delayedApprovals;
    private long totalPendingRequests;
    
    private long totalStaff;
    private long newHiresThisWeek;
    private long activeTrainingPrograms;
    private long trainingsFinishingSoon;
    private String attendancePercentage;
    
    private List<PassportExpiryAlert> passportExpiryAlerts;
    private List<MaternityReturnAlert> upcomingMaternityReturns;
    
    private Map<String, Long> departmentEmployeeCount;
    private Map<String, Long> departmentLeaveImpact;
    private Map<String, Long> designationEmployeeCount;
    private Map<String, Long> employmentStatusCount;
    private Map<String, Long> branchEmployeeCount;
    private Map<String, Long> leaveTypesUsed;
    private Map<String, Long> attendanceStatusToday;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PassportExpiryAlert {
        private String employeeName;
        private String passportNumber;
        private String expiryDate;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MaternityReturnAlert {
        private String employeeName;
        private String expectedReturnDate;
    }
}
