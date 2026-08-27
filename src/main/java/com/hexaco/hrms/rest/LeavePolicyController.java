package com.hexaco.hrms.rest;

import com.hexaco.hrms.models.LeavePolicy;
import com.hexaco.hrms.repository.LeavePolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/leave-policies")
@RequiredArgsConstructor
public class LeavePolicyController {

    private final LeavePolicyRepository leavePolicyRepository;

    @GetMapping("/{employeeType}")
    public ResponseEntity<Map<String, Object>> getLeavePolicies(@PathVariable String employeeType) {
        String normalizedType = employeeType != null ? employeeType.toUpperCase().replace("-", "_").replace(" ", "_") : "FULL_TIME";
        List<LeavePolicy> policies = leavePolicyRepository.findByEmployeeTypeAndIsActiveTrue(normalizedType);
        
        if (policies.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("employeeType", employeeType);
        
        int total = 0;
        for (LeavePolicy policy : policies) {
            String leaveName = policy.getLeaveType().getLeaveTypeName().toLowerCase();
            if (leaveName.contains("annual")) {
                response.put("annualLeave", policy.getEntitledDays());
                total += policy.getEntitledDays();
            } else if (leaveName.contains("casual")) {
                response.put("casualLeave", policy.getEntitledDays());
                total += policy.getEntitledDays();
            } else if (leaveName.contains("medical") || leaveName.contains("sick")) {
                response.put("medicalLeave", policy.getEntitledDays());
                total += policy.getEntitledDays();
            }
        }
        
        response.put("total", total);
        
        return ResponseEntity.ok(response);
    }
}
