package com.hexaco.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
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
    private String financeReferenceId;
    private String financeStatus;
    private String auditedBy;
    private LocalDateTime auditedAt;
    private Integer entriesCount;
    private Integer totalCarriedDays;
    private BigDecimal totalCalculatedAmount;
    private BigDecimal totalPaidAmount;
    private LocalDateTime createdAt;
}
