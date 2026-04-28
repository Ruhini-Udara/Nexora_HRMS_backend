package com.hexaco.hrms.repository;

import com.hexaco.hrms.models.TrainingFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Repository interface for TrainingFeedback
@Repository
public interface TrainingFeedbackRepository extends JpaRepository<TrainingFeedback, Long> {
    // Find all feedbacks for a specific training event
    List<TrainingFeedback> findByTrainingEventId(Long eventId);
    // Find all feedbacks submitted by a specific employee
    List<TrainingFeedback> findByEmployeeId(Long employeeId);
    Optional<TrainingFeedback> findByTrainingEventIdAndEmployeeId(Long eventId, Long employeeId);
    boolean existsByTrainingEventIdAndEmployeeId(Long eventId, Long employeeId);
}
