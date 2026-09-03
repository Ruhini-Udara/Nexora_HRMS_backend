package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.SupervisorReportAnalyticsDto;

public interface SupervisorReportService {
    SupervisorReportAnalyticsDto getSupervisorReportAnalytics(
            Long supervisorId,
            String period,
            String department,
            String branch
    );
}
