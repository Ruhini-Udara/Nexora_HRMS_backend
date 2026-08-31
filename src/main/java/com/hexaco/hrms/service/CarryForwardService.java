package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.CarryForwardBatchDto;
import com.hexaco.hrms.dto.CarryForwardBatchDetailDto;
import com.hexaco.hrms.dto.CarryForwardEntryDto;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CarryForwardService {
    List<CarryForwardBatchDto> getAllBatches(String userEmail);
    CarryForwardBatchDetailDto getBatchDetails(String batchId, String userEmail);
    CarryForwardBatchDetailDto uploadBatch(MultipartFile file, Integer year, String submittedBy);
    CarryForwardBatchDetailDto generateBatchFromLeaveBalance(
            Integer year,
            String submittedBy,
            boolean includeAnnual,
            boolean includeCasual,
            boolean includeMedical,
            Integer annualCap,
            Integer casualCap,
            Integer medicalCap);
    void verifyBatchByBranch(String batchId, String branch, String verifiedBy);
    void approveBatch(String batchId, String approvedBy);
    Map<String, Object> syncToFinanceApi(String batchId);
    CarryForwardEntryDto recordAuditAdjustment(Long entryId, Integer actualDays, BigDecimal actualAmount, String reason, String auditorName);
    void completeBatchAudit(String batchId, String auditorName);
    Map<String, Object> getAuditSummary(String batchId);
}
