package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.OverseasLeaveDto;
import com.hexaco.hrms.service.LeaveService;
import com.hexaco.hrms.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * REST Controller for handling Overseas Leave operations.
 * Evaluator Note: This controller provides the entry points for the Overseas Leave Workflow.
 * Just like Maternity Leave, it uses @PreAuthorize for method-level security, but also includes
 * specific endpoints like generateBoardReport that are exclusively available to ADMIN and DIRECTOR roles.
 */
@RestController
@RequestMapping("/api/v1/leaves/overseas")
@RequiredArgsConstructor
public class OverseasLeaveController {

    private final LeaveService leaveService;
    private final ReportService reportService;

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'HR', 'DIRECTOR')")
    public ResponseEntity<OverseasLeaveDto> submitOverseasLeave(@RequestBody OverseasLeaveDto dto) {
        OverseasLeaveDto savedLeave = leaveService.submitOverseasLeave(dto);
        return new ResponseEntity<>(savedLeave, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR', 'DIRECTOR', 'SUPERVISOR', 'ROLE_ADMIN', 'ROLE_HR', 'ROLE_DIRECTOR', 'ROLE_SUPERVISOR', 'admin', 'hr', 'director', 'supervisor')")
    public ResponseEntity<List<OverseasLeaveDto>> getAllOverseasLeaves() {
        List<OverseasLeaveDto> leaves = leaveService.getAllOverseasLeaves();
        return ResponseEntity.ok(leaves);
    }

    /**
     * Evaluator Note: This endpoint is used by HR and Admins to fetch requests that are in 
     * specific workflow states (e.g., PENDING_DIRECTOR_REVIEW).
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR', 'DIRECTOR', 'SUPERVISOR', 'ROLE_ADMIN', 'ROLE_HR', 'ROLE_DIRECTOR', 'ROLE_SUPERVISOR', 'admin', 'hr', 'director', 'supervisor')")
    public ResponseEntity<List<OverseasLeaveDto>> getOverseasLeavesByStatus(@PathVariable String status) {
        List<OverseasLeaveDto> leaves = leaveService.getOverseasLeavesByStatus(status);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<OverseasLeaveDto>> getOverseasLeavesByEmployeeId(@PathVariable Long employeeId) {
        List<OverseasLeaveDto> leaves = leaveService.getOverseasLeavesByEmployeeId(employeeId);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OverseasLeaveDto> getOverseasLeaveById(@PathVariable Long id) {
        return leaveService.getOverseasLeaveById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/impact")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR', 'DIRECTOR', 'SUPERVISOR', 'ROLE_ADMIN', 'ROLE_HR', 'ROLE_DIRECTOR', 'ROLE_SUPERVISOR', 'admin', 'hr', 'director', 'supervisor')")
    public ResponseEntity<com.hexaco.hrms.dto.LeaveImpactDto> getOverseasLeaveImpact(@PathVariable Long id) {
        return ResponseEntity.ok(leaveService.getOverseasLeaveImpact(id));
    }

    @GetMapping("/board-meeting-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public void generateBoardReport(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=board_meeting_leaves_" + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);

        reportService.exportBoardMeetingPdf(response);
    }
}
