package com.hexaco.hrms.repository;
 
import com.hexaco.hrms.models.TrainingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
 
// Repository interface for TrainingEvent
@Repository
public interface TrainingEventRepository extends JpaRepository<TrainingEvent, Long> {
    // Checks if an event with the same title and proposed start date already exists
    boolean existsByTitleAndProposedStartDate(String title, LocalDate proposedStartDate);
    // Checks if an event with the same title (case-insensitive) already exists
    boolean existsByTitleIgnoreCase(String title);
}
