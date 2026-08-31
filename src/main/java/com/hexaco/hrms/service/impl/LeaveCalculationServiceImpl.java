package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.LeaveBalance;
import com.hexaco.hrms.models.LeaveBalanceAdjustment;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.repository.LeaveBalanceRepository;
import com.hexaco.hrms.repository.LeaveBalanceAdjustmentRepository;
import com.hexaco.hrms.rest.LeaveAdjustmentRequest;
import com.hexaco.hrms.rest.LeaveImportRequest;
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
    private final LeaveBalanceAdjustmentRepository adjustmentRepository;
    private final com.hexaco.hrms.service.LeaveBalanceService leaveBalanceService;

    public LeaveCalculationServiceImpl(
            EmployeeRepository employeeRepository, 
            LeaveBalanceRepository leaveBalanceRepository, 
            LeaveBalanceAdjustmentRepository adjustmentRepository,
            com.hexaco.hrms.service.LeaveBalanceService leaveBalanceService) {
        this.employeeRepository = employeeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.adjustmentRepository = adjustmentRepository;
        this.leaveBalanceService = leaveBalanceService;
    }

    @Override
    @Transactional
    public void calculateLeaveForYear(int year) {
        List<Employee> employees = employeeRepository.findAll();

        for (Employee employee : employees) {
            leaveBalanceService.calculateLeave(employee.getId(), year);
            // The LeaveBalanceService creates them. We want to ensure calculationSource is AUTOMATIC.
            // Assuming LeaveBalanceService uses AUTOMATIC or we can just fetch and update.
            Optional<LeaveBalance> lbOpt = leaveBalanceRepository.findByEmployeeIdAndLeaveYear(employee.getId(), year);
            if (lbOpt.isPresent()) {
                LeaveBalance lb = lbOpt.get();
                if (lb.getCalculationSource() == null) {
                    lb.setCalculationSource("AUTOMATIC");
                    leaveBalanceRepository.save(lb);
                }
            }
        }
    }

    @Override
    public List<LeaveBalance> getLeaveBalancesByYear(int year) {
        return leaveBalanceRepository.findByLeaveYear(year).stream()
                .filter(lb -> lb.getEmployee() != null)
                .toList();
    }

    @Override
    public List<LeaveBalance> getLeaveBalancesByBranchAndYear(String branch, int year) {
        List<LeaveBalance> balances;
        if (branch == null || branch.isEmpty() || "all".equalsIgnoreCase(branch)) {
            balances = leaveBalanceRepository.findByLeaveYear(year);
        } else {
            balances = leaveBalanceRepository.findByEmployeeBranchAndLeaveYear(branch, year);
        }
        return balances.stream()
                .filter(lb -> lb.getEmployee() != null)
                .toList();
    }

    @Override
    @Transactional
    public void finalizeLeaveBalancesForBranch(String branch, int year, Long finalizedById) {
        Employee verifier = employeeRepository.findById(finalizedById)
                .orElseThrow(() -> new RuntimeException("Finalizer employee not found"));

        List<LeaveBalance> balances;
        List<Employee> branchEmployees;
        
        if (branch == null || branch.isEmpty() || "all".equalsIgnoreCase(branch)) {
            balances = leaveBalanceRepository.findByLeaveYear(year);
            branchEmployees = employeeRepository.findAll();
        } else {
            balances = leaveBalanceRepository.findByEmployeeBranchAndLeaveYear(branch, year);
            branchEmployees = employeeRepository.findByBranch(branch);
        }

        // Validate that all required employee leave balances for the selected year and district/branch are complete.
        if (balances.size() < branchEmployees.size()) {
             throw new RuntimeException("Cannot finalize. Missing leave balances for some employees in this branch.");
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
    public LeaveBalance manuallyAdjustLeaveBalance(LeaveAdjustmentRequest request) {
        LeaveBalance lb = leaveBalanceRepository.findById(request.getBalanceId())
                .orElseThrow(() -> new RuntimeException("Leave balance not found"));

        if ("FINALIZED".equals(lb.getStatus())) {
            throw new RuntimeException("Cannot adjust a finalized leave balance.");
        }

        Employee editor = employeeRepository.findById(request.getAdjustedById())
                .orElseThrow(() -> new RuntimeException("Editor employee not found"));

        Integer oldBalance = 0;
        
        if ("ANNUAL".equalsIgnoreCase(request.getLeaveType())) {
            oldBalance = lb.getAnnualLeaveQuota();
            lb.setAnnualLeaveQuota(request.getNewBalance());
        } else if ("CASUAL".equalsIgnoreCase(request.getLeaveType())) {
            oldBalance = lb.getCasualLeaveQuota();
            lb.setCasualLeaveQuota(request.getNewBalance());
        } else if ("MEDICAL".equalsIgnoreCase(request.getLeaveType())) {
            oldBalance = lb.getMedicalLeaveQuota();
            lb.setMedicalLeaveQuota(request.getNewBalance());
        } else {
            throw new RuntimeException("Invalid leave type");
        }

        lb.setIsManuallyEdited(true);
        lb.setLastEditedBy(editor);
        lb.setCalculationSource("MANUAL_ADJUSTMENT");
        
        LeaveBalance savedLb = leaveBalanceRepository.save(lb);

        LeaveBalanceAdjustment audit = LeaveBalanceAdjustment.builder()
                .leaveBalance(savedLb)
                .leaveType(request.getLeaveType().toUpperCase())
                .oldBalance(oldBalance)
                .newBalance(request.getNewBalance())
                .reason(request.getReason())
                .adjustedBy(editor)
                .adjustedAt(java.time.LocalDateTime.now())
                .build();
        adjustmentRepository.save(audit);

        return savedLb;
    }
    
    @Override
    @Transactional
    public void importHistoricalBalances(List<LeaveImportRequest> requests) {
        for (LeaveImportRequest req : requests) {
            Employee emp = employeeRepository.findByEmployeeCode(req.getEmployeeCode())
                    .orElseThrow(() -> new RuntimeException("Employee not found for Code: " + req.getEmployeeCode()));
                    
            Optional<LeaveBalance> existingOpt = leaveBalanceRepository.findByEmployeeIdAndLeaveYear(emp.getId(), req.getYear());
            LeaveBalance lb;
            
            if (existingOpt.isPresent()) {
                lb = existingOpt.get();
                if ("FINALIZED".equals(lb.getStatus())) {
                    continue; // Skip finalized records
                }
            } else {
                lb = new LeaveBalance();
                lb.setEmployee(emp);
                lb.setLeaveYear(req.getYear());
                lb.setStatus("CALCULATED");
            }
            
            lb.setAnnualLeaveQuota(req.getAnnualLeaveQuota() != null ? req.getAnnualLeaveQuota() : 0);
            lb.setCasualLeaveQuota(req.getCasualLeaveQuota() != null ? req.getCasualLeaveQuota() : 0);
            lb.setMedicalLeaveQuota(req.getMedicalLeaveQuota() != null ? req.getMedicalLeaveQuota() : 0);
            
            lb.setAnnualLeaveUsed(req.getAnnualLeaveUsed() != null ? req.getAnnualLeaveUsed() : 0);
            lb.setCasualLeaveUsed(req.getCasualLeaveUsed() != null ? req.getCasualLeaveUsed() : 0);
            lb.setMedicalLeaveUsed(req.getMedicalLeaveUsed() != null ? req.getMedicalLeaveUsed() : 0);
            
            lb.setCalculationSource("HISTORICAL_IMPORT");
            lb.setIsManuallyEdited(false);
            
            leaveBalanceRepository.save(lb);
        }
    }
}
