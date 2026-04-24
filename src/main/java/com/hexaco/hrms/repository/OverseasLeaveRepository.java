package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.OverseasLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OverseasLeaveRepository extends JpaRepository<OverseasLeave, Long> {
    List<OverseasLeave> findByStatus(String status);
    List<OverseasLeave> findByEmployeeId(Long employeeId);
}
