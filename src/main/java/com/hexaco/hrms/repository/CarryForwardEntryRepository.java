package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.CarryForwardEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarryForwardEntryRepository extends JpaRepository<CarryForwardEntry, Long> {
    List<CarryForwardEntry> findByBatchId(String batchId);
    List<CarryForwardEntry> findByBatchIdAndEmployee_Branch(String batchId, String branch);
    List<CarryForwardEntry> findByEmployee_Id(Long employeeId);
    List<CarryForwardEntry> findByEmployee_IdAndPayrollAppliedFalseAndAdjustmentAmountNotNull(Long employeeId);
}
