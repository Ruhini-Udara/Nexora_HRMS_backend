package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "leave_type_name", unique = true, nullable = false)
    private String leaveTypeName; // e.g., 'Overseas Leave', 'Maternity Leave'

    private String description;

    @Column(name = "requires_approvals")
    private Boolean requiresApprovals;

    @Column(name = "default_days_per_year")
    private Integer defaultDaysPerYear;
}
