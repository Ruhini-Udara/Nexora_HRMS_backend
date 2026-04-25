package com.hexaco.hrms.rest;

import com.hexaco.hrms.models.Designation;
import com.hexaco.hrms.repository.DesignationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/designations")
@CrossOrigin(origins = "http://localhost:3000")
public class DesignationController {

    private final DesignationRepository designationRepository;

    public DesignationController(DesignationRepository designationRepository) {
        this.designationRepository = designationRepository;
    }

    @GetMapping
    public ResponseEntity<List<Designation>> getAllDesignations() {
        return ResponseEntity.ok(designationRepository.findAll());
    }
}
