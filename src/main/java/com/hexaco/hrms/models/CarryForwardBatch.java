package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "carry_forward_batch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarryForwardBatch {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id; // e.g., CF-2026-001

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "DRAFT"; // DRAFT, VERIFIED, PENDING_APPROVAL, APPROVED, SYNCED

    @Column(name = "submitted_by")
    private String submittedBy;

    @Column(name = "approved_by")
    private String approvedBy;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CarryForwardEntry> entries;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "DRAFT";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
