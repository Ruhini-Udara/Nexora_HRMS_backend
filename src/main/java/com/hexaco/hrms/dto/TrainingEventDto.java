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
public class TrainingEventDto {
    private Long id;
    private String title;
    private String category;
    private Integer expectedParticipants;
    private String description;
    private LocalDate proposedStartDate;
    private String time;
    private LocalDate applyBefore;
    private String location;
    private Double budget;
    private String instructor;
    private String status;
}
