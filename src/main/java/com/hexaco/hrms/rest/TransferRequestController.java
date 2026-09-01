package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.TransferRequestDto;
import com.hexaco.hrms.service.TransferRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfer-requests")
@RequiredArgsConstructor
public class TransferRequestController {

    private final TransferRequestService transferRequestService;

    @PostMapping
    public ResponseEntity<TransferRequestDto> createRequest(@RequestBody TransferRequestDto dto) {
        return ResponseEntity.ok(transferRequestService.createRequest(dto));
    }

    @GetMapping
    public ResponseEntity<List<TransferRequestDto>> getAllRequests() {
        return ResponseEntity.ok(transferRequestService.getAllRequests());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<TransferRequestDto>> getRequestsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(transferRequestService.getRequestsByEmployee(employeeId));
    }

    @RequestMapping(value = "/{id}/status", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<TransferRequestDto> updateStatus(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, String> payload,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String remarks,
            @RequestParam(required = false) String boardMeetingDate) {
        
        String finalStatus = (payload != null && payload.containsKey("status")) ? payload.get("status") : status;
        String finalRemarks = (payload != null && payload.containsKey("remarks")) ? payload.get("remarks") : remarks;
        String finalBoardDate = (payload != null && payload.containsKey("boardMeetingDate")) ? payload.get("boardMeetingDate") : boardMeetingDate;

        return ResponseEntity.ok(transferRequestService.updateStatus(id, finalStatus, finalRemarks, finalBoardDate));
    }



    @PutMapping("/{id}")
    public ResponseEntity<TransferRequestDto> updateRequest(@PathVariable Long id, @RequestBody TransferRequestDto dto) {
        return ResponseEntity.ok(transferRequestService.updateRequest(id, dto));
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<TransferRequestDto> executeTransfer(@PathVariable Long id) {
        return ResponseEntity.ok(transferRequestService.executeTransfer(id));
    }
}
