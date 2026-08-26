package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.PayrollLeaveDto;
import com.hexaco.hrms.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @GetMapping("/approved-leaves")
    public ResponseEntity<List<PayrollLeaveDto>> getApprovedLeavesForPayroll(
            @RequestParam int month,
            @RequestParam int year) {
        
        List<PayrollLeaveDto> leaves = payrollService.getApprovedLeavesForPayroll(month, year);
        return ResponseEntity.ok(leaves);
    }
}
