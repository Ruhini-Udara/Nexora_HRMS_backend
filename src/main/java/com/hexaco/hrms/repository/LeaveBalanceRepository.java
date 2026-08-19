package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    Optional<LeaveBalance> findByEmployeeIdAndYear(Long employeeId, Integer year);
    List<LeaveBalance> findByYear(Integer year);
    List<LeaveBalance> findByEmployeeBranchAndYear(String branch, Integer year);
}
