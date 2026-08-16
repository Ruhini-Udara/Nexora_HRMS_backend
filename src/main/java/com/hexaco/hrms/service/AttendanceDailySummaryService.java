package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.AttendanceDailySummaryDto;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceDailySummaryService {
    List<AttendanceDailySummaryDto> getDailySummaries(
            LocalDate date,
            LocalDate startDate,
            LocalDate endDate,
            String employeeCode,
            String department
    );
}
