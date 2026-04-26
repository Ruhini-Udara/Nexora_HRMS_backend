package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.WelfareRequestDto;
import com.hexaco.hrms.service.WelfareRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/welfare-requests")
@RequiredArgsConstructor
public class WelfareRequestController {

    private final WelfareRequestService welfareRequestService;

    @PostMapping
    public ResponseEntity<WelfareRequestDto> createRequest(@RequestBody WelfareRequestDto dto) {
        return ResponseEntity.ok(welfareRequestService.createRequest(dto));
    }

    @GetMapping
    public ResponseEntity<List<WelfareRequestDto>> getAllRequests() {
        return ResponseEntity.ok(welfareRequestService.getAllRequests());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<WelfareRequestDto>> getRequestsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(welfareRequestService.getRequestsByEmployee(employeeId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<WelfareRequestDto> updateStatus(@PathVariable Long id, @RequestParam String status, @RequestParam(required = false) String remarks) {
        return ResponseEntity.ok(welfareRequestService.updateStatus(id, status, remarks));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WelfareRequestDto> updateRequest(@PathVariable Long id, @RequestBody WelfareRequestDto dto) {
        return ResponseEntity.ok(welfareRequestService.updateRequest(id, dto));
    }
}
