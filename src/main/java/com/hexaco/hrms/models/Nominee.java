package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nominee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nominee {

    @Id
    @Column(name = "employee_id")
    private Long employeeId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "nominee_name")
    private String nomineeName;

    private String relationship;
    
    private String nic;

    @Column(name = "phone_no")
    private String phoneNo;

    private String address;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_branch")
    private String bankBranch;

    @Column(name = "account_number")
    private String accountNumber;
}
