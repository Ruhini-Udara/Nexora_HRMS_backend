package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.AttendanceDevicePunch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceDevicePunchRepository extends JpaRepository<AttendanceDevicePunch, Long> {
    boolean existsByAttendanceDevice_IdAndSourceRecordKey(Long attendanceDeviceId, String sourceRecordKey);
    List<AttendanceDevicePunch> findByProcessedFalseOrderByPunchTimeAsc();
}
