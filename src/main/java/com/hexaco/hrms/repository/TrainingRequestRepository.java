package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.TrainingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingRequestRepository extends JpaRepository<TrainingRequest, Long> {
    List<TrainingRequest> findByEmployeeId(Long employeeId);
    List<TrainingRequest> findByTrainingEventId(Long eventId);
}
