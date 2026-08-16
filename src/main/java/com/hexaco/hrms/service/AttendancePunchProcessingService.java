package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.AttendancePunchProcessResponse;

public interface AttendancePunchProcessingService {
    AttendancePunchProcessResponse processUnprocessedPunches();
}
