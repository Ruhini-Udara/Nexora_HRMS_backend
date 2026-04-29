package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.AttendanceSubmitDto;
import com.hexaco.hrms.dto.ManualAttendanceDto;
import com.hexaco.hrms.models.AttendanceShift;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.ManualAttendance;
import com.hexaco.hrms.repository.AttendanceShiftRepository;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.repository.ManualAttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManualAttendanceService {

    private final ManualAttendanceRepository attendanceRepository;
    private final AttendanceShiftRepository  shiftRepository;
    private final EmployeeRepository         employeeRepository;

    // ── Get all shifts (for the dropdown in the UI) ──────────────────────────
    public List<AttendanceShift> getAllShifts() {
        return shiftRepository.findAll();
    }

    // ── Get attendance records for a specific date (optionally filter by dept) ─
    public List<ManualAttendanceDto> getAttendanceByDate(LocalDate date, String department) {
        List<ManualAttendance> records;
        if (department != null && !department.isBlank()) {
            records = attendanceRepository.findByAttendanceDateAndEmployee_Department(date, department);
        } else {
            records = attendanceRepository.findByAttendanceDate(date);
        }
        return records.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // ── Get attendance history for one employee ───────────────────────────────
    public List<ManualAttendanceDto> getAttendanceByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // ── Batch submit attendance (supervisor clicks "Submit Updates") ──────────
    @Transactional
    public List<ManualAttendanceDto> submitAttendance(AttendanceSubmitDto dto) {
        AttendanceShift shift = shiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new RuntimeException("Shift not found with id: " + dto.getShiftId()));

        Employee supervisor = employeeRepository.findById(dto.getSubmittedBy())
                .orElseThrow(() -> new RuntimeException("Supervisor not found with id: " + dto.getSubmittedBy()));

        List<ManualAttendance> saved = dto.getRecords().stream().map(record -> {
            Employee employee = employeeRepository.findById(record.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found: " + record.getEmployeeId()));

            // Upsert: update existing record if one exists for this employee + date
            ManualAttendance attendance = attendanceRepository
                    .findByEmployeeIdAndAttendanceDate(record.getEmployeeId(), dto.getAttendanceDate())
                    .orElse(ManualAttendance.builder()
                            .employee(employee)
                            .attendanceDate(dto.getAttendanceDate())
                            .build());

            attendance.setShift(shift);
            attendance.setStatus(record.getStatus());
            attendance.setInTime(record.getInTime());
            attendance.setOutTime(record.getOutTime());
            attendance.setRemarks(record.getRemarks());
            attendance.setSubmittedBy(supervisor);

            // Calculate work hours automatically
            if (record.getInTime() != null && record.getOutTime() != null) {
                long minutes = java.time.Duration.between(record.getInTime(), record.getOutTime()).toMinutes();
                if (minutes > 0) {
                    BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
                    attendance.setWorkHours(hours);
                    BigDecimal standard = BigDecimal.valueOf(shift.getStandardHours());
                    BigDecimal overtime = hours.subtract(standard);
                    attendance.setOvertimeHours(overtime.compareTo(BigDecimal.ZERO) > 0 ? overtime : BigDecimal.ZERO);
                }
            }

            return attendanceRepository.save(attendance);
        }).collect(Collectors.toList());

        return saved.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // ── Update a single attendance record ─────────────────────────────────────
    @Transactional
    public ManualAttendanceDto updateRecord(Long id, ManualAttendanceDto dto) {
        ManualAttendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance record not found with id: " + id));

        if (dto.getStatus() != null)  attendance.setStatus(dto.getStatus());
        if (dto.getInTime() != null)  attendance.setInTime(dto.getInTime());
        if (dto.getOutTime() != null) attendance.setOutTime(dto.getOutTime());
        if (dto.getRemarks() != null) attendance.setRemarks(dto.getRemarks());

        // Recalculate hours on update
        LocalTime inT  = attendance.getInTime();
        LocalTime outT = attendance.getOutTime();
        if (inT != null && outT != null) {
            long minutes = java.time.Duration.between(inT, outT).toMinutes();
            if (minutes > 0) {
                BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
                attendance.setWorkHours(hours);
                BigDecimal standard = BigDecimal.valueOf(attendance.getShift().getStandardHours());
                BigDecimal overtime = hours.subtract(standard);
                attendance.setOvertimeHours(overtime.compareTo(BigDecimal.ZERO) > 0 ? overtime : BigDecimal.ZERO);
            }
        }

        return mapToDto(attendanceRepository.save(attendance));
    }

    // ── Map entity → DTO ─────────────────────────────────────────────────────
    private ManualAttendanceDto mapToDto(ManualAttendance a) {
        return ManualAttendanceDto.builder()
                .id(a.getId())
                .employeeId(a.getEmployee().getId())
                .employeeCode(a.getEmployee().getEmployeeCode())
                .employeeName(a.getEmployee().getFullName())
                .designation(a.getEmployee().getDesignation() != null
                        ? a.getEmployee().getDesignation().getDesignationName() : "")
                .department(a.getEmployee().getDepartment())
                .shiftId(a.getShift() != null ? a.getShift().getId() : null)
                .shiftName(a.getShift() != null ? a.getShift().getShiftName() : null)
                .attendanceDate(a.getAttendanceDate())
                .status(a.getStatus())
                .inTime(a.getInTime())
                .outTime(a.getOutTime())
                .workHours(a.getWorkHours())
                .overtimeHours(a.getOvertimeHours())
                .remarks(a.getRemarks())
                .submittedBy(a.getSubmittedBy() != null ? a.getSubmittedBy().getId() : null)
                .submittedAt(a.getSubmittedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
