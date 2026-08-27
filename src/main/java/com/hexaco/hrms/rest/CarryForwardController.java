package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.CarryForwardBatchDetailDto;
import com.hexaco.hrms.dto.CarryForwardBatchDto;
import com.hexaco.hrms.service.CarryForwardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/carry-forward")
@RequiredArgsConstructor
public class CarryForwardController {

    private final CarryForwardService carryForwardService;

    @GetMapping("/batches")
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<CarryForwardBatchDto>> getAllBatches() {
        return ResponseEntity.ok(carryForwardService.getAllBatches());
    }

    @GetMapping("/batches/{id}")
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'ADMIN')")
    public ResponseEntity<CarryForwardBatchDetailDto> getBatchDetails(@PathVariable String id) {
        return ResponseEntity.ok(carryForwardService.getBatchDetails(id));
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<CarryForwardBatchDetailDto> uploadBatch(
            @RequestParam("file") MultipartFile file,
            @RequestParam("year") Integer year,
            @RequestParam("submittedBy") String submittedBy) {
        return ResponseEntity.ok(carryForwardService.uploadBatch(file, year, submittedBy));
    }

    @PutMapping("/batches/{id}/status")
    @PreAuthorize("hasAnyRole('HR', 'DIRECTOR', 'ADMIN')")
    public ResponseEntity<Void> updateBatchStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        String approvedBy = payload.get("approvedBy");
        carryForwardService.updateBatchStatus(id, status, approvedBy);
        return ResponseEntity.ok().build();
    }
}
