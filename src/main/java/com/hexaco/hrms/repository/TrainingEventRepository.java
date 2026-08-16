package com.hexaco.hrms.repository;
 
import com.hexaco.hrms.models.TrainingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
 
// Repository interface for TrainingEvent
public interface TrainingEventRepository extends JpaRepository<TrainingEvent, Long> {
    // Checks if an event with the same title and proposed start date already exists
    boolean existsByTitleAndProposedStartDate(String title, LocalDate proposedStartDate);
    // Checks if an event with the same title (case-insensitive) already exists
    boolean existsByTitleIgnoreCase(String title);
    // Checks if an event with the same title (case-insensitive) already exists, excluding a specific status
    boolean existsByTitleIgnoreCaseAndStatusNot(String title, String status);
    // Checks if an event with the same training code (case-insensitive) already exists
    boolean existsByTrainingCodeIgnoreCase(String trainingCode);
    // Checks if an event with the same training code (case-insensitive) already exists, excluding a specific status
    boolean existsByTrainingCodeIgnoreCaseAndStatusNot(String trainingCode, String status);
    // Checks if an event with the same training code (case-insensitive) exists, excluding a specific ID
    boolean existsByTrainingCodeIgnoreCaseAndIdNot(String trainingCode, Long id);
    // Checks if an event with the same training code (case-insensitive) exists, excluding a specific ID and status
    boolean existsByTrainingCodeIgnoreCaseAndIdNotAndStatusNot(String trainingCode, Long id, String status);
}
