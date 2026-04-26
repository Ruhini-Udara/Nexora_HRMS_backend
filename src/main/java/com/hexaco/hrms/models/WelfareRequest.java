package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "welfare_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WelfareRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "request_date")
    private LocalDate requestDate;

    @Column(name = "welfare_type")
    private String welfareType;

    @Column(name = "amount")
    private Double amount;

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "supporting_document")
    private String supportingDocument;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
