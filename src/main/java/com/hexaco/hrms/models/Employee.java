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
    @Column(nullable = false)
    private String designation;

    @Column(nullable = false)
    private String employeeType;

    @Column(nullable = false)
    private String department;


    
    
    @Column(name = "epf_number")
    private String epfNumber;

    @Column(name = "etf_number")
    private String etfNumber;

    

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
