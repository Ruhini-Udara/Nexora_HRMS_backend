package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private TrainingEvent trainingEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "attendance_status")
    private String attendanceStatus; // e.g., Confirmed, Absent

    @Column(length = 2000)
    private String feedback;

    @Column(name = "course_content_rating")
    private Integer courseContentRating;

    @Column(name = "instructor_rating")
    private Integer instructorRating;

    @Column(name = "overall_experience_rating")
    private Integer overallExperienceRating;

    @Column(length = 1000)
    private String suggestions;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
