package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.ResignationDto;
import com.hexaco.hrms.service.ResignationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resignations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ResignationController {

    private final ResignationService resignationService;

    @PostMapping
    public ResponseEntity<ResignationDto> createResignation(@RequestBody ResignationDto resignationDto) {
        return ResponseEntity.ok(resignationService.createResignation(resignationDto));
    }

    @GetMapping
    public ResponseEntity<List<ResignationDto>> getAllResignations() {
        return ResponseEntity.ok(resignationService.getAllResignations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResignationDto> getResignationById(@PathVariable Long id) {
        return ResponseEntity.ok(resignationService.getResignationById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ResignationDto>> getResignationsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(resignationService.getResignationsByEmployeeId(employeeId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ResignationDto> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String remarks,
            @RequestParam(required = false) String boardMeetingDate) {
        return ResponseEntity.ok(resignationService.updateResignationStatus(id, status, remarks, boardMeetingDate));
    }
}
