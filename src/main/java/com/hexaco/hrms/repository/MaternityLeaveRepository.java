package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.MaternityLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaternityLeaveRepository extends JpaRepository<MaternityLeave, Long> {
    List<MaternityLeave> findByStatus(String status);
    List<MaternityLeave> findByEmployeeId(Long employeeId);
}
