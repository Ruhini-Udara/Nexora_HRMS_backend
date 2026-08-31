package com.hexaco.hrms.rest;

import com.hexaco.hrms.models.LeavePolicy;
import com.hexaco.hrms.repository.LeavePolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping({"/api/v1/leave-policies", "/api/leave-policies"})
@RequiredArgsConstructor
public class LeavePolicyController {

    private final LeavePolicyRepository leavePolicyRepository;

    @GetMapping("/{employeeType}")
    public ResponseEntity<Map<String, Object>> getLeavePolicies(@PathVariable String employeeType) {
        if (employeeType == null || employeeType.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String rawType = employeeType.trim();
        String normalizedType = rawType.toUpperCase().replace("-", "_").replace(" ", "_");

        List<String> possibleTypes = new ArrayList<>();
        possibleTypes.add(normalizedType);
        possibleTypes.add(rawType);
        possibleTypes.add(rawType.toLowerCase());

        if (normalizedType.contains("PROBATION")) {
            possibleTypes.add("PROBATION");
            possibleTypes.add("PROBATIONARY");
            possibleTypes.add("Probationary");
            possibleTypes.add("Probation");
            possibleTypes.add("probationary");
        } else if (normalizedType.contains("FULL")) {
            possibleTypes.add("FULL_TIME");
            possibleTypes.add("Full-time");
            possibleTypes.add("Full Time");
            possibleTypes.add("full-time");
        } else if (normalizedType.contains("PART")) {
            possibleTypes.add("PART_TIME");
            possibleTypes.add("Part-time");
            possibleTypes.add("Part Time");
            possibleTypes.add("part-time");
        } else if (normalizedType.contains("TEMP")) {
            possibleTypes.add("TEMPORARY");
            possibleTypes.add("Temporary");
            possibleTypes.add("temporary");
        }

        List<LeavePolicy> policies = Collections.emptyList();
        for (String type : possibleTypes) {
            List<LeavePolicy> found = leavePolicyRepository.findByEmployeeTypeAndIsActiveTrue(type);
            if (found != null && !found.isEmpty()) {
                policies = found;
                break;
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("employeeType", employeeType);

        int annualLeave = 0;
        int casualLeave = 0;
        int medicalLeave = 0;

        if (!policies.isEmpty()) {
            for (LeavePolicy policy : policies) {
                if (policy.getLeaveType() == null || policy.getLeaveType().getLeaveTypeName() == null) continue;
                String leaveName = policy.getLeaveType().getLeaveTypeName().toLowerCase();
                int entitled = policy.getEntitledDays() != null ? policy.getEntitledDays() : 0;

                if (leaveName.contains("annual")) {
                    annualLeave = entitled;
                } else if (leaveName.contains("casual")) {
                    casualLeave = entitled;
                } else if (leaveName.contains("medical") || leaveName.contains("sick")) {
                    medicalLeave = entitled;
                }
            }
        } else {
            // Default fallback if not yet seeded for a specific type
            if (normalizedType.contains("PART") || normalizedType.contains("TEMP")) {
                annualLeave = 7;
                casualLeave = 4;
                medicalLeave = 7;
            } else {
                annualLeave = 14;
                casualLeave = 7;
                medicalLeave = 14;
            }
        }

        response.put("annualLeave", annualLeave);
        response.put("casualLeave", casualLeave);
        response.put("medicalLeave", medicalLeave);
        response.put("total", annualLeave + casualLeave + medicalLeave);

        return ResponseEntity.ok(response);
    }
}
