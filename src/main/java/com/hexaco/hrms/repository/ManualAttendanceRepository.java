package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.ManualAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ManualAttendanceRepository extends JpaRepository<ManualAttendance, Long> {

    // All records for a specific date (used by supervisor to load/submit attendance sheet)
    List<ManualAttendance> findByAttendanceDate(LocalDate date);

    // All records for a specific employee (attendance history)
    List<ManualAttendance> findByEmployeeId(Long employeeId);

    // Specific record for one employee on one date (used for upsert logic)
    Optional<ManualAttendance> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);

    // All records for a date AND department
    List<ManualAttendance> findByAttendanceDateAndEmployee_Department(LocalDate date, String department);
}
