package com.hexaco.hrms.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "death_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeathRequest {
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeIdString() { return employeeIdString; }
    public void setEmployeeIdString(String employeeIdString) { this.employeeIdString = employeeIdString; }
    public LocalDate getDateOfDeath() { return dateOfDeath; }
    public void setDateOfDeath(LocalDate dateOfDeath) { this.dateOfDeath = dateOfDeath; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getBoardMeetingDate() { return boardMeetingDate; }
    public void setBoardMeetingDate(String boardMeetingDate) { this.boardMeetingDate = boardMeetingDate; }
    public String getHrRemark() { return hrRemark; }
    public void setHrRemark(String hrRemark) { this.hrRemark = hrRemark; }
    public String getNatureOfDeath() { return natureOfDeath; }
    public void setNatureOfDeath(String natureOfDeath) { this.natureOfDeath = natureOfDeath; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public String getRequesterBranch() { return requesterBranch; }
    public void setRequesterBranch(String requesterBranch) { this.requesterBranch = requesterBranch; }
    public String getRequesterDesignation() { return requesterDesignation; }
    public void setRequesterDesignation(String requesterDesignation) { this.requesterDesignation = requesterDesignation; }
    public String getRequesterEmpId() { return requesterEmpId; }
    public void setRequesterEmpId(String requesterEmpId) { this.requesterEmpId = requesterEmpId; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getSpecialRemark() { return specialRemark; }
    public void setSpecialRemark(String specialRemark) { this.specialRemark = specialRemark; }
    public String getNomineeName() { return nomineeName; }
    public void setNomineeName(String nomineeName) { this.nomineeName = nomineeName; }
    public String getNomineeBank() { return nomineeBank; }
    public void setNomineeBank(String nomineeBank) { this.nomineeBank = nomineeBank; }
    public String getNomineeBranch() { return nomineeBranch; }
    public void setNomineeBranch(String nomineeBranch) { this.nomineeBranch = nomineeBranch; }
    public String getNomineeAccount() { return nomineeAccount; }
    public void setNomineeAccount(String nomineeAccount) { this.nomineeAccount = nomineeAccount; }
    public String getDeathCertificateDoc() { return deathCertificateDoc; }
    public void setDeathCertificateDoc(String deathCertificateDoc) { this.deathCertificateDoc = deathCertificateDoc; }
    public String getNomineeIdDoc() { return nomineeIdDoc; }
    public void setNomineeIdDoc(String nomineeIdDoc) { this.nomineeIdDoc = nomineeIdDoc; }
    public String getRequestLetterDoc() { return requestLetterDoc; }
    public void setRequestLetterDoc(String requestLetterDoc) { this.requestLetterDoc = requestLetterDoc; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

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
