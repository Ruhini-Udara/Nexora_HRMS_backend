package com.hexaco.hrms.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @GetMapping("/employee/{id}")
    public ResponseEntity<Map<String, Object>> getEmployeeDashboard(@PathVariable Long id) {
        Map<String, Object> mockDashboard = new HashMap<>();
        // Provide mock statistics to prevent frontend crashes
        mockDashboard.put("attendance", 100);
        mockDashboard.put("leaves", 0);
        return ResponseEntity.ok(mockDashboard);
    }
}
