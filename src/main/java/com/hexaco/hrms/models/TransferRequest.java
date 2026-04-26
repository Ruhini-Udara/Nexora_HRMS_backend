package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "request_date")
    private LocalDate requestDate;

    @Column(name = "current_branch")
    private String currentBranch;

    @Column(name = "target_branch")
    private String targetBranch;

    @Column(name = "transfer_type")
    private String transferType;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "expected_date")
    private LocalDate expectedDate;

    @Column(nullable = false)
    private String status; // SUBMITTED, VERIFIED_BY_HR, PENDING_ADMIN, REJECTED



    @Column(name = "justification_document_path")
    private String justificationDocumentPath;

    @Column(name = "proof_document_path")
    private String proofDocumentPath;

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
