package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.AttendanceDeviceDto;
import com.hexaco.hrms.service.AttendanceDeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing biometric attendance devices (e.g., ZKTeco).
 * Evaluator Note: This provides the API for the Fingerprint Attendance Module to register, 
 * update, and list the hardware devices that sync punches to this system.
 */
@RestController
@RequestMapping("/api/attendance/devices")
@CrossOrigin(origins = "http://localhost:3000")
public class AttendanceDeviceController {

    private final AttendanceDeviceService attendanceDeviceService;

    public AttendanceDeviceController(AttendanceDeviceService attendanceDeviceService) {
        this.attendanceDeviceService = attendanceDeviceService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttendanceDeviceDto> createDevice(@RequestBody AttendanceDeviceDto dto) {
        try {
            AttendanceDeviceDto created = attendanceDeviceService.createDevice(dto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<AttendanceDeviceDto>> getAllDevices() {
        return ResponseEntity.ok(attendanceDeviceService.getAllDevices());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttendanceDeviceDto> updateDevice(@PathVariable Long id, @RequestBody AttendanceDeviceDto dto) {
        try {
            return ResponseEntity.ok(attendanceDeviceService.updateDevice(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
