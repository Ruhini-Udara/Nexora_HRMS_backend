package com.hexaco.hrms.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WelfareRequestDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String email;
    private String role;
    private String initials;
    private LocalDate requestDate;
    private String welfareType;
    private Double amount;
    private String status;
    private String remarks;
    private String supportingDocument;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
