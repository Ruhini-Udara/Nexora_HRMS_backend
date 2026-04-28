package com.hexaco.hrms.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequestDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String epfNumber;
    private String designation;
    private String branch;
    private String currentBranch;
    private String targetBranch;
    private String transferType;
    private String reason;
    private LocalDate requestDate;
    private LocalDate expectedDate;
    private String status;
    private String justificationDocumentPath;
    private String proofDocumentPath;
    private String hrRemark;
    private String boardMeetingDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
