package com.hexaco.hrms.rest;

import com.hexaco.hrms.models.OverseasLeave;
import com.hexaco.hrms.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaves/overseas")
@RequiredArgsConstructor
public class OverseasLeaveController {

    private final LeaveService leaveService;

    @PostMapping
    public ResponseEntity<OverseasLeave> submitOverseasLeave(@RequestBody OverseasLeave requestedLeave) {
        OverseasLeave savedLeave = leaveService.submitOverseasLeave(requestedLeave);
        return new ResponseEntity<>(savedLeave, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OverseasLeave>> getAllOverseasLeaves() {
        List<OverseasLeave> leaves = leaveService.getAllOverseasLeaves();
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OverseasLeave> getOverseasLeaveById(@PathVariable Long id) {
        return leaveService.getOverseasLeaveById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
