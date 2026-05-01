package com.hexaco.hrms.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResignationDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String epfNumber;
    private String designation;
    private String branch;
    private LocalDate resignationDate;
    private LocalDate lastWorkingDate;
    private String obligationDetails;
    private String reason;
    private String specialRemark;
    private String status;
    private String resignationLetterDoc;
    private String clearanceLetterDoc;
    private String handoverChecklistDoc;
    private String hrRemark;
    private String directorRemark;
    private String boardMeetingDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
