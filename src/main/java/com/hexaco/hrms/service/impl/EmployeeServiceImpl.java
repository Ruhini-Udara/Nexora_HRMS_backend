package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.EmployeeDTO;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.service.EmployeeService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Employee registerEmployee(EmployeeDTO dto) {
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
                .designation(dto.getDesignation())
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

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return LocalDate.parse(dateStr, DATE_FORMAT);
    }
}
