package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.AttendanceSubmitDto;
import com.hexaco.hrms.dto.ManualAttendanceDto;
import com.hexaco.hrms.models.AttendanceShift;
import com.hexaco.hrms.service.ManualAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class ManualAttendanceController {

    private final ManualAttendanceService attendanceService;

    // ── GET /api/attendance/shifts ────────────────────────────────────────────
    @GetMapping("/shifts")
    public ResponseEntity<List<AttendanceShift>> getAllShifts() {
        return ResponseEntity.ok(attendanceService.getAllShifts());
    }

    // ── GET /api/attendance/manual?date=2026-04-29&department=Operations ──────
    @GetMapping("/manual")
    public ResponseEntity<List<ManualAttendanceDto>> getAttendance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String department) {
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(date, department));
    }

    // ── GET /api/attendance/manual/employee/{employeeId} ──────────────────────
    @GetMapping("/manual/employee/{employeeId}")
    public ResponseEntity<List<ManualAttendanceDto>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getEmployeeAttendance(employeeId));
    }

    // ── POST /api/attendance/manual/submit ────────────────────────────────────
    @PostMapping("/manual/submit")
    public ResponseEntity<?> submitAttendance(
            @RequestBody AttendanceSubmitDto dto) {
        try {
            return ResponseEntity.ok(attendanceService.batchSubmitAttendance(dto));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error saving attendance: " + e.getMessage());
        }
    }
}
