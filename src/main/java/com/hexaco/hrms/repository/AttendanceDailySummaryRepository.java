package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.AttendanceDailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AttendanceDailySummaryRepository extends JpaRepository<AttendanceDailySummary, Long>, JpaSpecificationExecutor<AttendanceDailySummary> {
    Optional<AttendanceDailySummary> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate attendanceDate);
}
