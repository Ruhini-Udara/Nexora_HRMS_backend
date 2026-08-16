package com.hexaco.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendancePunchProcessResponse {
    private int processedPunchCount;
    private int summaryCreatedCount;
    private int summaryUpdatedCount;
    private int unknownUserCount;
    private List<String> errors;
}
