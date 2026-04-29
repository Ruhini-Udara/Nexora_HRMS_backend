package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.AttendanceShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttendanceShiftRepository extends JpaRepository<AttendanceShift, Long> {
    Optional<AttendanceShift> findByShiftName(String shiftName);
}
