package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.LeaveBalance;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.repository.LeaveBalanceRepository;
import com.hexaco.hrms.service.LeaveCalculationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveCalculationServiceImpl implements LeaveCalculationService {

    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final com.hexaco.hrms.service.LeaveBalanceService leaveBalanceService;

    public LeaveCalculationServiceImpl(EmployeeRepository employeeRepository, LeaveBalanceRepository leaveBalanceRepository, com.hexaco.hrms.service.LeaveBalanceService leaveBalanceService) {
        this.employeeRepository = employeeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leaveBalanceService = leaveBalanceService;
    }

    @Override
    @Transactional
    public void calculateLeaveForYear(int year) {
        List<Employee> employees = employeeRepository.findAll();
        LocalDate cutOffDate = LocalDate.of(2011, 7, 2);

        for (Employee employee : employees) {
            // Use the new dynamic LeaveBalanceService to calculate based on policies
            leaveBalanceService.calculateLeave(employee.getId(), year);
        }
    }

    private void saveOrUpdateLeaveBalance(Employee employee, int year, int annual, int casual, int medical) {
        Optional<LeaveBalance> existing = leaveBalanceRepository.findByEmployeeIdAndLeaveYear(employee.getId(), year);
        if (existing.isPresent()) {
            LeaveBalance lb = existing.get();
            // Update quotas only if not finalized and not manually edited (to prevent overwriting manual adjustments)
            if (!"FINALIZED".equals(lb.getStatus()) && !lb.getIsManuallyEdited()) {
                lb.setAnnualLeaveQuota(annual);
                lb.setCasualLeaveQuota(casual);
                lb.setMedicalLeaveQuota(medical);
                leaveBalanceRepository.save(lb);
            }
        } else {
            LeaveBalance lb = LeaveBalance.builder()
                    .employee(employee)
                    .leaveYear(year)
                    .annualLeaveQuota(annual)
                    .casualLeaveQuota(casual)
                    .medicalLeaveQuota(medical)
                    .annualLeaveUsed(0)
                    .casualLeaveUsed(0)
                    .medicalLeaveUsed(0)
                    .status("CALCULATED")
                    .isManuallyEdited(false)
                    .build();
            leaveBalanceRepository.save(lb);
        }
    }

    @Override
    public List<LeaveBalance> getLeaveBalancesByYear(int year) {
        return leaveBalanceRepository.findByLeaveYear(year);
    }

    @Override
    public List<LeaveBalance> getLeaveBalancesByBranchAndYear(String branch, int year) {
        if (branch == null || branch.isEmpty() || "all".equalsIgnoreCase(branch)) {
            return leaveBalanceRepository.findByLeaveYear(year);
        }
        return leaveBalanceRepository.findByEmployeeBranchAndLeaveYear(branch, year);
    }

    @Override
    @Transactional
    public void finalizeLeaveBalancesForBranch(String branch, int year, Long finalizedById) {
        Employee verifier = employeeRepository.findById(finalizedById)
                .orElseThrow(() -> new RuntimeException("Finalizer employee not found"));

        List<LeaveBalance> balances;
        if (branch == null || branch.isEmpty() || "all".equalsIgnoreCase(branch)) {
            balances = leaveBalanceRepository.findByLeaveYear(year);
        } else {
            balances = leaveBalanceRepository.findByEmployeeBranchAndLeaveYear(branch, year);
        }

        for (LeaveBalance lb : balances) {
            lb.setStatus("FINALIZED");
            lb.setFinalizedBy(verifier);
            lb.setFinalizedAt(java.time.LocalDateTime.now());
            leaveBalanceRepository.save(lb);
        }
    }

    @Override
    @Transactional
    public LeaveBalance manuallyAdjustLeaveBalance(Long balanceId, int annual, int casual, int medical, Long editedById) {
        LeaveBalance lb = leaveBalanceRepository.findById(balanceId)
                .orElseThrow(() -> new RuntimeException("Leave balance not found"));

        Employee editor = employeeRepository.findById(editedById)
                .orElseThrow(() -> new RuntimeException("Editor employee not found"));

        lb.setAnnualLeaveQuota(annual);
        lb.setCasualLeaveQuota(casual);
        lb.setMedicalLeaveQuota(medical);
        lb.setIsManuallyEdited(true);
        lb.setLastEditedBy(editor);
        
        return leaveBalanceRepository.save(lb);
    }
}
