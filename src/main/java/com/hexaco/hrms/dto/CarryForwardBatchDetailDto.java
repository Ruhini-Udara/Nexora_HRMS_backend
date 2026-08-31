package com.hexaco.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarryForwardBatchDetailDto {
    private String id;
    private Integer year;
    private String status;
    private String submittedBy;
    private String approvedBy;
    private LocalDateTime createdAt;
    private String financeReferenceId;
    private String financeStatus;
    private LocalDateTime sentToFinanceAt;
    private String auditedBy;
    private LocalDateTime auditedAt;

    private int totalEmployees;
    private int totalCarriedDays;
    private BigDecimal totalCalculatedAmount;
    private BigDecimal totalPaidAmount;
    private BigDecimal totalAdjustmentAmount;

    private List<BranchGroupDto> branches;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchGroupDto {
        private String branchName;
        private int employeeCount;
        private int totalCarriedDays;
        private BigDecimal totalCalculatedAmount;
        private BigDecimal totalPaidAmount;
        private BigDecimal totalAdjustmentAmount;
        private boolean isBranchVerified;
        private String branchVerifiedBy;
        private LocalDateTime branchVerifiedAt;
        private List<CarryForwardEntryDto> entries;
    }
}
