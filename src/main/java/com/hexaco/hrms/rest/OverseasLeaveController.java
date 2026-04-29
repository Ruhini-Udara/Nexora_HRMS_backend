package com.hexaco.hrms.rest;

import com.hexaco.hrms.models.OverseasLeave;
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

@RestController
@RequestMapping("/api/v1/leaves/overseas")
@RequiredArgsConstructor
public class OverseasLeaveController {

    private final LeaveService leaveService;
    private final ReportService reportService;

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN', 'HR', 'DIRECTOR')")
    public ResponseEntity<OverseasLeave> submitOverseasLeave(@RequestBody OverseasLeave requestedLeave) {
        OverseasLeave savedLeave = leaveService.submitOverseasLeave(requestedLeave);
        return new ResponseEntity<>(savedLeave, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR', 'DIRECTOR', 'SUPERVISOR', 'ROLE_ADMIN', 'ROLE_HR', 'ROLE_DIRECTOR', 'ROLE_SUPERVISOR', 'admin', 'hr', 'director', 'supervisor')")
    public ResponseEntity<List<OverseasLeave>> getAllOverseasLeaves() {
        List<OverseasLeave> leaves = leaveService.getAllOverseasLeaves();
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR', 'DIRECTOR', 'SUPERVISOR', 'ROLE_ADMIN', 'ROLE_HR', 'ROLE_DIRECTOR', 'ROLE_SUPERVISOR', 'admin', 'hr', 'director', 'supervisor')")
    public ResponseEntity<List<OverseasLeave>> getOverseasLeavesByStatus(@PathVariable String status) {
        List<OverseasLeave> leaves = leaveService.getOverseasLeavesByStatus(status);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<OverseasLeave>> getOverseasLeavesByEmployeeId(@PathVariable Long employeeId) {
        List<OverseasLeave> leaves = leaveService.getOverseasLeavesByEmployeeId(employeeId);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OverseasLeave> getOverseasLeaveById(@PathVariable Long id) {
        return leaveService.getOverseasLeaveById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
