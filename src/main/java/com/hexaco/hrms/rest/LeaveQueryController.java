package com.hexaco.hrms.rest;

import com.hexaco.hrms.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
public class LeaveQueryController {

    private final LeaveService leaveService;

    @GetMapping("/daily-approved")
    public ResponseEntity<List<Long>> getEmployeesOnLeave(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(leaveService.getEmployeesOnLeave(date));
    }
}
