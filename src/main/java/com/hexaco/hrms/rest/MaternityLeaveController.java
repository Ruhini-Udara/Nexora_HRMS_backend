package com.hexaco.hrms.rest;

import com.hexaco.hrms.models.MaternityLeave;
import com.hexaco.hrms.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaves/maternity")
@RequiredArgsConstructor
public class MaternityLeaveController {

    private final LeaveService leaveService;

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'HR', 'DIRECTOR')")
    public ResponseEntity<MaternityLeave> submitMaternityLeave(@RequestBody MaternityLeave requestedLeave) {
        MaternityLeave savedLeave = leaveService.submitMaternityLeave(requestedLeave);
        return new ResponseEntity<>(savedLeave, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR', 'DIRECTOR', 'ROLE_ADMIN', 'ROLE_HR', 'ROLE_DIRECTOR', 'admin', 'hr', 'director')")
    public ResponseEntity<List<MaternityLeave>> getAllMaternityLeaves() {
        List<MaternityLeave> leaves = leaveService.getAllMaternityLeaves();
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR', 'DIRECTOR', 'ROLE_ADMIN', 'ROLE_HR', 'ROLE_DIRECTOR', 'admin', 'hr', 'director')")
    public ResponseEntity<List<MaternityLeave>> getMaternityLeavesByStatus(@PathVariable String status) {
        List<MaternityLeave> leaves = leaveService.getMaternityLeavesByStatus(status);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<MaternityLeave>> getMaternityLeavesByEmployeeId(@PathVariable Long employeeId) {
        List<MaternityLeave> leaves = leaveService.getMaternityLeavesByEmployeeId(employeeId);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaternityLeave> getMaternityLeaveById(@PathVariable Long id) {
        return leaveService.getMaternityLeaveById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
