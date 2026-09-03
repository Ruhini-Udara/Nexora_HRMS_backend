package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.SupervisorReportAnalyticsDto;
import com.hexaco.hrms.service.SupervisorReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/supervisor/reports")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class SupervisorReportController {

    private final SupervisorReportService supervisorReportService;

    @GetMapping("/analytics")
    public ResponseEntity<SupervisorReportAnalyticsDto> getAnalytics(
            @RequestParam(value = "supervisorId", required = false) Long supervisorId,
            @RequestParam(value = "period", required = false, defaultValue = "30d") String period,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "branch", required = false) String branch
    ) {
        log.info("API request: GET /api/v1/supervisor/reports/analytics (supervisorId={}, period={}, dept={}, branch={})",
                supervisorId, period, department, branch);

        SupervisorReportAnalyticsDto analytics = supervisorReportService.getSupervisorReportAnalytics(
                supervisorId, period, department, branch
        );

        return ResponseEntity.ok(analytics);
    }
}
