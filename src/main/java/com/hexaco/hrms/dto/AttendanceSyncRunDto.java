package com.hexaco.hrms.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttendanceSyncRunDto {
    private Long id;
    private String deviceCode;
    private String deviceName;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer receivedCount;
    private Integer insertedCount;
    private Integer duplicateCount;
    private Integer failedCount;
    private String status;
    private String message;
}
