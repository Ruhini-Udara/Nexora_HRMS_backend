package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.NormalLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NormalLeaveRepository extends JpaRepository<NormalLeave, Long> {
    List<NormalLeave> findByStatus(String status);
    List<NormalLeave> findByEmployeeId(Long employeeId);
}
