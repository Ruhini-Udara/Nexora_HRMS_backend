package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.TrainingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

// Repository interface for TrainingRequest
@Repository
public interface TrainingRequestRepository extends JpaRepository<TrainingRequest, Long> {
    // Find all requests by employee
    List<TrainingRequest> findByEmployeeId(Long employeeId);
    // Find all requests for a training event
    List<TrainingRequest> findByTrainingEventId(Long eventId);
    // Check if any request exists for a training event
    boolean existsByTrainingEventId(Long eventId);

    @Query("SELECT COUNT(t) FROM TrainingRequest t WHERE LOWER(t.status) = 'pending'")
    long countPending();
}
