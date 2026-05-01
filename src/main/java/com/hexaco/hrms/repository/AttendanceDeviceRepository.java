package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.AttendanceDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceDeviceRepository extends JpaRepository<AttendanceDevice, Long> {
    Optional<AttendanceDevice> findByDeviceCodeIgnoreCase(String deviceCode);
    List<AttendanceDevice> findByActiveTrue();
}
