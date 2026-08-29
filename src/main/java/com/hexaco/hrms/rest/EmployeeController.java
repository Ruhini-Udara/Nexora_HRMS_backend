package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.EmployeeDTO;
import com.hexaco.hrms.dto.EmployeeUpdateDTO;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "http://localhost:3000")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Employee> registerEmployee(@RequestBody EmployeeDTO employeeDTO) {
        Employee saved = employeeService.registerEmployee(employeeDTO);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        if (employee != null) return ResponseEntity.ok(employee);
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<java.util.List<Employee>> getAllEmployees(@RequestParam(required = false) Long supervisorId) {
        if (supervisorId != null) {
            return ResponseEntity.ok(employeeService.getEmployeesBySupervisor(supervisorId));
        }
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @DeleteMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String code) {
        try {
            employeeService.deleteEmployeeByCode(code);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Employee> updateEmployee(@PathVariable String code, @RequestBody EmployeeUpdateDTO dto) {
        try {
            Employee updated = employeeService.updateEmployee(code, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{code}/fingerprint-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Employee> updateFingerprintStatus(@PathVariable String code, @RequestBody EmployeeUpdateDTO dto) {
        if (dto.getFingerprintEnrolled() == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Employee updated = employeeService.updateFingerprintStatus(code, dto.getFingerprintEnrolled());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/profile-picture")
    public ResponseEntity<Employee> updateProfilePicture(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        String profilePicturePath = body.get("profilePicturePath");
        try {
            Employee updated = employeeService.updateProfilePicture(id, profilePicturePath);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/exists-nic/{nicNumber}")
    public ResponseEntity<Boolean> existsByNicNumber(@PathVariable String nicNumber) {
        boolean exists = employeeService.existsByNicNumber(nicNumber);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists-email/{email:.+}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = employeeService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists-phone")
    public ResponseEntity<Boolean> existsByPhoneParam(@org.springframework.web.bind.annotation.RequestParam(required = false) String phoneNumber) {
        boolean exists = employeeService.existsByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists-phone/{phoneNumber}")
    public ResponseEntity<Boolean> existsByPhoneNumber(@PathVariable String phoneNumber) {
        boolean exists = employeeService.existsByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(exists);
    }
}
