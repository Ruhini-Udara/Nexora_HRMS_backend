package com.hexaco.hrms.rest;

import com.hexaco.hrms.models.LeaveType;
import com.hexaco.hrms.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leave-types")
@RequiredArgsConstructor
public class LeaveTypeController {

    private final LeaveTypeRepository leaveTypeRepository;

    @GetMapping
    public ResponseEntity<List<LeaveType>> getAllLeaveTypes() {
        return ResponseEntity.ok(leaveTypeRepository.findAll());
    }

    @GetMapping("/public")
    public ResponseEntity<List<LeaveType>> getPublicLeaveTypes() {
        return ResponseEntity.ok(leaveTypeRepository.findAll());
    }
}
