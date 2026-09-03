package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "carry_forward_entry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarryForwardEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false, updatable = false)
    @JsonIgnore
    private CarryForwardBatch batch;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false, updatable = false)
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    private Employee employee;

    @Column(name = "carried_forward_days", nullable = false)
    private Integer carriedForwardDays;

    @Column(name = "daily_rate", precision = 12, scale = 2)
    private BigDecimal dailyRate;

    @Column(name = "calculated_amount", precision = 12, scale = 2)
    private BigDecimal calculatedAmount;

    @Column(name = "paid_amount", precision = 12, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "is_branch_verified")
    @Builder.Default
    private Boolean isBranchVerified = false;

    @Column(name = "branch_verified_by")
    private String branchVerifiedBy;

    @Column(name = "branch_verified_at")
    private LocalDateTime branchVerifiedAt;

    // Auditing fields
    @Column(name = "actual_days")
    private Integer actualDays;

    @Column(name = "actual_amount", precision = 12, scale = 2)
    private BigDecimal actualAmount;

    @Column(name = "adjustment_amount", precision = 12, scale = 2)
    private BigDecimal adjustmentAmount; // < 0: Overpaid deduction, > 0: Underpaid addition

    @Column(name = "audit_status")
    @Builder.Default
    private String auditStatus = "PENDING_AUDIT"; // PENDING_AUDIT, MATCHED, DISCREPANCY_OVERPAID, DISCREPANCY_UNDERPAID

    @Column(name = "adjustment_reason")
    private String adjustmentReason;

    @Column(name = "payroll_applied")
    @Builder.Default
    private Boolean payrollApplied = false;

    @Column(name = "audited_by")
    private String auditedBy;

    @Column(name = "audited_at")
    private LocalDateTime auditedAt;

    @Column(name = "remarks")
    private String remarks;
}
