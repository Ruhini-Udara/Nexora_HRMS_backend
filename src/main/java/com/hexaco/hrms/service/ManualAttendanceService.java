package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.AttendanceSubmitDto;
import com.hexaco.hrms.dto.ManualAttendanceDto;
import com.hexaco.hrms.models.Shift;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.ManualAttendance;
import com.hexaco.hrms.models.UserAccount;
import com.hexaco.hrms.repository.ShiftRepository;
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
    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final UserAccountRepository userRepository;

    // ── Get all available shifts ─────────────────────────────────────────────
    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    // ── Get attendance records for a specific date (returns ALL employees in dept) ──
    public List<ManualAttendanceDto> getAttendanceByDate(LocalDate date, String department, Long supervisorId) {
        List<Employee> employees;
        if (supervisorId != null) {
            employees = employeeRepository.findByReportingOfficerId(supervisorId);
            // Fallback for demo: if no subordinates found, fetch all employees
            if (employees.isEmpty()) {
                employees = employeeRepository.findAll();
            }
            if (department != null && !department.isBlank() && !department.equalsIgnoreCase("All Departments")) {
                employees = employees.stream().filter(e -> department.equalsIgnoreCase(e.getDepartment())).collect(Collectors.toList());
            }
        } else if (department != null && !department.isBlank() && !department.equalsIgnoreCase("All Departments")) {
            employees = employeeRepository.findByDepartmentIgnoreCase(department);
        } else {
            employees = employeeRepository.findAll();
        }

        return employees.stream().map(emp -> {
            ManualAttendance attendance = null;
            try {
                attendance = attendanceRepository
                        .findByEmployeeIdAndAttendanceDate(emp.getId(), date)
                        .orElse(null);
            } catch (org.springframework.orm.jpa.JpaObjectRetrievalFailureException | jakarta.persistence.EntityNotFoundException e) {
                // Ignore employees with broken foreign keys (e.g. deleted designations) that cause Hibernate to fail fetching
                System.out.println("Skipping employee " + emp.getId() + " due to data integrity issue: " + e.getMessage());
            }

            if (attendance != null) {
                // Explicitly set the employee in case Hibernate failed to eagerly load it due to corrupted foreign keys
                if (attendance.getEmployee() == null) {
                    attendance.setEmployee(emp);
                }
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
        Shift shift = shiftRepository.findById(submitDto.getShiftId())
                .orElseThrow(() -> new RuntimeException("Shift not found with ID: " + submitDto.getShiftId()));

        UserAccount submitter = userRepository.findById(submitDto.getSubmittedBy())
                .orElseGet(() -> {
                    List<UserAccount> users = userRepository.findByEmployeeId(submitDto.getSubmittedBy());
                    if (!users.isEmpty()) return users.get(0);
                    throw new RuntimeException("Submitter (User or Employee) not found with ID: " + submitDto.getSubmittedBy());
                });

        return submitDto.getRecords().stream().map(record -> {
            try {
                Employee emp = employeeRepository.findAll().stream()
                        .filter(e -> e.getId().equals(record.getEmployeeId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + record.getEmployeeId()));

                // Find existing or create new
                ManualAttendance attendance;
                try {
                    attendance = attendanceRepository
                            .findByEmployeeIdAndAttendanceDate(emp.getId(), submitDto.getAttendanceDate())
                            .orElse(new ManualAttendance());
                } catch (org.springframework.orm.jpa.JpaObjectRetrievalFailureException | jakarta.persistence.EntityNotFoundException e) {
                    attendance = new ManualAttendance();
                }

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
    private void calculateHours(ManualAttendance attendance, Shift shift) {
        Duration duration = Duration.between(attendance.getInTime(), attendance.getOutTime());
        if (duration.isNegative()) {
            // Handle night shifts crossing midnight (basic logic)
            duration = duration.plusDays(1);
        }

        BigDecimal hours = new BigDecimal(duration.toMinutes()).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
        attendance.setWorkHours(hours);

        Duration shiftDuration = Duration.between(shift.getStartTime(), shift.getEndTime());
        if (shiftDuration.isNegative()) {
            shiftDuration = shiftDuration.plusDays(1);
        }
        BigDecimal stdHours = new BigDecimal(shiftDuration.toMinutes()).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
        
        if (hours.compareTo(stdHours) > 0) {
            attendance.setOvertimeHours(hours.subtract(stdHours));
        } else {
            attendance.setOvertimeHours(BigDecimal.ZERO);
        }
    }

    // ── Employee Submit Edit Request ──────────────────────────────────────────
    @Transactional
    public ManualAttendanceDto submitEmployeeRequest(Long employeeId, com.hexaco.hrms.dto.EmployeeAttendanceRequestDto dto) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        ManualAttendance attendance = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(employeeId, dto.getAttendanceDate())
                .orElse(new ManualAttendance());

        if (attendance.getId() == null) {
            attendance.setEmployee(emp);
            attendance.setAttendanceDate(dto.getAttendanceDate());
            List<Shift> shifts = shiftRepository.findAll();
            if (!shifts.isEmpty()) attendance.setShift(shifts.get(0));
        }

        attendance.setInTime(dto.getInTime());
        attendance.setOutTime(dto.getOutTime());
        attendance.setRemarks(dto.getReason());
        attendance.setStatus("PRESENT"); // Default to Present, approvalStatus tracks PENDING state
        attendance.setApprovalStatus("PENDING");
        attendance.setIsCustomEntry(true);
        
        // Let's set submitted by to employee's user account if exists
        List<UserAccount> users = userRepository.findByEmployeeId(employeeId);
        if (!users.isEmpty()) {
            attendance.setSubmittedBy(users.get(0));
        }

        if (dto.getInTime() != null && dto.getOutTime() != null && attendance.getShift() != null) {
            calculateHours(attendance, attendance.getShift());
        }

        return mapToDto(attendanceRepository.save(attendance));
    }

    // ── Employee Cancel Request ───────────────────────────────────────────────
    @Transactional
    public void cancelEmployeeRequest(Long attendanceId) {
        ManualAttendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));
        
        if (!"PENDING".equalsIgnoreCase(attendance.getApprovalStatus())) {
            throw new RuntimeException("Only pending requests can be cancelled");
        }
        
        attendance.setApprovalStatus("CANCELLED");
        attendanceRepository.save(attendance);
    }

    // ── Supervisor Approve Multiple Requests ──────────────────────────────────
    @Transactional
    public void approveMultipleRequests(List<Long> attendanceIds) {
        // Ideally we pass supervisorId, but for now we just approve them.
        List<ManualAttendance> records = attendanceRepository.findAllById(attendanceIds);
        for (ManualAttendance attendance : records) {
            if ("PENDING".equalsIgnoreCase(attendance.getApprovalStatus())) {
                attendance.setApprovalStatus("APPROVED");
                attendance.setStatus("PRESENT"); // Or Working depending on logic, but Present is standard
                // In reality we should fetch current user and set approvedBy, approvedAt
                attendance.setApprovedAt(java.time.LocalDateTime.now());
            }
        }
        attendanceRepository.saveAll(records);
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
                .shiftId(entity.getShift() != null ? entity.getShift().getId() : null)
                .shiftName(entity.getShift() != null ? entity.getShift().getName() : null)
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
