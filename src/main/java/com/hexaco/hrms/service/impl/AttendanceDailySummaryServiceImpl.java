package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.AttendanceDailySummaryDto;
import com.hexaco.hrms.models.AttendanceDailySummary;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.repository.AttendanceDailySummaryRepository;
import com.hexaco.hrms.service.AttendanceDailySummaryService;
import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceDailySummaryServiceImpl implements AttendanceDailySummaryService {

    private final AttendanceDailySummaryRepository attendanceDailySummaryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDailySummaryDto> getDailySummaries(
            LocalDate date,
            LocalDate startDate,
            LocalDate endDate,
            String employeeCode,
            String department
    ) {
        LocalDate effectiveStartDate = date != null ? date : startDate;
        LocalDate effectiveEndDate = date != null ? date : endDate;
        String effectiveEmployeeCode = trimToNull(employeeCode);
        String effectiveDepartment = trimToNull(department);

        return attendanceDailySummaryRepository.findAll(
                        buildSpecification(
                                effectiveStartDate,
                                effectiveEndDate,
                                effectiveEmployeeCode,
                                effectiveDepartment
                        ),
                        Sort.by(
                                Sort.Order.desc("attendanceDate"),
                                Sort.Order.asc("employee.employeeCode")
                        )
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    private Specification<AttendanceDailySummary> buildSpecification(
            LocalDate startDate,
            LocalDate endDate,
            String employeeCode,
            String department
    ) {
        return (root, query, criteriaBuilder) -> {
            Join<AttendanceDailySummary, Employee> employee = root.join("employee");

            var predicate = criteriaBuilder.conjunction();
            if (startDate != null) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("attendanceDate"), startDate)
                );
            }
            if (endDate != null) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("attendanceDate"), endDate)
                );
            }
            if (employeeCode != null) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(employee.get("employeeCode")),
                                employeeCode.toLowerCase()
                        )
                );
            }
            if (department != null) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(employee.get("department")),
                                department.toLowerCase()
                        )
                );
            }
            return predicate;
        };
    }

    private AttendanceDailySummaryDto toDto(AttendanceDailySummary summary) {
        Employee employee = summary.getEmployee();

        AttendanceDailySummaryDto dto = new AttendanceDailySummaryDto();
        dto.setEmployeeCode(employee.getEmployeeCode());
        dto.setEmployeeName(employee.getFullName());
        dto.setDepartment(employee.getDepartment());
        dto.setAttendanceDate(summary.getAttendanceDate());
        dto.setCheckInTime(summary.getCheckInTime());
        dto.setCheckOutTime(summary.getCheckOutTime());
        dto.setFingerprintUserId(employee.getFingerprintUserId());
        dto.setSource("DEVICE");
        return dto;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
