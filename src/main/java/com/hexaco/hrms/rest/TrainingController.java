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

    // --- Training Events ---

    @PostMapping("/events")
    public ResponseEntity<TrainingEventDto> createTrainingEvent(@RequestBody TrainingEventDto dto) {
        TrainingEventDto created = trainingService.createTrainingEvent(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
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

    // --- Training Requests ---

    @PostMapping("/requests")
    public ResponseEntity<TrainingRequestDto> applyForTraining(@RequestBody TrainingRequestDto dto) {
        TrainingRequestDto created = trainingService.applyForTraining(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/events/{eventId}/requests")
    public ResponseEntity<List<TrainingRequestDto>> getRequestsByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(trainingService.getRequestsByEvent(eventId));
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

    @GetMapping("/events/{eventId}/feedback")
    public ResponseEntity<List<TrainingFeedbackDto>> getFeedbackByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(trainingService.getFeedbackByEvent(eventId));
    }
}
