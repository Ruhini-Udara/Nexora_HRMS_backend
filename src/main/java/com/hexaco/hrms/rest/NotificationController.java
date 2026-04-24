package com.hexaco.hrms.rest;

import com.hexaco.hrms.service.NotificationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/handover")
    public ResponseEntity<String> sendHandoverNotifications(@RequestBody HandoverRequest request) {
        for (HandoverTask task : request.getTasks()) {
            notificationService.sendLeaveStatusUpdate(
                task.getColleagueName(),
                task.getColleagueEmail(),
                null, // No phone number for now
                "Handover Task: " + task.getTaskTitle(),
                "ASSIGNED",
                "Assigned to you by " + request.getEmployeeName()
            );
        }
        return ResponseEntity.ok("Notifications sent successfully");
    }

    @Data
    public static class HandoverRequest {
        private String employeeName;
        private List<HandoverTask> tasks;
    }

    @Data
    public static class HandoverTask {
        private String taskTitle;
        private String colleagueName;
        private String colleagueEmail;
    }
}
