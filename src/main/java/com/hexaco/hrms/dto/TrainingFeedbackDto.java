package com.hexaco.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingFeedbackDto {
    private Long id;
    private Long eventId;
    private Long employeeId;
    private String employeeName;
    private String workEmail;
    
    private String attendanceStatus;
    private String feedback;
    private Integer courseContentRating;
    private Integer instructorRating;
    private Integer overallExperienceRating;
    private String suggestions;
}
