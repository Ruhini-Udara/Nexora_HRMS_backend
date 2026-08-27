package com.hexaco.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarryForwardBatchDto {
    private String id;
    private Integer year;
    private String status;
    private String submittedBy;
    private String approvedBy;
    private Integer entriesCount;
    private LocalDateTime createdAt;
}
