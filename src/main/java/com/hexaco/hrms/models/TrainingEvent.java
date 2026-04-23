package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category; // e.g., Soft Skills, Technical Training

    @Column(name = "expected_participants")
    private Integer expectedParticipants;

    @Column(length = 1000)
    private String description;

    @Column(name = "proposed_start_date")
    private LocalDate proposedStartDate;

    private String time;

    @Column(name = "apply_before")
    private LocalDate applyBefore;

    private String location;

    private Double budget;

    private String instructor;

    @Column(nullable = false)
    private String status; // e.g., Published, Draft

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "Published"; // Defaulting based on frontend behavior
        }
    }
}
