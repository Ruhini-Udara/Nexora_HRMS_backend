package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.EmployeeDTO;
import com.hexaco.hrms.models.Designation;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.repository.DesignationRepository;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.service.EmployeeService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DesignationRepository designationRepository;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               DesignationRepository designationRepository) {
        this.employeeRepository = employeeRepository;
        this.designationRepository = designationRepository;
    }

    @Override
    public Employee registerEmployee(EmployeeDTO dto) {
        // Look up designation by ID
        Designation designation = null;
        if (dto.getDesignationId() != null) {
            designation = designationRepository.findById(dto.getDesignationId())
                    .orElseThrow(() -> new RuntimeException("Designation not found with id: " + dto.getDesignationId()));
        }

        Employee employee = Employee.builder()
                .nicNumber(dto.getNicNumber())
                .sex(dto.getSex())
                .fullName(dto.getFullName())
                .surname(dto.getSurname())
                .dateOfBirth(parseDate(dto.getDateOfBirth()))
                .dateJoined(parseDate(dto.getDateJoined()))
                .email(dto.getEmail())
                .homeAddress(dto.getHomeAddress())
                .maritalStatus(dto.getMaritalStatus())
                .designation(designation)
                .employeeType(dto.getEmployeeType())
                .department(dto.getDepartment())
                .epfNumber(dto.getEpfNumber())
                .etfNumber(dto.getEtfNumber())
                .build();

        Employee savedEmployee = employeeRepository.save(employee);

        // Generate employee code from ID (e.g. EMP001, EMP002, ...)
        String code = "EMP" + String.format("%03d", savedEmployee.getId());
        savedEmployee.setEmployeeCode(code);

        return employeeRepository.save(savedEmployee);
    }

    @Override
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id).orElse(null);
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return LocalDate.parse(dateStr, DATE_FORMAT);
    }
}
