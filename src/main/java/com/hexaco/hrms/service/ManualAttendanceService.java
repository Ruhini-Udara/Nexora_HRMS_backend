package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.AttendanceSubmitDto;
import com.hexaco.hrms.dto.ManualAttendanceDto;
import com.hexaco.hrms.models.AttendanceShift;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.ManualAttendance;
import com.hexaco.hrms.models.UserAccount;
import com.hexaco.hrms.repository.AttendanceShiftRepository;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.repository.ManualAttendanceRepository;
import com.hexaco.hrms.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManualAttendanceService {

    private final ManualAttendanceRepository attendanceRepository;
    private final AttendanceShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final UserAccountRepository userRepository;

    // ── Get all available shifts ─────────────────────────────────────────────
    public List<AttendanceShift> getAllShifts() {
        return shiftRepository.findAll();
    }

    // ── Get attendance records for a specific date (returns ALL employees in dept) ──
    public List<ManualAttendanceDto> getAttendanceByDate(LocalDate date, String department) {
        List<Employee> employees;
        if (department != null && !department.isBlank() && !department.equalsIgnoreCase("All Departments")) {
            employees = employeeRepository.findByDepartmentIgnoreCase(department);
        } else {
            employees = employeeRepository.findAll();
        }

        return employees.stream().map(emp -> {
            ManualAttendance attendance = attendanceRepository
                    .findByEmployeeIdAndAttendanceDate(emp.getId(), date)
                    .orElse(null);
            
            if (attendance != null) {
                return mapToDto(attendance);
            } else {
                // Return a "blank" DTO for employees with no record yet
                return ManualAttendanceDto.builder()
                        .employeeId(emp.getId())
                        .employeeCode(emp.getEmployeeCode())
                        .employeeName(emp.getFullName())
                        .designation(emp.getDesignation() != null ? emp.getDesignation().getDesignationName() : "")
                        .department(emp.getDepartment())
                        .attendanceDate(date)
                        .status(null)
                        .build();
            }
        }).collect(Collectors.toList());
    }

    // ── Get attendance history for one employee ───────────────────────────────
    public List<ManualAttendanceDto> getEmployeeAttendance(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ── Batch Submit/Update Attendance (Upsert) ───────────────────────────────
    @Transactional
    public List<ManualAttendanceDto> batchSubmitAttendance(AttendanceSubmitDto submitDto) {
        AttendanceShift shift = shiftRepository.findById(submitDto.getShiftId())
                .orElseThrow(() -> new RuntimeException("Shift not found with ID: " + submitDto.getShiftId()));

        UserAccount submitter = userRepository.findById(submitDto.getSubmittedBy())
                .orElseGet(() -> {
                    List<UserAccount> users = userRepository.findByEmployeeId(submitDto.getSubmittedBy());
                    if (!users.isEmpty()) return users.get(0);
                    throw new RuntimeException("Submitter (User or Employee) not found with ID: " + submitDto.getSubmittedBy());
                });

        return submitDto.getRecords().stream().map(record -> {
            try {
                Employee emp = employeeRepository.findById(record.getEmployeeId())
                        .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + record.getEmployeeId()));

                // Find existing or create new
                ManualAttendance attendance = attendanceRepository
                        .findByEmployeeIdAndAttendanceDate(emp.getId(), submitDto.getAttendanceDate())
                        .orElse(new ManualAttendance());

                attendance.setEmployee(emp);
                attendance.setShift(shift);
                attendance.setAttendanceDate(submitDto.getAttendanceDate());
                attendance.setStatus(record.getStatus());
                attendance.setInTime(record.getInTime());
                attendance.setOutTime(record.getOutTime());
                attendance.setRemarks(record.getRemarks());
                attendance.setSubmittedBy(submitter);
                
                // Workflow flags
                attendance.setIsCustomEntry(record.getInTime() != null || record.getOutTime() != null);
                if (attendance.getApprovalStatus() == null) {
                    attendance.setApprovalStatus("APPROVED");
                }

                // Auto-calculate hours if Present
                if ("PRESENT".equalsIgnoreCase(record.getStatus()) && record.getInTime() != null && record.getOutTime() != null) {
                    calculateHours(attendance, shift);
                } else {
                    attendance.setWorkHours(BigDecimal.ZERO);
                    attendance.setOvertimeHours(BigDecimal.ZERO);
                }

                return mapToDto(attendanceRepository.save(attendance));
            } catch (Exception e) {
                throw new RuntimeException("Attendance submission failed for employee " + record.getEmployeeId() + ": " + e.getMessage());
            }
        }).collect(Collectors.toList());
    }

    // ── Calculate Work Hours & Overtime ───────────────────────────────────────
    private void calculateHours(ManualAttendance attendance, AttendanceShift shift) {
        Duration duration = Duration.between(attendance.getInTime(), attendance.getOutTime());
        if (duration.isNegative()) {
            // Handle night shifts crossing midnight (basic logic)
            duration = duration.plusDays(1);
        }

        BigDecimal hours = new BigDecimal(duration.toMinutes()).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
        attendance.setWorkHours(hours);

        BigDecimal stdHours = BigDecimal.valueOf(shift.getStandardHours());
        if (hours.compareTo(stdHours) > 0) {
            attendance.setOvertimeHours(hours.subtract(stdHours));
        } else {
            attendance.setOvertimeHours(BigDecimal.ZERO);
        }
    }

    // ── Mapper ───────────────────────────────────────────────────────────────
    private ManualAttendanceDto mapToDto(ManualAttendance entity) {
        return ManualAttendanceDto.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployee().getId())
                .employeeCode(entity.getEmployee().getEmployeeCode())
                .employeeName(entity.getEmployee().getFullName())
                .designation(entity.getEmployee().getDesignation() != null ? entity.getEmployee().getDesignation().getDesignationName() : "")
                .department(entity.getEmployee().getDepartment())
                .shiftId(entity.getShift().getId())
                .shiftName(entity.getShift().getShiftName())
                .attendanceDate(entity.getAttendanceDate())
                .status(entity.getStatus())
                .inTime(entity.getInTime())
                .outTime(entity.getOutTime())
                .workHours(entity.getWorkHours())
                .overtimeHours(entity.getOvertimeHours())
                .remarks(entity.getRemarks())
                .isCustomEntry(entity.getIsCustomEntry())
                .approvalStatus(entity.getApprovalStatus())
                .rejectionReason(entity.getRejectionReason())
                .submittedBy(entity.getSubmittedBy() != null ? entity.getSubmittedBy().getUserId() : null)
                .submittedByName(entity.getSubmittedBy() != null ? entity.getSubmittedBy().getUserName() : "System")
                .submittedAt(entity.getSubmittedAt())
                .approvedBy(entity.getApprovedBy() != null ? entity.getApprovedBy().getUserId() : null)
                .approvedAt(entity.getApprovedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
