package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_code", unique = true)
    private String employeeCode;

    // Step 1: Personal Info
    @Column(nullable = false, unique = true)
    private String nicNumber;

    @Column(nullable = false)
    private String sex;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String surname;

    private LocalDate dateOfBirth;

    @Column(name = "joined_date")
    private LocalDate dateJoined;

    @Column(nullable = false, unique = true)
    private String email;

    private String homeAddress;
    private String maritalStatus;

    // Step 2: Employment Info
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "designation_id" , nullable = false)
    private Designation designation;

    @Column(nullable = false)
    private String employeeType;

    @Column(nullable = false)
    private String department;

    @Column(nullable = true)
    private String branch;

    @Column(name = "epf_number")
    private String epfNumber;

    @Column(name = "etf_number")
    private String etfNumber;

    @Column(nullable = true)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = true)
    private Role role;

    

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}