package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.Resignation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResignationRepository extends JpaRepository<Resignation, Long> {
    List<Resignation> findByEmployeeId(Long employeeId);
    List<Resignation> findByStatus(String status);

    @Query("SELECT COUNT(r) FROM Resignation r WHERE r.status = 'PENDING' OR r.status = 'SUBMITTED'")
    long countPending();
}
