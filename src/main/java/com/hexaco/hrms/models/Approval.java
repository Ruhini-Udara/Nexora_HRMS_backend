package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ref_id", nullable = false)
    private Long refId; // Link to the Leave request ID

    @Column(name = "ref_type", nullable = false)
    private String refType; // Tells us if it's 'OVERSEAS_LEAVE' or 'MATERNITY_LEAVE'

    @ManyToOne
    @JoinColumn(name = "approved_by", nullable = false)
    private Employee approvedBy;

    @Column(nullable = false)
    private String decision; // APPROVED, REJECTED, PENDING

    @Column(columnDefinition = "TEXT")
    private String remark;

    @Column(name = "decision_date")
    private LocalDateTime decisionDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
