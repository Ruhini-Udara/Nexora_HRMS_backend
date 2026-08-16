package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.AttendanceSyncRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceSyncRunRepository extends JpaRepository<AttendanceSyncRun, Long> {
    List<AttendanceSyncRun> findAllByOrderByStartedAtDesc();
}
