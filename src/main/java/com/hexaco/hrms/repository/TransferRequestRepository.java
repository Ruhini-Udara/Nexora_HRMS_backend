package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.TransferRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRequestRepository extends JpaRepository<TransferRequest, Long> {
    List<TransferRequest> findByEmployeeId(Long employeeId);
    List<TransferRequest> findByStatus(String status);

    @Query("SELECT COUNT(t) FROM TransferRequest t WHERE t.status = 'SUBMITTED' OR t.status = 'VERIFIED_BY_HR' OR t.status = 'PENDING_ADMIN'")
    long countPending();
}
