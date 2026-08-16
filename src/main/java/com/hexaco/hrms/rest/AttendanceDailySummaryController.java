package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.AttendanceDailySummaryDto;
import com.hexaco.hrms.service.AttendanceDailySummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance/daily")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AttendanceDailySummaryController {

    private final AttendanceDailySummaryService attendanceDailySummaryService;

    @GetMapping
    public ResponseEntity<List<AttendanceDailySummaryDto>> getDailySummaries(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String employeeCode,
            @RequestParam(required = false) String department
    ) {
        return ResponseEntity.ok(attendanceDailySummaryService.getDailySummaries(
                date,
                startDate,
                endDate,
                employeeCode,
                department
        ));
    }
}
