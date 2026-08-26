package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.NormalLeaveDto;
import com.hexaco.hrms.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaves/normal")
@RequiredArgsConstructor
public class NormalLeaveController {

    private final LeaveService leaveService;

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'HR', 'DIRECTOR')")
    public ResponseEntity<NormalLeaveDto> submitNormalLeave(@RequestBody NormalLeaveDto dto) {
        NormalLeaveDto savedLeave = leaveService.submitNormalLeave(dto);
        return new ResponseEntity<>(savedLeave, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR', 'DIRECTOR', 'SUPERVISOR', 'ROLE_ADMIN', 'ROLE_HR', 'ROLE_DIRECTOR', 'ROLE_SUPERVISOR', 'admin', 'hr', 'director', 'supervisor', 'Admin', 'Hr', 'Director', 'Supervisor')")
    public ResponseEntity<List<NormalLeaveDto>> getAllNormalLeaves() {
        List<NormalLeaveDto> leaves = leaveService.getAllNormalLeaves();
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR', 'DIRECTOR', 'SUPERVISOR', 'ROLE_ADMIN', 'ROLE_HR', 'ROLE_DIRECTOR', 'ROLE_SUPERVISOR', 'admin', 'hr', 'director', 'supervisor', 'Admin', 'Hr', 'Director', 'Supervisor')")
    public ResponseEntity<List<NormalLeaveDto>> getNormalLeavesByStatus(@PathVariable String status) {
        List<NormalLeaveDto> leaves = leaveService.getNormalLeavesByStatus(status);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<NormalLeaveDto>> getNormalLeavesByEmployeeId(@PathVariable Long employeeId) {
        List<NormalLeaveDto> leaves = leaveService.getNormalLeavesByEmployeeId(employeeId);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NormalLeaveDto> getNormalLeaveById(@PathVariable Long id) {
        return leaveService.getNormalLeaveById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/impact")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR', 'DIRECTOR', 'ROLE_ADMIN', 'ROLE_HR', 'ROLE_DIRECTOR', 'admin', 'hr', 'director')")
    public ResponseEntity<com.hexaco.hrms.dto.LeaveImpactDto> getNormalLeaveImpact(@PathVariable Long id) {
        com.hexaco.hrms.dto.LeaveImpactDto impact = leaveService.getNormalLeaveImpact(id);
        return ResponseEntity.ok(impact);
    }
}
