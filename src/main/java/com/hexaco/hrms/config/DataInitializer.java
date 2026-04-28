package com.hexaco.hrms.config;

import com.hexaco.hrms.models.*;
import com.hexaco.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserAccountRepository userAccountRepository;
    private final EmployeeRepository employeeRepository;
    private final DesignationRepository designationRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            // 1. Initialize Roles
            initializeRoles();

            // 2. Initialize Designations
            initializeDesignations();

            // 3. Get Admin Designation safely
            Designation adminDesignation = designationRepository.findAll().stream()
                    .filter(d -> d.getDesignationName().equalsIgnoreCase("System Administrator"))
                    .findFirst()
                    .orElseGet(() -> designationRepository.save(Designation.builder()
                            .designationName("System Administrator")
                            .description("Default Admin Designation")
                            .build()));

            // 4. Initialize a default Employee
            Employee adminEmployee = initializeEmployee(adminDesignation);

            // 5. Initialize User Accounts
            initializeUserAccounts(adminEmployee);
        } catch (Exception e) {
            System.err.println("Error during Data Initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeRoles() {
        List<String> roleNames = Arrays.asList("ROLE_ADMIN", "ROLE_EMPLOYEE", "ROLE_HR", "ROLE_DIRECTOR", "ROLE_SUPERVISOR");
        for (String roleName : roleNames) {
            if (roleRepository.findByRoleName(roleName).isEmpty()) {
                roleRepository.save(Role.builder()
                        .roleName(roleName)
                        .description("Default " + roleName)
                        .build());
            }
        }
    }

    private void initializeDesignations() {
        List<String> names = Arrays.asList("System Administrator", "Director", "HR Manager", "Software Engineer",
                "Operations Manager");
        for (String name : names) {
            boolean exists = designationRepository.findAll().stream()
                    .anyMatch(d -> d.getDesignationName().equalsIgnoreCase(name));
            if (!exists) {
                designationRepository.save(Designation.builder()
                        .designationName(name)
                        .description("Default " + name)
                        .build());
            }
        }
    }

    private Employee initializeEmployee(Designation designation) {
        String adminEmail = "admin@nexora.com";
        return employeeRepository.findAll().stream()
                .filter(e -> e.getEmail().equals(adminEmail))
                .findFirst()
                .orElseGet(() -> employeeRepository.save(Employee.builder()
                        .employeeCode("EMP000")
                        .nicNumber("000000000V")
                        .sex("Male")
                        .fullName("System Admin User")
                        .surname("Admin")
                        .email(adminEmail)
                        .dateOfBirth(LocalDate.of(1990, 1, 1))
                        .dateJoined(LocalDate.now())
                        .designation(designation)
                        .employeeType("Permanent")
                        .department("IT")
                        .build()));
    }

    private void initializeUserAccounts(Employee employee) {
        Role adminRole = roleRepository.findByRoleName("ROLE_ADMIN").orElseThrow();
        Role employeeRole = roleRepository.findByRoleName("ROLE_EMPLOYEE").orElseThrow();

        // Admin Account
        userAccountRepository.findByEmail("admin@nexora.com").ifPresentOrElse(
                user -> {
                    user.setPasswordHash(passwordEncoder.encode("admin123"));
                    userAccountRepository.save(user);
                },
                () -> userAccountRepository.save(UserAccount.builder()
                        .userName("admin")
                        .email("admin@nexora.com")
                        .passwordHash(passwordEncoder.encode("admin123"))
                        .isActive(true)
                        .role(adminRole)
                        .employee(employee)
                        .build()));

        // Normal Employee Account
        userAccountRepository.findByEmail("pasan.emp@nexora.com").ifPresentOrElse(
                user -> {
                    user.setPasswordHash(passwordEncoder.encode("pasan123"));
                    userAccountRepository.save(user);
                },
                () -> userAccountRepository.save(UserAccount.builder()
                        .userName("pasan_emp")
                        .email("pasan.emp@nexora.com")
                        .passwordHash(passwordEncoder.encode("pasan123"))
                        .isActive(true)
                        .role(employeeRole)
                        .employee(employee)
                        .build()));
    }
}
