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
    // Returns all shift options for the Department/Shift dropdowns in the UI
    @GetMapping("/shifts")
    public ResponseEntity<List<AttendanceShift>> getAllShifts() {
        return ResponseEntity.ok(attendanceService.getAllShifts());
    }

    // ── GET /api/attendance/manual?date=2026-04-29&department=Operations ──────
    // Loads the employee attendance list for a specific date (with optional dept filter)
    @GetMapping("/manual")
    public ResponseEntity<List<ManualAttendanceDto>> getAttendance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String department) {
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(date, department));
    }

    // ── GET /api/attendance/manual/employee/{employeeId} ──────────────────────
    // Attendance history for one employee
    @GetMapping("/manual/employee/{employeeId}")
    public ResponseEntity<List<ManualAttendanceDto>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByEmployee(employeeId));
    }

    // ── POST /api/attendance/manual/submit ────────────────────────────────────
    // Supervisor submits the whole attendance sheet (batch upsert)
    @PostMapping("/manual/submit")
    public ResponseEntity<List<ManualAttendanceDto>> submitAttendance(
            @RequestBody AttendanceSubmitDto dto) {
        return ResponseEntity.ok(attendanceService.submitAttendance(dto));
    }

    // ── PUT /api/attendance/manual/{id} ───────────────────────────────────────
    // Update a single employee's attendance record (Custom Time Entry panel)
    @PutMapping("/manual/{id}")
    public ResponseEntity<ManualAttendanceDto> updateRecord(
            @PathVariable Long id,
            @RequestBody ManualAttendanceDto dto) {
        return ResponseEntity.ok(attendanceService.updateRecord(id, dto));
    }
}
