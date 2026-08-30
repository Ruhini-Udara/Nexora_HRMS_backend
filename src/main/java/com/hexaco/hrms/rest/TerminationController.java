package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.TerminationDto;
import com.hexaco.hrms.service.TerminationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/terminations")
@RequiredArgsConstructor
public class TerminationController {

    private final TerminationService terminationService;

    @PostMapping
    public ResponseEntity<TerminationDto> createTermination(@RequestBody TerminationDto dto) {
        return ResponseEntity.ok(terminationService.createTermination(dto));
    }

    @GetMapping
    public ResponseEntity<List<TerminationDto>> getAllTerminations() {
        return ResponseEntity.ok(terminationService.getAllTerminations());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<TerminationDto>> getTerminationsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(terminationService.getTerminationsByEmployeeId(employeeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TerminationDto> getTerminationById(@PathVariable Long id) {
        return ResponseEntity.ok(terminationService.getTerminationById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TerminationDto> updateTermination(@PathVariable Long id, @RequestBody TerminationDto dto) {
        return ResponseEntity.ok(terminationService.updateTermination(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TerminationDto> updateTerminationStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        
        String status = payload.get("status");
        String remarks = payload.get("remarks");
        String boardMeetingDate = payload.get("boardMeetingDate");

        return ResponseEntity.ok(terminationService.updateTerminationStatus(id, status, remarks, boardMeetingDate));
    }
}
