package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.AttendancePunchBatchRequest;
import com.hexaco.hrms.dto.AttendancePunchBatchResponse;
import com.hexaco.hrms.service.AttendancePunchIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance/punches")
@RequiredArgsConstructor
public class AttendancePunchController {

    private final AttendancePunchIngestionService attendancePunchIngestionService;

    @PostMapping("/batch")
    public ResponseEntity<AttendancePunchBatchResponse> ingestBatch(@RequestBody AttendancePunchBatchRequest request) {
        return ResponseEntity.ok(attendancePunchIngestionService.ingestBatch(request));
    }
}
