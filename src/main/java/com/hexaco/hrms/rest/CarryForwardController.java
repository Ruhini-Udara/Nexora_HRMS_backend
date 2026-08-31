package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.CarryForwardBatchDetailDto;
import com.hexaco.hrms.dto.CarryForwardBatchDto;
import com.hexaco.hrms.dto.CarryForwardEntryDto;
import com.hexaco.hrms.service.CarryForwardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/carry-forward")
@RequiredArgsConstructor
public class CarryForwardController {

    private final CarryForwardService carryForwardService;

    @GetMapping("/batches")
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<CarryForwardBatchDto>> getAllBatches(Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(carryForwardService.getAllBatches(email));
    }

    @GetMapping("/batches/{id}")
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'ADMIN')")
    public ResponseEntity<CarryForwardBatchDetailDto> getBatchDetails(@PathVariable String id, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(carryForwardService.getBatchDetails(id, email));
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<CarryForwardBatchDetailDto> uploadBatch(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "submittedBy", required = false) String submittedBy,
            Principal principal) {
        Integer batchYear = year != null ? year : java.time.LocalDate.now().getYear();
        String submitter = submittedBy != null && !submittedBy.isBlank() ? submittedBy
                : (principal != null ? principal.getName() : "HR Admin");
        return ResponseEntity.ok(carryForwardService.uploadBatch(file, batchYear, submitter));
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public ResponseEntity<CarryForwardBatchDetailDto> generateBatch(
            @RequestBody(required = false) Map<String, Object> payload,
            Principal principal) {
        Integer year = (payload != null && payload.get("year") != null) 
                ? Integer.parseInt(payload.get("year").toString()) : java.time.LocalDate.now().getYear();
        String submittedBy = (payload != null && payload.get("submittedBy") != null) 
                ? payload.get("submittedBy").toString()
                : (principal != null ? principal.getName() : "HR Admin");

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
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'ADMIN')")
    public ResponseEntity<Map<String, String>> verifyBatchByBranch(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> payload,
            Principal principal) {
        String branch = (payload != null && payload.get("branch") != null) ? payload.get("branch") : "Head Office";
        String verifiedBy = (payload != null && payload.get("verifiedBy") != null) ? payload.get("verifiedBy")
                : (principal != null ? principal.getName() : "Branch Officer");
        carryForwardService.verifyBatchByBranch(id, branch, verifiedBy);
        return ResponseEntity.ok(Map.of("message", "Branch " + branch + " verified successfully."));
    }

    @PostMapping("/batches/{id}/approve")
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'ADMIN')")
    public ResponseEntity<Map<String, String>> approveBatch(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> payload,
            Principal principal) {
        String approvedBy = (payload != null && payload.get("approvedBy") != null)
                ? payload.get("approvedBy")
                : (principal != null ? principal.getName() : "Head Office HR");
        carryForwardService.approveBatch(id, approvedBy);
        return ResponseEntity.ok(Map.of("message", "Carry forward batch approved by Head Office HR."));
    }

    @PostMapping("/batches/{id}/sync-finance")
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> syncFinance(@PathVariable String id) {
        return ResponseEntity.ok(carryForwardService.syncToFinanceApi(id));
    }

    @PostMapping("/entries/{entryId}/audit-adjustment")
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'ADMIN')")
    public ResponseEntity<CarryForwardEntryDto> recordAuditAdjustment(
            @PathVariable Long entryId,
            @RequestBody(required = false) Map<String, Object> payload,
            Principal principal) {
        Integer actualDays = (payload != null && payload.get("actualDays") != null) 
                ? Integer.parseInt(payload.get("actualDays").toString()) : 0;
        BigDecimal actualAmount = (payload != null && payload.get("actualAmount") != null) 
                ? new BigDecimal(payload.get("actualAmount").toString()) : BigDecimal.ZERO;
        String reason = (payload != null && payload.get("adjustmentReason") != null) 
                ? payload.get("adjustmentReason").toString() : "";
        String auditorName = (payload != null && payload.get("auditorName") != null) 
                ? payload.get("auditorName").toString()
                : (principal != null ? principal.getName() : "Auditor");

        return ResponseEntity.ok(carryForwardService.recordAuditAdjustment(entryId, actualDays, actualAmount, reason, auditorName));
    }

    @PostMapping("/batches/{id}/complete-audit")
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'ADMIN')")
    public ResponseEntity<Map<String, String>> completeAudit(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> payload,
            Principal principal) {
        String auditorName = (payload != null && payload.get("auditorName") != null)
                ? payload.get("auditorName")
                : (principal != null ? principal.getName() : "Head Office Auditor");
        carryForwardService.completeBatchAudit(id, auditorName);
        return ResponseEntity.ok(Map.of("message", "Audit completed for batch " + id));
    }

    @GetMapping("/batches/{id}/audit-summary")
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getAuditSummary(@PathVariable String id) {
        return ResponseEntity.ok(carryForwardService.getAuditSummary(id));
    }
}
