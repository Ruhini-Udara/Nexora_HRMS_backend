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

    @Column(name = "supporting_document")
    private String supportingDocument;

    @Column(name = "designation")
    private String designation;

    @Column(name = "branch")
    private String branch;

    @Column(name = "epf_number")
    private String epfNumber;

    @Column(name = "employee_remarks", columnDefinition = "TEXT")
    private String employeeRemarks;

    @Column(name = "hr_remarks", columnDefinition = "TEXT")
    private String hrRemarks;

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
