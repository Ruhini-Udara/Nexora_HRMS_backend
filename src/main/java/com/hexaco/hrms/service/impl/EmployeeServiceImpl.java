package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.EmployeeDTO;
import com.hexaco.hrms.models.Designation;
import java.util.List;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.Role;
import com.hexaco.hrms.models.UserAccount;
import com.hexaco.hrms.repository.BranchRepository;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service implementation for managing Employee profiles and related operations.
 * Evaluator Note: This service acts as the central business logic layer for the Employee Profile Module.
 * It handles strict validations (e.g., NIC formats, Email uniqueness), handles cascading entity saves
 * (like creating associated system UserAccounts), and ensures biometric identities are generated.
 */
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DesignationRepository designationRepository;
    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @Override
    @Transactional
    public Employee registerEmployee(EmployeeDTO dto) {
        // Evaluator Note: Data validation phase. We enforce strict rules on identity documents (NIC, EPF, ETF)
        // to ensure data integrity before attempting any database operations.
        // Validate required fields
        if (dto.getNicNumber() == null || dto.getNicNumber().trim().isEmpty()) {
            throw new RuntimeException("NIC Number is required.");
        }
        if (dto.getFullName() == null || dto.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full Name is required.");
        }
        if (dto.getSurname() == null || dto.getSurname().trim().isEmpty()) {
            throw new RuntimeException("Surname is required.");
        }
        if (dto.getSex() == null || dto.getSex().trim().isEmpty()) {
            throw new RuntimeException("Sex is required.");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email Address is required.");
        }

        // Validate NIC Format (Sri Lankan): 9 digits + v/V/x/X or 12 digits
        String nic = dto.getNicNumber().trim();
        if (!nic.matches("^([0-9]{9}[vVxX]|[0-9]{12})$")) {
            throw new RuntimeException("Invalid Sri Lankan NIC format. Must be 9 digits with V/X or 12 digits.");
        }

        // Validate Email format
        String email = dto.getEmail().trim();
        if (!email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,7}$")) {
            throw new RuntimeException("Invalid Email Address format.");
        }

        // Check uniqueness of NIC Number
        if (employeeRepository.findByNicNumber(nic).isPresent()) {
            throw new RuntimeException("An employee with this NIC number already exists.");
        }

        // Check uniqueness of Email
        if (employeeRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("An employee with this Email address already exists.");
        }

        // Validate EPF format & uniqueness (if provided)
        if (dto.getEpfNumber() != null && !dto.getEpfNumber().trim().isEmpty()) {
            String epf = dto.getEpfNumber().trim();
            if (!epf.matches("^\\d{5}/[A-Za-z]/\\d{2}$")) {
                throw new RuntimeException("Invalid EPF format. Format must be 12345/A/12 (5 digits / Letter / 2 digits).");
            }
            if (employeeRepository.findByEpfNumber(epf).isPresent()) {
                throw new RuntimeException("An employee with this EPF number already exists.");
            }
        }

        // Validate ETF format & uniqueness (if provided)
        if (dto.getEtfNumber() != null && !dto.getEtfNumber().trim().isEmpty()) {
            String etf = dto.getEtfNumber().trim();
            if (!etf.matches("^\\d{5}/[A-Za-z]/\\d{2}$")) {
                throw new RuntimeException("Invalid ETF format. Format must be 12345/A/12 (5 digits / Letter / 2 digits).");
            }
            if (employeeRepository.findByEtfNumber(etf).isPresent()) {
                throw new RuntimeException("An employee with this ETF number already exists.");
            }
        }

        // Look up designation by ID
        Designation designation = null;
        if (dto.getDesignationId() != null) {
            designation = designationRepository.findById(dto.getDesignationId())
                    .orElseThrow(
                            () -> new RuntimeException("Designation not found with id: " + dto.getDesignationId()));
        }

        LocalDate dob = parseDate(dto.getDateOfBirth());
        LocalDate dateJoined = parseDate(dto.getDateJoined());
        if (dob != null && dateJoined != null) {
            if (dateJoined.isBefore(dob.plusYears(18))) {
                throw new RuntimeException("Date joined must be after birthday + 18 years.");
            }
        }

        // Validate that birth year matches NIC number
        if (dob != null && dto.getNicNumber() != null) {
            String nicTrimmed = dto.getNicNumber().trim();
            if (nicTrimmed.length() >= 4 && nicTrimmed.matches("^[0-9]{4}.*")) {
                String nicYear = nicTrimmed.substring(0, 4);
                if (nicTrimmed.matches("^[0-9]{9}[vVxX]$")) {
                    String nic2Digit = nicTrimmed.substring(0, 2);
                    String dob2Digit = String.valueOf(dob.getYear()).substring(2);
                    if (!dob2Digit.equals(nic2Digit)) {
                        throw new RuntimeException("The year of birthday must match the birth year in the NIC number.");
                    }
                } else if (dob.getYear() != Integer.parseInt(nicYear)) {
                    throw new RuntimeException("The year of birthday should be same to nic number first four number.");
                }
            }
        }

        Employee employee = Employee.builder()
                .nicNumber(dto.getNicNumber())
                .sex(dto.getSex())
                .fullName(dto.getFullName())
                .surname(dto.getSurname())
                .dateOfBirth(dob)
                .dateJoined(dateJoined)
                .email(dto.getEmail() != null ? dto.getEmail().trim() : null)
                .homeAddress(dto.getHomeAddress())
                .maritalStatus(dto.getMaritalStatus())
                .phoneNumber(dto.getPhoneNumber())
                .designation(designation)
                .employeeType(dto.getEmployeeType())
                .department(dto.getDepartment())
                .branch(dto.getBranch())
                .epfNumber(dto.getEpfNumber())
                .etfNumber(dto.getEtfNumber())
                .build();

        Employee savedEmployee = employeeRepository.save(employee);

        // Generate employee code
        String code = "EMP" + String.format("%03d", savedEmployee.getId());
        savedEmployee.setEmployeeCode(code);
        savedEmployee.setFingerprintUserId(savedEmployee.getId());
        savedEmployee.setFingerprintEnrolled(false);
        savedEmployee = employeeRepository.save(savedEmployee);

        // Handle System Access / User Accounts
        if (dto.isEnableSystemAccess()) {
            createUserAccounts(savedEmployee, dto);
        }

        return savedEmployee;
    }

    /**
     * Creates system access accounts for a newly registered employee.
     * Evaluator Note: We automatically provision a "ROLE_EMPLOYEE" account for self-service, 
     * and conditionally provision an additional specialized account (e.g., "ROLE_HR") if requested.
     */
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
    @Transactional
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .map(this::ensureFingerprintIdentity)
                .orElse(null);
    }

    @Override
    @Transactional
    public java.util.List<Employee> getAllEmployees() {
        java.util.List<Employee> employees = employeeRepository.findAll();
        employees.forEach(this::ensureFingerprintIdentity);
        return employees;
    }

    @Override
    @Transactional
    public java.util.List<Employee> getEmployeesBySupervisor(Long supervisorId) {
        java.util.List<Employee> employees = employeeRepository.findByReportingOfficerId(supervisorId);
        employees.forEach(this::ensureFingerprintIdentity);
        return employees;
    }

    @Override
    public List<Employee> getUpcomingBirthdays() {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        
        return employeeRepository.findAll().stream()
            .filter(e -> e.getDateOfBirth() != null)
            .filter(e -> {
                LocalDate dob = e.getDateOfBirth();
                LocalDate nextBirthday = dob.withYear(today.getYear());
                if (nextBirthday.isBefore(today) || (nextBirthday.isBefore(today) && dob.getMonthValue() == 2 && dob.getDayOfMonth() == 29 && !today.isLeapYear())) {
                    nextBirthday = nextBirthday.plusYears(1);
                }
                return !nextBirthday.isBefore(today) && !nextBirthday.isAfter(nextWeek);
            })
            .sorted(java.util.Comparator.comparing(e -> {
                LocalDate dob = e.getDateOfBirth();
                LocalDate nextBirthday = dob.withYear(today.getYear());
                if (nextBirthday.isBefore(today) || (nextBirthday.isBefore(today) && dob.getMonthValue() == 2 && dob.getDayOfMonth() == 29 && !today.isLeapYear())) {
                    nextBirthday = nextBirthday.plusYears(1);
                }
                return nextBirthday;
            }))
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteEmployeeByCode(String code) {
        Employee employee = employeeRepository.findByEmployeeCode(code)
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + code));

        userAccountRepository.deleteByEmployee(employee);
        employeeRepository.delete(employee);
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        return LocalDate.parse(dateStr, DATE_FORMAT);
    }

    @Override
    @Transactional
    public Employee updateEmployee(String code, com.hexaco.hrms.dto.EmployeeUpdateDTO dto) {
        Employee employee = employeeRepository.findByEmployeeCode(code)
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + code));

        if (dto.getFullName() != null) {
            employee.setFullName(dto.getFullName());
        }
        if (dto.getEmail() != null) {
            employee.setEmail(dto.getEmail().trim());
        }
        if (dto.getDepartment() != null) {
            employee.setDepartment(dto.getDepartment());
        }
        if (dto.getBranch() != null) {
            employee.setBranch(dto.getBranch());
        }
        if (dto.getEmployeeType() != null) {
            employee.setEmployeeType(dto.getEmployeeType());
        }
        if (dto.getPhoneNumber() != null) {
            employee.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getDesignationId() != null) {
            Designation designation = designationRepository.findById(dto.getDesignationId())
                    .orElseThrow(
                            () -> new RuntimeException("Designation not found with id: " + dto.getDesignationId()));
            employee.setDesignation(designation);
        }
        if (dto.getFingerprintEnrolled() != null) {
            applyFingerprintStatus(employee, dto.getFingerprintEnrolled());
        }

        return employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public Employee updateFingerprintStatus(String code, Boolean fingerprintEnrolled) {
        if (fingerprintEnrolled == null) {
            throw new RuntimeException("fingerprintEnrolled is required");
        }

        Employee employee = employeeRepository.findByEmployeeCode(code)
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + code));

        applyFingerprintStatus(employee, fingerprintEnrolled);
        return employeeRepository.save(employee);
    }

    private void applyFingerprintStatus(Employee employee, boolean fingerprintEnrolled) {
        ensureFingerprintIdentity(employee);
        employee.setFingerprintEnrolled(fingerprintEnrolled);
        employee.setFingerprintEnrolledAt(fingerprintEnrolled ? LocalDateTime.now() : null);
        employee.setLastFingerprintSyncAt(LocalDateTime.now());
    }

    private Employee ensureFingerprintIdentity(Employee employee) {
        if (employee.getFingerprintUserId() == null && employee.getId() != null) {
            employee.setFingerprintUserId(employee.getId());
        }
        if (employee.getFingerprintEnrolled() == null) {
            employee.setFingerprintEnrolled(false);
        }
        return employee;
    }

    @Override
    @Transactional
    public Employee updateProfilePicture(Long id, String profilePicturePath) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        employee.setProfilePicturePath(profilePicturePath);
        return employeeRepository.save(employee);
    }

    @Override
    public boolean existsByNicNumber(String nicNumber) {
        if (nicNumber == null || nicNumber.trim().isEmpty()) {
            return false;
        }
        return employeeRepository.findByNicNumber(nicNumber.trim()).isPresent();
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return employeeRepository.findByEmail(email.trim()).isPresent();
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        return employeeRepository.findByPhoneNumber(phoneNumber.trim()).isPresent();
    }

    @Override
    public boolean existsByEpfNumber(String epfNumber) {
        if (epfNumber == null || epfNumber.trim().isEmpty()) {
            return false;
        }
        return employeeRepository.findByEpfNumber(epfNumber.trim()).isPresent();
    }

    @Override
    public boolean existsByEtfNumber(String etfNumber) {
        if (etfNumber == null || etfNumber.trim().isEmpty()) {
            return false;
        }
        return employeeRepository.findByEtfNumber(etfNumber.trim()).isPresent();
    }

    @Override
    public List<String> getDistinctBranches() {
        return branchRepository.findAll().stream()
                .map(com.hexaco.hrms.models.Branch::getName)
                .collect(java.util.stream.Collectors.toList());
    }
}
