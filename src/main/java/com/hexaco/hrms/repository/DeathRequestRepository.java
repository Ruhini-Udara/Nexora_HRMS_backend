package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.DeathRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeathRequestRepository extends JpaRepository<DeathRequest, Long> {
    List<DeathRequest> findByEmployeeId(Long employeeId);
    List<DeathRequest> findByStatus(String status);

    @Query("SELECT COUNT(d) FROM DeathRequest d WHERE d.status = 'PENDING' OR d.status = 'SUBMITTED'")
    long countPending();
}
