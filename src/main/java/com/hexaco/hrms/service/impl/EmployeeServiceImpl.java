package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.EmployeeDTO;
import com.hexaco.hrms.models.Designation;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.Role;
import com.hexaco.hrms.models.UserAccount;
import com.hexaco.hrms.repository.DesignationRepository;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.repository.RoleRepository;
import com.hexaco.hrms.repository.UserAccountRepository;
import com.hexaco.hrms.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DesignationRepository designationRepository;
    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @Override
    @Transactional
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
                .email(dto.getEmail() != null ? dto.getEmail().trim() : null)
                .homeAddress(dto.getHomeAddress())
                .maritalStatus(dto.getMaritalStatus())
                .designation(designation)
                .employeeType(dto.getEmployeeType())
                .department(dto.getDepartment())
                .epfNumber(dto.getEpfNumber())
                .etfNumber(dto.getEtfNumber())
                .build();

        Employee savedEmployee = employeeRepository.save(employee);

        // Generate employee code
        String code = "EMP" + String.format("%03d", savedEmployee.getId());
        savedEmployee.setEmployeeCode(code);
        savedEmployee = employeeRepository.save(savedEmployee);

        // Handle System Access / User Accounts
        if (dto.isEnableSystemAccess()) {
            createUserAccounts(savedEmployee, dto);
        }

        return savedEmployee;
    }

    private void createUserAccounts(Employee employee, EmployeeDTO dto) {
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        // 1. Create the standard "Normal Employee" account (using personal email)
        Role employeeRole = roleRepository.findByRoleName("ROLE_EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Default Employee role not found"));

        UserAccount personalAccount = UserAccount.builder()
                .userName(employee.getEmployeeCode() + "_emp")
                .email(employee.getEmail())
                .passwordHash(encodedPassword)
                .role(employeeRole)
                .employee(employee)
                .isActive(true)
                .build();
        userAccountRepository.save(personalAccount);

        // 2. Create the specialized Role account if applicable (using account email)
        if (dto.getRoleName() != null && !dto.getRoleName().equalsIgnoreCase("Employee")) {
            String roleName = "ROLE_" + dto.getRoleName().toUpperCase();
            Role specializedRole = roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

            UserAccount roleAccount = UserAccount.builder()
                    .userName(employee.getEmployeeCode() + "_" + dto.getRoleName().toLowerCase())
                    .email(dto.getAccountEmail() != null ? dto.getAccountEmail().trim() : null)
                    .passwordHash(encodedPassword)
                    .role(specializedRole)
                    .employee(employee)
                    .isActive(true)
                    .build();
            userAccountRepository.save(roleAccount);
        }
    }

    @Override
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id).orElse(null);
    }

    @Override
    public java.util.List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return LocalDate.parse(dateStr, DATE_FORMAT);
    }
}
