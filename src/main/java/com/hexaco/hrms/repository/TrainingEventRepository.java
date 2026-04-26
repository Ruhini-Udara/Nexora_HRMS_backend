package com.hexaco.hrms.repository;
 
import com.hexaco.hrms.models.TrainingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
 
@Repository
public interface TrainingEventRepository extends JpaRepository<TrainingEvent, Long> {
    boolean existsByTitleAndProposedStartDate(String title, LocalDate proposedStartDate);
    boolean existsByTitleIgnoreCase(String title);
}
