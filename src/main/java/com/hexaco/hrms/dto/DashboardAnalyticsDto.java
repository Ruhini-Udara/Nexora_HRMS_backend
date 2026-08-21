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
    
    private long pendingOverseas;
    private long pendingMaternity;
    private long delayedApprovals;
    
    private List<PassportExpiryAlert> passportExpiryAlerts;
    private List<MaternityReturnAlert> upcomingMaternityReturns;
    
    private Map<String, Long> departmentEmployeeCount;
    private Map<String, Long> departmentLeaveImpact;
    
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
