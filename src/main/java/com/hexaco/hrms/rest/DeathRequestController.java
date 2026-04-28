package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.DeathRequestDto;
import com.hexaco.hrms.service.DeathRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/death-requests")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class DeathRequestController {

    private final DeathRequestService service;

    @GetMapping
    public ResponseEntity<List<DeathRequestDto>> getAllRequests() {
        return ResponseEntity.ok(service.getAllRequests());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<DeathRequestDto>> getRequestsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(service.getRequestsByEmployee(employeeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeathRequestDto> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getRequestById(id));
    }

    @PostMapping
    public ResponseEntity<DeathRequestDto> createRequest(@RequestBody DeathRequestDto dto) {
        return new ResponseEntity<>(service.createRequest(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeathRequestDto> updateRequest(@PathVariable Long id, @RequestBody DeathRequestDto dto) {
        return ResponseEntity.ok(service.updateRequest(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        service.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<DeathRequestDto> verifyDeathRequest(@PathVariable Long id) {
        DeathRequestDto result = service.verifyRequest(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<DeathRequestDto> rejectDeathRequest(@PathVariable Long id, @RequestBody String reason) {
        DeathRequestDto result = service.rejectRequest(id, reason);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/submit-admin")
    public ResponseEntity<DeathRequestDto> submitToAdmin(@PathVariable Long id) {
        DeathRequestDto result = service.submitToAdmin(id);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<DeathRequestDto> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String boardMeetingDate) {
        return ResponseEntity.ok(service.updateStatus(id, status, boardMeetingDate));
    }
}
