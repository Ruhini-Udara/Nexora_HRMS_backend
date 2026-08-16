package com.hexaco.hrms.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeathRequestDto {
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeIdString() { return employeeIdString; }
    public void setEmployeeIdString(String employeeIdString) { this.employeeIdString = employeeIdString; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getBoardMeetingDate() { return boardMeetingDate; }
    public void setBoardMeetingDate(String boardMeetingDate) { this.boardMeetingDate = boardMeetingDate; }
    public String getHrRemark() { return hrRemark; }
    public void setHrRemark(String hrRemark) { this.hrRemark = hrRemark; }
    public LocalDate getDateOfDeath() { return dateOfDeath; }
    public void setDateOfDeath(LocalDate dateOfDeath) { this.dateOfDeath = dateOfDeath; }
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
    public String getEpfNumber() { return epfNumber; }
    public void setEpfNumber(String epfNumber) { this.epfNumber = epfNumber; }
    public String getNomineeRelationship() { return nomineeRelationship; }
    public void setNomineeRelationship(String nomineeRelationship) { this.nomineeRelationship = nomineeRelationship; }
    public String getNomineeNic() { return nomineeNic; }
    public void setNomineeNic(String nomineeNic) { this.nomineeNic = nomineeNic; }
    public String getNomineePhone() { return nomineePhone; }
    public void setNomineePhone(String nomineePhone) { this.nomineePhone = nomineePhone; }
    public String getNomineeAddress() { return nomineeAddress; }
    public void setNomineeAddress(String nomineeAddress) { this.nomineeAddress = nomineeAddress; }
    private Long id;
    private Long employeeId;
    private String employeeIdString;
    private String employeeName;
    private String epfNumber;
    private LocalDate dateOfDeath;
    private String natureOfDeath;
    private String requesterName;
    private String requesterBranch;
    private String requesterDesignation;
    private String requesterEmpId;
    private String address;
    private String contactNumber;
    private String specialRemark;
    private String status;
    private String nomineeName;
    private String nomineeRelationship;
    private String nomineeNic;
    private String nomineePhone;
    private String nomineeAddress;
    private String nomineeBank;
    private String nomineeBranch;
    private String nomineeAccount;
    private String deathCertificateDoc;
    private String nomineeIdDoc;
    private String requestLetterDoc;
    private String hrRemark;
    private String boardMeetingDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
