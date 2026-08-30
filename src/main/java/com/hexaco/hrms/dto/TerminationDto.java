package com.hexaco.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TerminationDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String epfNumber;
    private String branch;
    private String type;
    private LocalDate initiationDate;
    private LocalDate effectiveDate;
    private String reason;
    private String specialRemark;
    private String status;
    private String requestForTerminationDoc;
    private String loanClearanceLetterDoc;
    private String otherDocumentDoc;
    private String hrRemark;
    private String directorRemark;
    private String boardMeetingDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
