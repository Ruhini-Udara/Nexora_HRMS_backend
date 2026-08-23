package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_balance", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "leave_year"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "leave_year", nullable = false)
    private Integer year;

    @Column(name = "annual_leave_quota")
    private Integer annualLeaveQuota;

    @Column(name = "casual_leave_quota")
    private Integer casualLeaveQuota;

    @Column(name = "medical_leave_quota")
    private Integer medicalLeaveQuota;

    @Column(name = "annual_leave_used")
    @Builder.Default
    private Integer annualLeaveUsed = 0;

    @Column(name = "casual_leave_used")
    @Builder.Default
    private Integer casualLeaveUsed = 0;

    @Column(name = "medical_leave_used")
    @Builder.Default
    private Integer medicalLeaveUsed = 0;

    @Column(nullable = false)
    @Builder.Default
    private String status = "CALCULATED"; // CALCULATED, FINALIZED

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "finalized_by")
    private Employee finalizedBy;

    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;

    @Column(name = "is_manually_edited")
    @Builder.Default
    private Boolean isManuallyEdited = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "last_edited_by")
    private Employee lastEditedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (annualLeaveUsed == null) annualLeaveUsed = 0;
        if (casualLeaveUsed == null) casualLeaveUsed = 0;
        if (medicalLeaveUsed == null) medicalLeaveUsed = 0;
        if (isManuallyEdited == null) isManuallyEdited = false;
        if (status == null) status = "CALCULATED";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
