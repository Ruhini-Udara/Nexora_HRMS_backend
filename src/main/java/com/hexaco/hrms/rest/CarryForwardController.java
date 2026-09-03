package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.CarryForwardBatchDetailDto;
import com.hexaco.hrms.dto.CarryForwardBatchDto;
import com.hexaco.hrms.dto.CarryForwardEntryDto;
import com.hexaco.hrms.service.CarryForwardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/carry-forward")
@RequiredArgsConstructor
public class CarryForwardController {

    private final CarryForwardService carryForwardService;

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) ? auth.getName() : null;
    }

    @GetMapping("/batches")
    public ResponseEntity<List<CarryForwardBatchDto>> getAllBatches() {
        return ResponseEntity.ok(carryForwardService.getAllBatches(getCurrentUserEmail()));
    }

    @GetMapping("/batches/{id}")
    public ResponseEntity<CarryForwardBatchDetailDto> getBatchDetails(@PathVariable String id) {
        return ResponseEntity.ok(carryForwardService.getBatchDetails(id, getCurrentUserEmail()));
    }

    @PostMapping("/upload")
    public ResponseEntity<CarryForwardBatchDetailDto> uploadBatch(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "submittedBy", required = false) String submittedBy) {
        Integer batchYear = year != null ? year : java.time.LocalDate.now().getYear();
        String currentUser = getCurrentUserEmail();
        String submitter = (submittedBy != null && !submittedBy.isBlank()) ? submittedBy
                : (currentUser != null ? currentUser : "HR Admin");
        return ResponseEntity.ok(carryForwardService.uploadBatch(file, batchYear, submitter));
    }

    @PostMapping("/generate")
    public ResponseEntity<CarryForwardBatchDetailDto> generateBatch(
            @RequestBody(required = false) Map<String, Object> payload) {
        Integer year = (payload != null && payload.get("year") != null) 
                ? Integer.parseInt(payload.get("year").toString()) : java.time.LocalDate.now().getYear();
        String currentUser = getCurrentUserEmail();
        String submittedBy = (payload != null && payload.get("submittedBy") != null) 
                ? payload.get("submittedBy").toString()
                : (currentUser != null ? currentUser : "HR Admin");

        boolean includeAnnual = payload == null || payload.get("includeAnnual") == null || Boolean.parseBoolean(payload.get("includeAnnual").toString());
        boolean includeCasual = payload != null && payload.get("includeCasual") != null && Boolean.parseBoolean(payload.get("includeCasual").toString());
        boolean includeMedical = payload != null && payload.get("includeMedical") != null && Boolean.parseBoolean(payload.get("includeMedical").toString());

        Integer annualCap = (payload != null && payload.get("annualCap") != null) ? Integer.parseInt(payload.get("annualCap").toString()) : 7;
        Integer casualCap = (payload != null && payload.get("casualCap") != null) ? Integer.parseInt(payload.get("casualCap").toString()) : 3;
        Integer medicalCap = (payload != null && payload.get("medicalCap") != null) ? Integer.parseInt(payload.get("medicalCap").toString()) : 3;

        return ResponseEntity.ok(carryForwardService.generateBatchFromLeaveBalance(
                year, submittedBy, includeAnnual, includeCasual, includeMedical, annualCap, casualCap, medicalCap));
    }

    @PostMapping("/batches/{id}/verify-branch")
    public ResponseEntity<Map<String, String>> verifyBatchByBranch(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> payload) {
        String branch = (payload != null && payload.get("branch") != null) ? payload.get("branch") : "Head Office";
        String currentUser = getCurrentUserEmail();
        String verifiedBy = (payload != null && payload.get("verifiedBy") != null) ? payload.get("verifiedBy")
                : (currentUser != null ? currentUser : "Branch Officer");
        carryForwardService.verifyBatchByBranch(id, branch, verifiedBy);
        return ResponseEntity.ok(Map.of("message", "Branch " + branch + " verified successfully."));
    }

    @PostMapping("/batches/{id}/approve")
    public ResponseEntity<Map<String, String>> approveBatch(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> payload) {
        String currentUser = getCurrentUserEmail();
        String approvedBy = (payload != null && payload.get("approvedBy") != null)
                ? payload.get("approvedBy")
                : (currentUser != null ? currentUser : "Head Office HR");
        carryForwardService.approveBatch(id, approvedBy);
        return ResponseEntity.ok(Map.of("message", "Carry forward batch approved by Head Office HR."));
    }

    @PostMapping("/batches/{id}/sync-finance")
    public ResponseEntity<Map<String, Object>> syncFinance(@PathVariable String id) {
        return ResponseEntity.ok(carryForwardService.syncToFinanceApi(id));
    }

    @PostMapping("/entries/{entryId}/audit-adjustment")
    public ResponseEntity<CarryForwardEntryDto> recordAuditAdjustment(
            @PathVariable Long entryId,
            @RequestBody(required = false) Map<String, Object> payload) {
        Integer actualDays = (payload != null && payload.get("actualDays") != null) 
                ? Integer.parseInt(payload.get("actualDays").toString()) : 0;
        BigDecimal actualAmount = (payload != null && payload.get("actualAmount") != null) 
                ? new BigDecimal(payload.get("actualAmount").toString()) : BigDecimal.ZERO;
        String reason = (payload != null && payload.get("adjustmentReason") != null) 
                ? payload.get("adjustmentReason").toString() : "";
        String currentUser = getCurrentUserEmail();
        String auditorName = (payload != null && payload.get("auditorName") != null) 
                ? payload.get("auditorName").toString()
                : (currentUser != null ? currentUser : "Auditor");

        return ResponseEntity.ok(carryForwardService.recordAuditAdjustment(entryId, actualDays, actualAmount, reason, auditorName));
    }

    @PostMapping("/batches/{id}/complete-audit")
    public ResponseEntity<Map<String, String>> completeAudit(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> payload) {
        String currentUser = getCurrentUserEmail();
        String auditorName = (payload != null && payload.get("auditorName") != null)
                ? payload.get("auditorName")
                : (currentUser != null ? currentUser : "Head Office Auditor");
        carryForwardService.completeBatchAudit(id, auditorName);
        return ResponseEntity.ok(Map.of("message", "Audit completed for batch " + id));
    }

    @GetMapping("/batches/{id}/audit-summary")
    public ResponseEntity<Map<String, Object>> getAuditSummary(@PathVariable String id) {
        return ResponseEntity.ok(carryForwardService.getAuditSummary(id));
    }
}
