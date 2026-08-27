package com.hexaco.hrms.config;

import com.hexaco.hrms.models.LeavePolicy;
import com.hexaco.hrms.models.LeaveType;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.repository.LeavePolicyRepository;
import com.hexaco.hrms.repository.LeaveTypeRepository;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.service.LeaveBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class LeavePolicySeeder implements CommandLineRunner {

    private final LeavePolicyRepository leavePolicyRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceService leaveBalanceService;

    @Override
    public void run(String... args) throws Exception {
        // Find Leave Types
        LeaveType annualLeave = null;
        LeaveType casualLeave = null;
        LeaveType medicalLeave = null;

        for (LeaveType lt : leaveTypeRepository.findAll()) {
            String name = lt.getLeaveTypeName().toLowerCase();
            if (name.contains("annual")) annualLeave = lt;
            if (name.contains("casual")) casualLeave = lt;
            if (name.contains("medical") || name.contains("sick")) medicalLeave = lt;
        }

        if (annualLeave == null || casualLeave == null || medicalLeave == null) {
            System.out.println("Leave types not fully seeded, skipping LeavePolicySeeder.");
            return;
        }

        if (leavePolicyRepository.count() == 0) {
            System.out.println("Seeding Leave Policies...");
            
            // Full Time
            addPolicy("FULL_TIME", annualLeave, 14);
            addPolicy("FULL_TIME", casualLeave, 7);
            addPolicy("FULL_TIME", medicalLeave, 14);
            
            // Part Time
            addPolicy("PART_TIME", annualLeave, 7);
            addPolicy("PART_TIME", casualLeave, 4);
            addPolicy("PART_TIME", medicalLeave, 7);
            
            // Temporary
            addPolicy("TEMPORARY", annualLeave, 7);
            addPolicy("TEMPORARY", casualLeave, 4);
            addPolicy("TEMPORARY", medicalLeave, 7);
            
            // Probation
            addPolicy("PROBATION", annualLeave, 14);
            addPolicy("PROBATION", casualLeave, 7);
            addPolicy("PROBATION", medicalLeave, 14);
            
            // Standardizing existing employees that don't have types set properly
            addPolicy("Full Time", annualLeave, 14);
            addPolicy("Full Time", casualLeave, 7);
            addPolicy("Full Time", medicalLeave, 14);
        }

        // Also re-calculate leave balances for current year for all employees
        // so the frontend reflects the new quotas
        int currentYear = LocalDate.now().getYear();
        List<Employee> employees = employeeRepository.findAll();
        for (Employee emp : employees) {
            try {
                leaveBalanceService.calculateLeave(emp.getId(), currentYear);
            } catch (Exception e) {
                System.out.println("Failed to calculate leave for employee " + emp.getId() + ": " + e.getMessage());
            }
        }
    }

    private void addPolicy(String employeeType, LeaveType leaveType, int entitledDays) {
        LeavePolicy policy = LeavePolicy.builder()
                .employeeType(employeeType)
                .leaveType(leaveType)
                .entitledDays(entitledDays)
                .isActive(true)
                .build();
        leavePolicyRepository.save(policy);
    }
}
