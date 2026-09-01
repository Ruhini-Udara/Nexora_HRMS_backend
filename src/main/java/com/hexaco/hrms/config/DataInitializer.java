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
    private final ShiftRepository shiftRepository;
    private final ResignationRepository resignationRepository;
    private final TerminationRepository terminationRepository;
    private final DeathRequestRepository deathRequestRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        try {
            // 1. Initialize Roles
            initializeRoles();

            // 2. Initialize Designations
            initializeDesignations();

            // 3. Initialize Shifts
            initializeShifts();

            // 4. Initialize Shift Assignments
            initializeShiftAssignments();

            // 5. Get Admin Designation safely
            Designation adminDesignation = designationRepository.findAll().stream()
                    .filter(d -> d.getDesignationName().equalsIgnoreCase("System Administrator"))
                    .findFirst()
                    .orElseGet(() -> designationRepository.save(Designation.builder()
                            .designationName("System Administrator")
                            .description("Default Admin Designation")
                            .build()));

            // 6. Initialize a default Employee
            Employee adminEmployee = initializeEmployee(adminDesignation);

            // 7. Initialize User Accounts
            initializeUserAccounts(adminEmployee);
        } catch (Exception e) {
            System.err.println("Error during Data Initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeRoles() {
        List<String> roleNames = Arrays.asList("ROLE_ADMIN", "ROLE_EMPLOYEE", "ROLE_HR", "ROLE_DIRECTOR",
                "ROLE_SUPERVISOR");
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
        List<String> names = Arrays.asList(
                "System Administrator", "Director", "HR Manager", "Software Engineer", "Operations Manager",
                "Senior Engineer", "Engineer", "HR Executive", "Sales Executive", "Product Manager", "Driver",
                "Support Staff");
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

    private void initializeShifts() {
        List<Shift> defaultShifts = Arrays.asList(
                Shift.builder()
                        .name("Normal Shift")
                        .startTime(java.time.LocalTime.of(8, 30))
                        .endTime(java.time.LocalTime.of(16, 30))
                        .description("Standard operational hours")
                        .build(),
                Shift.builder()
                        .name("Temporary Shift")
                        .startTime(java.time.LocalTime.of(8, 15))
                        .endTime(java.time.LocalTime.of(16, 45))
                        .description("Contract staff & interns")
                        .build(),
                Shift.builder()
                        .name("Drivers Shift")
                        .startTime(java.time.LocalTime.of(8, 0))
                        .endTime(java.time.LocalTime.of(17, 0))
                        .description("Transport & Logistics")
                        .build());

        for (Shift shift : defaultShifts) {
            if (shiftRepository.findByName(shift.getName()).isEmpty()) {
                shiftRepository.save(shift);
            }
        }
    }

    private void initializeShiftAssignments() {
        Shift normalShift = shiftRepository.findByName("Normal Shift").orElse(null);
        Shift temporaryShift = shiftRepository.findByName("Temporary Shift").orElse(null);
        Shift driversShift = shiftRepository.findByName("Drivers Shift").orElse(null);

        if (normalShift == null || driversShift == null || temporaryShift == null) {
            return;
        }

        List<Designation> designations = designationRepository.findAll();
        for (Designation designation : designations) {
            if (designation.getShift() == null) {
                String name = designation.getDesignationName().toLowerCase();
                if (name.contains("driver")) {
                    designation.setShift(driversShift);
                } else if (name.contains("support") || name.contains("temp")) {
                    designation.setShift(temporaryShift);
                } else {
                    designation.setShift(normalShift);
                }
                designationRepository.save(designation);
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
                        .branch("Head Office")
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

        // HR Accounts
        userAccountRepository.findByEmail("rashmi@nexora.com").ifPresent(user -> {
            user.setPasswordHash(passwordEncoder.encode("password123"));
            user.setActive(true);
            userAccountRepository.save(user);
        });
        userAccountRepository.findByEmail("hr@nexora.com").ifPresent(user -> {
            user.setPasswordHash(passwordEncoder.encode("password123"));
            user.setActive(true);
            userAccountRepository.save(user);
        });

        // Fix plaintext passwords and initialize is_active for valid non-offboarded users
        List<UserAccount> allUsers = userAccountRepository.findAll();
        for (UserAccount u : allUsers) {
            boolean modified = false;
            // BCrypt hashes start with $2a$, $2b$, or $2y$
            if (u.getPasswordHash() != null && !u.getPasswordHash().startsWith("$2a$")) {
                System.out.println("Converting plaintext password to BCrypt for user: " + u.getEmail());
                u.setPasswordHash(passwordEncoder.encode(u.getPasswordHash()));
                modified = true;
            }

            boolean isOffboarded = false;
            if (u.getEmployee() != null && u.getEmployee().getId() != null) {
                Long empId = u.getEmployee().getId();
                if (resignationRepository.findByEmployeeId(empId).stream().anyMatch(r -> "EXECUTED".equalsIgnoreCase(r.getStatus())) ||
                    terminationRepository.findByEmployeeId(empId).stream().anyMatch(t -> "EXECUTED".equalsIgnoreCase(t.getStatus())) ||
                    deathRequestRepository.findByEmployeeId(empId).stream().anyMatch(d -> "EXECUTED".equalsIgnoreCase(d.getStatus()))) {
                    isOffboarded = true;
                }
            }

            // If not offboarded, ensure account is active
            if (!isOffboarded && !u.isActive()) {
                System.out.println("Activating valid user account: " + u.getEmail());
                u.setActive(true);
                modified = true;
            }

            if (modified) {
                userAccountRepository.save(u);
            }
        }
    }
}
