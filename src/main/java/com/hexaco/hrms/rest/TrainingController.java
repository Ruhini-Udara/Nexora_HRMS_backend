package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.TrainingEventDto;
import com.hexaco.hrms.dto.TrainingFeedbackDto;
import com.hexaco.hrms.dto.TrainingRequestDto;
import com.hexaco.hrms.service.TrainingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/training")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

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

    // API endpoint to get all training events
    @GetMapping("/events")
    public ResponseEntity<List<TrainingEventDto>> getAllTrainingEvents() {
        return ResponseEntity.ok(trainingService.getAllTrainingEvents());
    }

    // API endpoint to get a specific training event by ID
    @GetMapping("/events/{id}")
    public ResponseEntity<TrainingEventDto> getTrainingEventById(@PathVariable Long id) {
        return ResponseEntity.ok(trainingService.getTrainingEventById(id));
    }

    // API endpoint to update a training event
    @PutMapping("/events/{id}")
    public ResponseEntity<?> updateTrainingEvent(
            @PathVariable Long id,
            @RequestBody TrainingEventDto dto) {
        try {
            TrainingEventDto updated = trainingService.updateTrainingEvent(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // API endpoint to update event status
    @PutMapping("/events/{id}/status")
    public ResponseEntity<TrainingEventDto> updateEventStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        String status = (String) payload.get("status");
        String reason = (String) payload.get("reason");
        String approvedBy = (String) payload.get("approvedBy");
        String dateSubmitted = (String) payload.get("dateSubmitted");
        
        TrainingEventDto event = trainingService.getTrainingEventById(id);
        event.setStatus(status);
        event.setReason(reason);
        event.setApprovedBy(approvedBy);
        if (dateSubmitted != null) {
            event.setDateSubmitted(dateSubmitted);
        }
        
        return ResponseEntity.ok(trainingService.updateTrainingEvent(id, event));
    }

    // API endpoint to delete or cancel a training event
    @DeleteMapping("/events/{id}")
    public ResponseEntity<?> deleteTrainingEvent(
            @PathVariable Long id,
            @RequestParam(required = false) Long cancelledById) {
        try {
            trainingService.deleteTrainingEvent(id, cancelledById);
            return ResponseEntity.ok(Map.of("message", "Training event successfully processed."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // --- Training Requests ---

    // API endpoint to apply for training
    @PostMapping("/requests")
    public ResponseEntity<TrainingRequestDto> applyForTraining(@RequestBody TrainingRequestDto dto) {
        TrainingRequestDto created = trainingService.applyForTraining(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // API endpoint to get training requests by event
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

    // API endpoint to get training requests by employee
    @GetMapping("/employees/{employeeId}/requests")
    public ResponseEntity<List<TrainingRequestDto>> getRequestsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(trainingService.getRequestsByEmployee(employeeId));
    }

    // API endpoint to update training request status
    @PutMapping("/requests/{id}/status")
    public ResponseEntity<TrainingRequestDto> updateRequestStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        System.out.println("[DEBUG] Received status update for training request: " + id + " | Payload: " + payload);
        String status = (String) payload.get("status");
        String rejectionReason = (String) payload.get("rejectionReason");
        Long approverId = payload.get("approverId") != null ? Long.valueOf(payload.get("approverId").toString()) : null;
        return ResponseEntity.ok(trainingService.updateRequestStatus(id, status, rejectionReason, approverId));
    }

    // --- Training Feedback ---

    // API endpoint to submit feedback
    @PostMapping("/feedback")
    public ResponseEntity<TrainingFeedbackDto> submitFeedback(@RequestBody TrainingFeedbackDto dto) {
        TrainingFeedbackDto created = trainingService.submitFeedback(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // API endpoint to check event existence
    @GetMapping("/events/exists")
    public ResponseEntity<Boolean> checkEventExistence(
            @RequestParam String title) {
        try {
            return ResponseEntity.ok(trainingService.existsByTitle(title.trim()));
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    // API endpoint to check training code existence
    @GetMapping("/events/exists-code")
    public ResponseEntity<Boolean> checkEventCodeExistence(
            @RequestParam String code,
            @RequestParam(required = false) Long excludeId) {
        try {
            return ResponseEntity.ok(trainingService.existsByTrainingCode(code.trim(), excludeId));
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    // API endpoint to get feedback by event
    @GetMapping("/events/{eventId}/feedback")
    public ResponseEntity<List<TrainingFeedbackDto>> getFeedbackByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(trainingService.getFeedbackByEvent(eventId));
    }
}
