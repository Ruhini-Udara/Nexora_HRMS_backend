package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "overseas_leave")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverseasLeave extends LeaveRequest {

    @Column(name = "passport_number")
    private String passportNumber;

    @Column(name = "passport_exp_date")
    private LocalDate passportExpDate;

    private String branch;

    @Column(name = "contact_number")
    private String contactNumber;

    private String email;

    @Column(name = "special_remark", columnDefinition = "TEXT")
    private String specialRemark;
}
