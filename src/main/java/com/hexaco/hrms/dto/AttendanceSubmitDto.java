package com.hexaco.hrms.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Payload sent by the frontend when the supervisor clicks "Submit Updates".
 * Contains one record per employee on the attendance sheet.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSubmitDto {

    private LocalDate attendanceDate;   // The date for this attendance sheet
    private Long shiftId;               // Which shift is being submitted
    private Long submittedBy;           // Supervisor's employee ID

    private List<AttendanceRecordDto> records;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttendanceRecordDto {
        private Long employeeId;
        private String status;          // PRESENT, ABSENT, LATE, HALF_DAY
        private LocalTime inTime;       // null if Absent
        private LocalTime outTime;      // null if Absent
        private String remarks;
    }
}
