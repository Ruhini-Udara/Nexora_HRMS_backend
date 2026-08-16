package com.hexaco.hrms.rest;

import com.hexaco.hrms.models.Designation;
import com.hexaco.hrms.models.Shift;
import com.hexaco.hrms.repository.DesignationRepository;
import com.hexaco.hrms.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/designations")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationRepository designationRepository;
    private final ShiftRepository shiftRepository;

    @GetMapping
    public ResponseEntity<List<Designation>> getAllDesignations() {
        return ResponseEntity.ok(designationRepository.findAll());
    }

    @PutMapping("/{id}/shift/{shiftId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Designation> assignShiftToDesignation(
            @PathVariable Long id,
            @PathVariable Long shiftId) {
        Designation designation = designationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Designation not found with id: " + id));
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found with id: " + shiftId));
        designation.setShift(shift);
        return ResponseEntity.ok(designationRepository.save(designation));
    }

    @PutMapping("/{id}/shift")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Designation> clearShiftFromDesignation(
            @PathVariable Long id) {
        Designation designation = designationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Designation not found with id: " + id));
        designation.setShift(null);
        return ResponseEntity.ok(designationRepository.save(designation));
    }
}
