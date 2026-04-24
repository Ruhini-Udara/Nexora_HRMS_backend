package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private TrainingEvent trainingEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "date_submitted")
    private LocalDate dateSubmitted;

    @Column(nullable = false)
    private String status; // Pending, Approved, Rejected

    @Column(length = 2000)
    private String justification;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "attachment_path")
    private String attachmentPath;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (dateSubmitted == null) {
            dateSubmitted = LocalDate.now();
        }
        if (status == null) {
            status = "Pending";
        }
    }
}
