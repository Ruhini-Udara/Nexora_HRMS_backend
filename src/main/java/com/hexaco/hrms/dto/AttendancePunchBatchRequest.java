package com.hexaco.hrms.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AttendancePunchBatchRequest {
    private String deviceCode;
    private List<Punch> punches;

    @Data
    public static class Punch {
        private Long terminalUserId;
        private LocalDateTime punchTime;
        private String sourceRecordKey;
        private String rawPayload;
    }
}
