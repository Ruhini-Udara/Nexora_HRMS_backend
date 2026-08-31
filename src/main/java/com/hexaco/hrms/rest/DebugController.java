package com.hexaco.hrms.rest;

import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.LeaveBalance;
import com.hexaco.hrms.models.LeavePolicy;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.repository.LeaveBalanceRepository;
import com.hexaco.hrms.repository.LeavePolicyRepository;
import com.hexaco.hrms.service.LeaveBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/public/debug")
@RequiredArgsConstructor
public class DebugController {

    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final LeaveBalanceService leaveBalanceService;

    @GetMapping("/emp/{id}")
    public ResponseEntity<Map<String, Object>> debugEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(getEmployeeDebug(id));
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> debugAll() {
        return ResponseEntity.ok(employeeRepository.findAll().stream().map(emp -> getEmployeeDebug(emp.getId())).collect(Collectors.toList()));
    }

    private Map<String, Object> getEmployeeDebug(Long id) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Employee> empOpt = employeeRepository.findById(id);
        if (!empOpt.isPresent()) {
            response.put("error", "Employee not found");
            return response;
        }
        
        Employee emp = empOpt.get();
        response.put("id", emp.getId());
        response.put("name", emp.getFullName());
        response.put("employeeType", emp.getEmployeeType());
        response.put("joinedDate", emp.getDateJoined());
        
        String empType = emp.getEmployeeType() != null ? emp.getEmployeeType() : "Full Time";
        List<LeavePolicy> policies = leavePolicyRepository.findByEmployeeTypeAndIsActiveTrue(empType);
        response.put("policiesCount", policies.size());
        
        List<Map<String, Object>> policyDetails = new java.util.ArrayList<>();
        for (LeavePolicy p : policies) {
            Map<String, Object> pd = new HashMap<>();
            pd.put("employeeType", p.getEmployeeType());
            pd.put("leaveType", p.getLeaveType().getLeaveTypeName());
            pd.put("entitledDays", p.getEntitledDays());
            policyDetails.add(pd);
        }
        response.put("policies", policyDetails);
        
        Optional<LeaveBalance> lbOpt = leaveBalanceRepository.findByEmployeeIdAndLeaveYear(id, 2026);
        if (lbOpt.isPresent()) {
            LeaveBalance lb = lbOpt.get();
            response.put("leaveBalance", lb);
        } else {
            response.put("leaveBalance", "Not Found");
        }
        
        return response;
    }
}
