package com.hexaco.hrms.rest;

import com.hexaco.hrms.models.LeaveBalance;
import com.hexaco.hrms.models.NormalLeave;
import com.hexaco.hrms.repository.LeaveBalanceRepository;
import com.hexaco.hrms.repository.NormalLeaveRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leave-balance")
@RequiredArgsConstructor
public class LeaveBalanceController {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final NormalLeaveRepository normalLeaveRepository;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/employee/{employeeId}/year/{year}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'HR', 'DIRECTOR', 'SUPERVISOR')")
    public ResponseEntity<LeaveBalance> getLeaveBalanceByEmployeeAndYear(
            @PathVariable Long employeeId,
            @PathVariable Integer year) {
        
        return leaveBalanceRepository.findByEmployeeIdAndLeaveYear(employeeId, year)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sync-historical")
    public ResponseEntity<String> syncHistoricalLeaves() {
        int currentYear = java.time.LocalDate.now().getYear();

        // 1. Reset all used balances to 0 for current year via SQL
        jdbcTemplate.update(
            "UPDATE leave_balance SET annual_leave_used = 0, medical_leave_used = 0, casual_leave_used = 0 WHERE year = ?", 
            currentYear
        );

        // 2. Calculate and update based on approved normal leaves via SQL
        jdbcTemplate.update("""
            UPDATE leave_balance lb
            SET 
                annual_leave_used = COALESCE((
                    SELECT SUM(lr.total_days) FROM normal_leave nl
                    JOIN leave_request lr ON nl.id = lr.id
                    JOIN leave_type lt ON lr.leave_type_id = lt.id
                    WHERE lr.employee_id = lb.employee_id 
                    AND lr.status = 'APPROVED' 
                    AND EXTRACT(YEAR FROM lr.from_date) = ?
                    AND LOWER(lt.leave_type_name) LIKE '%annual%'
                ), 0),
                medical_leave_used = COALESCE((
                    SELECT SUM(lr.total_days) FROM normal_leave nl
                    JOIN leave_request lr ON nl.id = lr.id
                    JOIN leave_type lt ON lr.leave_type_id = lt.id
                    WHERE lr.employee_id = lb.employee_id 
                    AND lr.status = 'APPROVED' 
                    AND EXTRACT(YEAR FROM lr.from_date) = ?
                    AND (LOWER(lt.leave_type_name) LIKE '%medical%' OR LOWER(lt.leave_type_name) LIKE '%sick%')
                ), 0),
                casual_leave_used = COALESCE((
                    SELECT SUM(lr.total_days) FROM normal_leave nl
                    JOIN leave_request lr ON nl.id = lr.id
                    JOIN leave_type lt ON lr.leave_type_id = lt.id
                    WHERE lr.employee_id = lb.employee_id 
                    AND lr.status = 'APPROVED' 
                    AND EXTRACT(YEAR FROM lr.from_date) = ?
                    AND LOWER(lt.leave_type_name) LIKE '%casual%'
                ), 0)
            WHERE lb.year = ?
        """, currentYear, currentYear, currentYear, currentYear);
        
        return ResponseEntity.ok("Successfully synced historical leave balances for year " + currentYear);
    }
}
