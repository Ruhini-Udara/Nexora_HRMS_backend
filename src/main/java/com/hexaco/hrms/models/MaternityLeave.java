package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "maternity_leave")
@PrimaryKeyJoinColumn(name = "id") // Shared ID with parent LeaveRequest
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaternityLeave extends LeaveRequest {

    @Column(name = "child_number")
    private String childNumber; // e.g., '1st Child', '2nd Child'

    @Column(name = "employee_type")
    private String employeeType; // e.g., 'Permanent', 'Contract'

    private String branch;

    @Column(name = "contact_number")
    private String contactNumber;

    private String email;

    @Column(name = "special_remark", columnDefinition = "TEXT")
    private String specialRemark;
}
