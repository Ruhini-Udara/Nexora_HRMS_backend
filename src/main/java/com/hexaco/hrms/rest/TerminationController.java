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

    @RequestMapping(value = "/{id}/status", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<TerminationDto> updateTerminationStatus(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> payload,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String remarks,
            @RequestParam(required = false) String boardMeetingDate) {
        
        String finalStatus = (payload != null && payload.containsKey("status")) ? payload.get("status") : status;
        String finalRemarks = (payload != null && payload.containsKey("remarks")) ? payload.get("remarks") : remarks;
        String finalBoardDate = (payload != null && payload.containsKey("boardMeetingDate")) ? payload.get("boardMeetingDate") : boardMeetingDate;

        return ResponseEntity.ok(terminationService.updateTerminationStatus(id, finalStatus, finalRemarks, finalBoardDate));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TerminationDto> updateTermination(@PathVariable Long id, @RequestBody TerminationDto dto) {
        return ResponseEntity.ok(terminationService.updateTermination(id, dto));
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<Void> executeTermination(@PathVariable Long id) {
        terminationService.executeTermination(id);
        return ResponseEntity.ok().build();
    }
}
