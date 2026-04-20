package com.hexaco.hrms.rest;

import com.hexaco.hrms.models.Approval;
import com.hexaco.hrms.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping
    public ResponseEntity<Approval> createApproval(@RequestBody Approval approval) {
        Approval savedApproval = approvalService.saveApproval(approval);
        return new ResponseEntity<>(savedApproval, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Approval>> getApprovalsByRef(
            @RequestParam Long refId, 
            @RequestParam String refType) {
        List<Approval> approvals = approvalService.getApprovalsByRef(refId, refType);
        return ResponseEntity.ok(approvals);
    }
}
