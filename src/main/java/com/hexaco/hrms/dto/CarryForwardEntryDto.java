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
public class CarryForwardEntryDto {
    private Long id;
    private String empId;
    private String name;
    private String department;
    private String branch;
    private Integer carriedForwardDays;
    private BigDecimal dailyRate;
    private BigDecimal calculatedAmount;
    private BigDecimal paidAmount;
    private Boolean isBranchVerified;
    private String branchVerifiedBy;
    private LocalDateTime branchVerifiedAt;
    
    // Auditing fields
    private Integer actualDays;
    private BigDecimal actualAmount;
    private BigDecimal adjustmentAmount;
    private String auditStatus;
    private String adjustmentReason;
    private Boolean payrollApplied;
    private String auditedBy;
    private LocalDateTime auditedAt;

    private String remarks;
}
