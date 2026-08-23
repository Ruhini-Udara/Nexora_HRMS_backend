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

    public LeaveCalculationServiceImpl(EmployeeRepository employeeRepository, LeaveBalanceRepository leaveBalanceRepository) {
        this.employeeRepository = employeeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
    }

    @Override
    @Transactional
    public void calculateLeaveForYear(int year) {
        List<Employee> employees = employeeRepository.findAll();
        LocalDate cutOffDate = LocalDate.of(2011, 7, 2);

        for (Employee employee : employees) {
            LocalDate joinDate = employee.getDateJoined();
            if (joinDate == null) {
                // If no join date, default to a safe standard (pre-2011: 21 leaves)
                saveOrUpdateLeaveBalance(employee, year, 7, 0, 14);
                continue;
            }

            int annual = 0;
            int casual = 0;
            int medical = 0;

            // Check if it is the First Year of employment
            if (joinDate.getYear() == year) {
                // Prorated first year logic
                LocalDate april1st = LocalDate.of(year, 4, 1);
                LocalDate july1st = LocalDate.of(year, 7, 1);
                LocalDate october1st = LocalDate.of(year, 10, 1);

                if (!joinDate.isAfter(april1st)) {
                    // <= April 1st: 14 leaves (Medical 6, Casual 2, Annual 6)
                    annual = 6;
                    casual = 2;
                    medical = 6;
                } else if (!joinDate.isAfter(july1st)) {
                    // <= July 1st: 10 leaves (Medical 4, Casual 2, Annual 4)
                    annual = 4;
                    casual = 2;
                    medical = 4;
                } else if (!joinDate.isAfter(october1st)) {
                    // <= October 1st: 7 leaves (Medical 3, Casual 1, Annual 3)
                    annual = 3;
                    casual = 1;
                    medical = 3;
                } else {
                    // > October 1st: 4 leaves (Medical 2, Casual 0, Annual 2)
                    annual = 2;
                    casual = 0;
                    medical = 2;
                }
            } else {
                // Regular permanent employees
                if (joinDate.isAfter(cutOffDate)) {
                    // > 02/07/2011: 35 leaves (Medical 14, Casual 7, Annual 14)
                    annual = 14;
                    casual = 7;
                    medical = 14;
                } else {
                    // <= 02/07/2011: 21 leaves (Medical 14, Annual 7, Casual 0)
                    annual = 7;
                    casual = 0;
                    medical = 14;
                }
            }

            saveOrUpdateLeaveBalance(employee, year, annual, casual, medical);
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
