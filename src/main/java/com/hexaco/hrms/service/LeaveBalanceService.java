package com.hexaco.hrms.service;

import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.LeaveBalance;
import com.hexaco.hrms.models.LeavePolicy;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.repository.LeaveBalanceRepository;
import com.hexaco.hrms.repository.LeavePolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import com.hexaco.hrms.repository.NormalLeaveRepository;
import com.hexaco.hrms.models.NormalLeave;

@Service
@RequiredArgsConstructor
public class LeaveBalanceService {

    private final EmployeeRepository employeeRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final NormalLeaveRepository normalLeaveRepository;

    @Transactional
    public LeaveBalance calculateLeave(Long employeeId, int year) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        String employeeType = employee.getEmployeeType();
        if (employeeType == null) {
            employeeType = "FULL_TIME"; // fallback
        } else {
            employeeType = employeeType.toUpperCase().replace("-", "_").replace(" ", "_");
        }

        List<LeavePolicy> policies = leavePolicyRepository.findByEmployeeTypeAndIsActiveTrue(employeeType);

        int annualQuota = 0;
        int casualQuota = 0;
        int medicalQuota = 0;

        LocalDate joinedDate = employee.getDateJoined();
        boolean isFirstYear = false;
        
        if (joinedDate != null && joinedDate.getYear() == year) {
            isFirstYear = true;
        }

        for (LeavePolicy policy : policies) {
            String leaveName = policy.getLeaveType().getLeaveTypeName().toLowerCase();
            
            if (leaveName.contains("annual")) {
                if (isFirstYear) {
                    if (!joinedDate.isAfter(LocalDate.of(year, Month.APRIL, 1))) {
                        annualQuota = 14;
                    } else if (!joinedDate.isAfter(LocalDate.of(year, Month.JULY, 1))) {
                        annualQuota = 10;
                    } else if (!joinedDate.isAfter(LocalDate.of(year, Month.OCTOBER, 1))) {
                        annualQuota = 7;
                    } else {
                        annualQuota = 4;
                    }
                } else {
                    annualQuota = policy.getEntitledDays();
                }
            } else if (leaveName.contains("casual")) {
                casualQuota = policy.getEntitledDays();
            } else if (leaveName.contains("medical") || leaveName.contains("sick")) {
                medicalQuota = policy.getEntitledDays();
            }
        }

        LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployeeIdAndLeaveYear(employeeId, year)
                .orElse(new LeaveBalance());

        if ("FINALIZED".equals(leaveBalance.getStatus()) || Boolean.TRUE.equals(leaveBalance.getIsManuallyEdited())) {
            return leaveBalance; // Skip if already finalized or manually edited
        }

        leaveBalance.setEmployee(employee);
        leaveBalance.setLeaveYear(year);
        leaveBalance.setAnnualLeaveQuota(annualQuota);
        leaveBalance.setCasualLeaveQuota(casualQuota);
        leaveBalance.setMedicalLeaveQuota(medicalQuota);
        
        // Dynamically calculate used leaves by querying all approved leaves
        int annualUsed = 0;
        int casualUsed = 0;
        int medicalUsed = 0;
        
        List<NormalLeave> approvedLeaves = normalLeaveRepository.findByEmployeeIdAndStatus(employeeId, "APPROVED");
        for (NormalLeave leave : approvedLeaves) {
            if (leave.getFromDate() != null && leave.getFromDate().getYear() == year) {
                String typeName = leave.getLeaveType().getLeaveTypeName().toLowerCase();
                int days = leave.getTotalDays() != null ? leave.getTotalDays() : 0;
                
                if (typeName.contains("annual")) {
                    annualUsed += days;
                } else if (typeName.contains("casual")) {
                    casualUsed += days;
                } else if (typeName.contains("medical") || typeName.contains("sick")) {
                    medicalUsed += days;
                }
            }
        }

        leaveBalance.setAnnualLeaveUsed(annualUsed);
        leaveBalance.setCasualLeaveUsed(casualUsed);
        leaveBalance.setMedicalLeaveUsed(medicalUsed);

        return leaveBalanceRepository.save(leaveBalance);
    }
}
