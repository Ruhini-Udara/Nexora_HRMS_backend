package com.hexaco.hrms.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Collections;

@RestController
@RequestMapping("/api/system-notifications")
@CrossOrigin(origins = "*")
public class SystemNotificationController {

    @GetMapping("/all")
    public ResponseEntity<List<Object>> getAllNotifications() {
        return ResponseEntity.ok(Collections.emptyList());
    }
}
