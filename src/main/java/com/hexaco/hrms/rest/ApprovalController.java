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
    public ResponseEntity<?> createApproval(@RequestBody Approval approval) {
        try {
            Approval savedApproval = approvalService.saveApproval(approval);
            return new ResponseEntity<>(savedApproval, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    // Helper class for JSON error response
    public static class ErrorResponse {
        private String message;
        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    @GetMapping
    public ResponseEntity<List<Approval>> getApprovalsByRef(
            @RequestParam Long refId, 
            @RequestParam String refType) {
        List<Approval> approvals = approvalService.getApprovalsByRef(refId, refType);
        return ResponseEntity.ok(approvals);
    }
}
