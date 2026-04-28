package com.hexaco.hrms.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeathRequestDto {
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
