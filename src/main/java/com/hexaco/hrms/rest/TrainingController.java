package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.TrainingEventDto;
import com.hexaco.hrms.dto.TrainingFeedbackDto;
import com.hexaco.hrms.dto.TrainingRequestDto;
import com.hexaco.hrms.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/training")
@CrossOrigin(origins = "http://localhost:3000") // security configuration
public class TrainingController {

    @Autowired
    private TrainingService trainingService;

    @GetMapping("/debug")
    public ResponseEntity<String> debug() {
        return ResponseEntity.ok("Debug OK");
    }

    // --- Training Events ---

    @PostMapping("/events")
    public ResponseEntity<?> createTrainingEvent(@RequestBody TrainingEventDto dto) {
        try {
            TrainingEventDto created = trainingService.createTrainingEvent(dto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/events")
    public ResponseEntity<List<TrainingEventDto>> getAllTrainingEvents() {
        return ResponseEntity.ok(trainingService.getAllTrainingEvents());
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<TrainingEventDto> getTrainingEventById(@PathVariable Long id) {
        return ResponseEntity.ok(trainingService.getTrainingEventById(id));
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<TrainingEventDto> updateTrainingEvent(
            @PathVariable Long id,
            @RequestBody TrainingEventDto dto) {
        return ResponseEntity.ok(trainingService.updateTrainingEvent(id, dto));
    }

    @PutMapping("/events/{id}/status")
    public ResponseEntity<TrainingEventDto> updateEventStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        TrainingEventDto event = trainingService.getTrainingEventById(id);
        event.setStatus(status);
        return ResponseEntity.ok(trainingService.updateTrainingEvent(id, event));
    }

    // --- Training Requests ---

    @PostMapping("/requests")
    public ResponseEntity<TrainingRequestDto> applyForTraining(@RequestBody TrainingRequestDto dto) {
        TrainingRequestDto created = trainingService.applyForTraining(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/events/{eventId}/requests")
    public ResponseEntity<?> getRequestsByEvent(@PathVariable Long eventId) {
        try {
            System.out.println("Fetching requests for event: " + eventId);
            List<TrainingRequestDto> requests = trainingService.getRequestsByEvent(eventId);
            System.out.println("Found " + requests.size() + " requests.");
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            System.err.println("ERROR FETCHING EVENT REQUESTS: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/employees/{employeeId}/requests")
    public ResponseEntity<List<TrainingRequestDto>> getRequestsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(trainingService.getRequestsByEmployee(employeeId));
    }

    @PutMapping("/requests/{id}/status")
    public ResponseEntity<TrainingRequestDto> updateRequestStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        String rejectionReason = payload.get("rejectionReason");
        return ResponseEntity.ok(trainingService.updateRequestStatus(id, status, rejectionReason));
    }

    // --- Training Feedback ---

    @PostMapping("/feedback")
    public ResponseEntity<TrainingFeedbackDto> submitFeedback(@RequestBody TrainingFeedbackDto dto) {
        TrainingFeedbackDto created = trainingService.submitFeedback(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/events/exists")
    public ResponseEntity<Boolean> checkEventExistence(
            @RequestParam String title) {
        try {
            return ResponseEntity.ok(trainingService.existsByTitle(title.trim()));
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/events/{eventId}/feedback")
    public ResponseEntity<List<TrainingFeedbackDto>> getFeedbackByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(trainingService.getFeedbackByEvent(eventId));
    }
}
