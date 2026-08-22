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
import com.hexaco.hrms.service.EmployeeDashboardService;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final EmployeeDashboardService employeeDashboardService;

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'HR')")
    public ResponseEntity<EmployeeDashboardDto> getEmployeeDashboard(@PathVariable Long employeeId) {
        return ResponseEntity.ok(employeeDashboardService.getEmployeeDashboard(employeeId));
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'DIRECTOR')")
    public ResponseEntity<DashboardAnalyticsDto> getAnalytics() {
        return ResponseEntity.ok(dashboardService.getAnalytics());
    }
}
