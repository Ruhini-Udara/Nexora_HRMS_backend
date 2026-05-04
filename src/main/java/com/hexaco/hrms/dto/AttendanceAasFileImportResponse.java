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
public class AttendanceAasFileImportResponse {
    private int insertedCount;
    private int duplicateCount;
    private int failedCount;
    private List<String> errors;
}
