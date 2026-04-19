package com.hexaco.hrms.rest;

import com.hexaco.hrms.models.MaternityLeave;
import com.hexaco.hrms.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaves/maternity")
@RequiredArgsConstructor
public class MaternityLeaveController {

    private final LeaveService leaveService;

    @PostMapping
    public ResponseEntity<MaternityLeave> submitMaternityLeave(@RequestBody MaternityLeave requestedLeave) {
        MaternityLeave savedLeave = leaveService.submitMaternityLeave(requestedLeave);
        return new ResponseEntity<>(savedLeave, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MaternityLeave>> getAllMaternityLeaves() {
        List<MaternityLeave> leaves = leaveService.getAllMaternityLeaves();
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaternityLeave> getMaternityLeaveById(@PathVariable Long id) {
        return leaveService.getMaternityLeaveById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
