package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.NomineeDto;
import com.hexaco.hrms.service.NomineeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nominees")
@RequiredArgsConstructor
public class NomineeController {

    private final NomineeService service;

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<NomineeDto> getNominee(@PathVariable Long employeeId) {
        try {
            return ResponseEntity.ok(service.getNomineeByEmployeeId(employeeId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<NomineeDto> saveNominee(@RequestBody NomineeDto dto) {
        return ResponseEntity.ok(service.saveOrUpdateNominee(dto));
    }
}
