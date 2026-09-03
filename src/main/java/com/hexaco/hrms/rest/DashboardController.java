package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.DashboardAnalyticsDto;
import com.hexaco.hrms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.hexaco.hrms.dto.EmployeeDashboardDto;
import com.hexaco.hrms.dto.EmployeeLeaveOverviewDto;
import com.hexaco.hrms.dto.DirectorDashboardDto;
import com.hexaco.hrms.dto.RecentRequestItemDto;
import com.hexaco.hrms.models.UserAccount;
import com.hexaco.hrms.repository.UserAccountRepository;
import com.hexaco.hrms.service.EmployeeDashboardService;
import com.hexaco.hrms.service.DirectorDashboardService;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final EmployeeDashboardService employeeDashboardService;
    private final DirectorDashboardService directorDashboardService;
    private final UserAccountRepository userAccountRepository;

    @GetMapping("/employee/leave-overview")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'HR')")
    public ResponseEntity<EmployeeLeaveOverviewDto> getAuthenticatedEmployeeLeaveOverview(Authentication authentication) {
        String email = authentication.getName();
        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for authenticated session"));
        if (userAccount.getEmployee() == null) {
            throw new RuntimeException("No employee record associated with user account");
        }
        Long employeeId = userAccount.getEmployee().getId();
        return ResponseEntity.ok(employeeDashboardService.getEmployeeLeaveOverview(employeeId));
    }

    @GetMapping("/employee/{employeeId}/leave-overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    public ResponseEntity<EmployeeLeaveOverviewDto> getEmployeeLeaveOverview(@PathVariable Long employeeId) {
        return ResponseEntity.ok(employeeDashboardService.getEmployeeLeaveOverview(employeeId));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'HR')")
    public ResponseEntity<EmployeeDashboardDto> getEmployeeDashboard(@PathVariable Long employeeId) {
        return ResponseEntity.ok(employeeDashboardService.getEmployeeDashboard(employeeId));
    }

    @GetMapping("/employee/{employeeId}/requests")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'HR')")
    public ResponseEntity<List<RecentRequestItemDto>> getAllEmployeeRequests(@PathVariable Long employeeId) {
        return ResponseEntity.ok(employeeDashboardService.getAllEmployeeRequests(employeeId));
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'DIRECTOR')")
    public ResponseEntity<DashboardAnalyticsDto> getAnalytics() {
        return ResponseEntity.ok(dashboardService.getAnalytics());
    }

    @GetMapping("/director")
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<DirectorDashboardDto> getDirectorDashboard() {
        return ResponseEntity.ok(directorDashboardService.getDirectorDashboard());
    }
}
