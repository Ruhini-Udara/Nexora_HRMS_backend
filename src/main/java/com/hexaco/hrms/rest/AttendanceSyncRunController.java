package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.AttendanceSyncRunDto;
import com.hexaco.hrms.service.AttendanceSyncRunService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/attendance/sync-runs")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AttendanceSyncRunController {

    private final AttendanceSyncRunService attendanceSyncRunService;

    @GetMapping
    public ResponseEntity<List<AttendanceSyncRunDto>> getSyncRuns() {
        return ResponseEntity.ok(attendanceSyncRunService.getSyncRuns());
    }
}
