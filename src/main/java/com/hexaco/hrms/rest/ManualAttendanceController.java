package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.AttendanceSubmitDto;
import com.hexaco.hrms.dto.ManualAttendanceDto;
import com.hexaco.hrms.models.Shift;
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
    public ResponseEntity<List<Shift>> getAllShifts() {
        return ResponseEntity.ok(attendanceService.getAllShifts());
    }

    // ── GET /api/attendance/manual?date=2026-04-29&department=Operations ──────
    @GetMapping("/manual")
    public ResponseEntity<List<ManualAttendanceDto>> getAttendance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Long supervisorId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(date, department, supervisorId));
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

    // ── POST /api/attendance/manual/employee/{employeeId}/request ─────────────
    @PostMapping("/manual/employee/{employeeId}/request")
    public ResponseEntity<?> submitEmployeeRequest(
            @PathVariable Long employeeId,
            @RequestBody com.hexaco.hrms.dto.EmployeeAttendanceRequestDto dto) {
        try {
            return ResponseEntity.ok(attendanceService.submitEmployeeRequest(employeeId, dto));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error submitting request: " + e.getMessage());
        }
    }

    // ── POST /api/attendance/manual/employee/cancel/{id} ──────────────────────
    @PostMapping("/manual/employee/cancel/{id}")
    public ResponseEntity<?> cancelEmployeeRequest(@PathVariable Long id) {
        try {
            attendanceService.cancelEmployeeRequest(id);
            return ResponseEntity.ok("Request cancelled successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error cancelling request: " + e.getMessage());
        }
    }

    // ── POST /api/attendance/manual/supervisor/approve-multiple ───────────────
    @PostMapping("/manual/supervisor/approve-multiple")
    public ResponseEntity<?> approveMultipleRequests(@RequestBody List<Long> attendanceIds) {
        try {
            attendanceService.approveMultipleRequests(attendanceIds);
            return ResponseEntity.ok("Requests approved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error approving requests: " + e.getMessage());
        }
    }
}

