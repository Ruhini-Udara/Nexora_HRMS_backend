package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "death_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeathRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "employee_name", nullable = false)
    private String employeeName;

    @Column(name = "employee_id_string")
    private String employeeIdString;

    @Column(name = "date_of_death")
    private LocalDate dateOfDeath;

    @Column(name = "nature_of_death")
    private String natureOfDeath;

    @Column(name = "requester_name")
    private String requesterName;

    @Column(name = "requester_branch")
    private String requesterBranch;

    @Column(name = "requester_designation")
    private String requesterDesignation;

    @Column(name = "requester_emp_id")
    private String requesterEmpId;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "special_remark", columnDefinition = "TEXT")
    private String specialRemark;

    @Column(nullable = false)
    private String status;

    @Column(name = "nominee_name")
    private String nomineeName;

    @Column(name = "nominee_bank")
    private String nomineeBank;

    @Column(name = "nominee_branch")
    private String nomineeBranch;

    @Column(name = "nominee_account")
    private String nomineeAccount;

    @Column(name = "death_certificate_doc")
    private String deathCertificateDoc;

    @Column(name = "nominee_id_doc")
    private String nomineeIdDoc;

    @Column(name = "request_letter_doc")
    private String requestLetterDoc;

    @Column(name = "hr_remark", columnDefinition = "TEXT")
    private String hrRemark;

    @Column(name = "board_meeting_date")
    private String boardMeetingDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
