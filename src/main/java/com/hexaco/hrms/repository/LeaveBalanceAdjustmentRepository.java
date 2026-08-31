package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.LeaveBalanceAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveBalanceAdjustmentRepository extends JpaRepository<LeaveBalanceAdjustment, Long> {
}
