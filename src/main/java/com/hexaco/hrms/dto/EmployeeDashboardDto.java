package com.hexaco.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDashboardDto {
    private String attendanceStatus; // "Checked In", "Not Checked In"
    private String attendanceTime; // "at 9:00 AM" or null
    private int leaveBalance; // Total available days
    private int activeTrainingPrograms; // Count of active training programs
    private int pendingRequestsCount; // Count of active items
    private List<RecentRequestItemDto> recentRequests;
    private String shiftName;
    private String shiftStartTime;
    private String shiftEndTime;
}

