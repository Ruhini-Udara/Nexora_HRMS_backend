package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_balance_adjustment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalanceAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_balance_id", nullable = false)
    private LeaveBalance leaveBalance;

    @Column(name = "leave_type", nullable = false)
    private String leaveType; // ANNUAL, CASUAL, MEDICAL

    @Column(name = "old_balance", nullable = false)
    private Integer oldBalance;

    @Column(name = "new_balance", nullable = false)
    private Integer newBalance;

    @Column(name = "reason", length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adjusted_by", nullable = false)
    private Employee adjustedBy;

    @Column(name = "adjusted_at", nullable = false)
    private LocalDateTime adjustedAt;

    @PrePersist
    protected void onCreate() {
        if (adjustedAt == null) {
            adjustedAt = LocalDateTime.now();
        }
    }
}
