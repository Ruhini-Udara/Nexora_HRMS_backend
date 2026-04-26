package com.hexaco.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingRequestDto {
    private Long id;
    private Long eventId;
    private Long employeeId;
    private String employeeName;
    private String epfNumber;
    private String department;
    private String designation;
    private String personalEmail;
    
    private String trainingTitle;
    private String trainingCategory;
    private LocalDate trainingDate;
    private String trainingTime;
    
    private LocalDate dateSubmitted;
    private String status;
    private String eventStatus;
    private String eventRejectionReason;
    private Integer age;
    private String justification;
    private String rejectionReason;
    private String attachmentPath;
}
