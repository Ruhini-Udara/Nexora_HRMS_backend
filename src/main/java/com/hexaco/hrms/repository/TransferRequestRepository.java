package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.TransferRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRequestRepository extends JpaRepository<TransferRequest, Long> {
    List<TransferRequest> findByEmployeeId(Long employeeId);
}
