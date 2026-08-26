package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.NotificationDto;
import com.hexaco.hrms.service.SystemNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system-notifications")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SystemNotificationController {

    private final SystemNotificationService systemNotificationService;

    // TODO: Ideally employeeId should be extracted from the authenticated user's JWT
    // For simplicity, passing it as a request param
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(@RequestParam Long employeeId) {
        return ResponseEntity.ok(systemNotificationService.getUnreadNotifications(employeeId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<NotificationDto>> getAllNotifications(@RequestParam Long employeeId) {
        return ResponseEntity.ok(systemNotificationService.getAllNotifications(employeeId));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getUnreadCount(@RequestParam Long employeeId) {
        return ResponseEntity.ok(systemNotificationService.getUnreadCount(employeeId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        systemNotificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}
