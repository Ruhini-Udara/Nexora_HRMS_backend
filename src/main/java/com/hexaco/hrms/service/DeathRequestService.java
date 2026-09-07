package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.DeathRequestDto;
import com.hexaco.hrms.models.DeathRequest;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.Nominee;
import com.hexaco.hrms.repository.DeathRequestRepository;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.repository.NomineeRepository;
import com.hexaco.hrms.repository.UserAccountRepository;
import com.hexaco.hrms.models.UserAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeathRequestService {

    private final DeathRequestRepository repository;
    private final EmployeeRepository employeeRepository;
    private final NomineeRepository nomineeRepository;
    private final NotificationService notificationService;
    private final UserAccountRepository userAccountRepository;
    private final JdbcTemplate jdbcTemplate;

    public List<DeathRequestDto> getAllRequests() {
        return repository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<DeathRequestDto> getRequestsByEmployee(Long employeeId) {
        return repository.findByEmployeeId(employeeId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public DeathRequestDto getRequestById(Long id) {
        DeathRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("DeathRequest not found with id: " + id));
        return mapToDto(request);
    }

    private Employee resolveEmployee(DeathRequestDto dto) {
        // 1. Try by numeric employeeId if provided and exists
        if (dto.getEmployeeId() != null && dto.getEmployeeId() > 0) {
            java.util.Optional<Employee> emp = employeeRepository.findById(dto.getEmployeeId());
            if (emp.isPresent()) {
                return emp.get();
            }
        }

        // 2. Try by employeeIdString
        if (dto.getEmployeeIdString() != null && !dto.getEmployeeIdString().trim().isEmpty()) {
            String code = dto.getEmployeeIdString().trim();
            java.util.Optional<Employee> emp = employeeRepository.findByEmployeeCode(code);
            if (emp.isPresent()) return emp.get();

            emp = employeeRepository.findByEmployeeCode(code.toUpperCase());
            if (emp.isPresent()) return emp.get();

            emp = employeeRepository.findByEpfNumber(code);
            if (emp.isPresent()) return emp.get();

            try {
                Long parsedId = Long.parseLong(code);
                emp = employeeRepository.findById(parsedId);
                if (emp.isPresent()) return emp.get();
            } catch (NumberFormatException ignored) {}
        }

        // 3. Try by EPF number
        if (dto.getEpfNumber() != null && !dto.getEpfNumber().trim().isEmpty()) {
            String epf = dto.getEpfNumber().trim();
            java.util.Optional<Employee> emp = employeeRepository.findByEpfNumber(epf);
            if (emp.isPresent()) return emp.get();

            emp = employeeRepository.findByEmployeeCode(epf);
            if (emp.isPresent()) return emp.get();
        }

        // 4. Try by employeeName if exact match
        if (dto.getEmployeeName() != null && !dto.getEmployeeName().trim().isEmpty()) {
            String name = dto.getEmployeeName().trim();
            java.util.List<Employee> allEmps = employeeRepository.findAll();
            for (Employee e : allEmps) {
                if (name.equalsIgnoreCase(e.getFullName())) {
                    return e;
                }
            }
        }

        throw new RuntimeException("Employee not found with code: " + dto.getEmployeeIdString() + " or EPF: " + dto.getEpfNumber());
    }

    @Transactional
    public DeathRequestDto createRequest(DeathRequestDto dto) {
        Employee employee = resolveEmployee(dto);

        DeathRequest request = DeathRequest.builder()
                .employee(employee)
                .employeeName(dto.getEmployeeName() != null ? dto.getEmployeeName() : employee.getFullName())
                .employeeIdString(dto.getEmployeeIdString() != null ? dto.getEmployeeIdString() : employee.getEmployeeCode())
                .employeePhone(dto.getEmployeePhone() != null ? dto.getEmployeePhone() : employee.getPhoneNumber())
                .dateOfDeath(dto.getDateOfDeath())
                .natureOfDeath(dto.getNatureOfDeath())
                .requesterName(dto.getRequesterName())
                .requesterBranch(dto.getRequesterBranch())
                .requesterDesignation(dto.getRequesterDesignation())
                .requesterEmpId(dto.getRequesterEmpId())
                .requesterNic(dto.getRequesterNic())
                .requesterEmail(dto.getRequesterEmail())
                .address(dto.getAddress())
                .contactNumber(dto.getContactNumber())
                .specialRemark(dto.getSpecialRemark())
                .status(dto.getStatus() == null ? "NEW" : dto.getStatus())
                .nomineeName(dto.getNomineeName())
                .nomineeBank(dto.getNomineeBank())
                .nomineeBranch(dto.getNomineeBranch())
                .nomineeAccount(dto.getNomineeAccount())
                .deathCertificateDoc(dto.getDeathCertificateDoc())
                .nomineeIdDoc(dto.getNomineeIdDoc())
                .requestLetterDoc(dto.getRequestLetterDoc())
                .hrRemark(dto.getHrRemark())
                .build();

        DeathRequest saved = repository.save(request);
        updateNominee(employee, dto);
        return mapToDto(saved);
    }

    private void updateNominee(Employee employee, DeathRequestDto dto) {
        if (employee == null || employee.getId() == null) return;

        boolean hasNomineeData = (dto.getNomineeName() != null && !dto.getNomineeName().trim().isEmpty())
                || (dto.getNomineeRelationship() != null && !dto.getNomineeRelationship().trim().isEmpty())
                || (dto.getNomineeNic() != null && !dto.getNomineeNic().trim().isEmpty())
                || (dto.getNomineeAccount() != null && !dto.getNomineeAccount().trim().isEmpty());

        if (!hasNomineeData) return;

        try {
            String sql = """
                INSERT INTO nominee (employee_id, nominee_name, relationship, nic, phone_no, address, bank_name, bank_branch, account_number)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (employee_id) DO UPDATE SET
                    nominee_name = COALESCE(EXCLUDED.nominee_name, nominee.nominee_name),
                    relationship = COALESCE(EXCLUDED.relationship, nominee.relationship),
                    nic = COALESCE(EXCLUDED.nic, nominee.nic),
                    phone_no = COALESCE(EXCLUDED.phone_no, nominee.phone_no),
                    address = COALESCE(EXCLUDED.address, nominee.address),
                    bank_name = COALESCE(EXCLUDED.bank_name, nominee.bank_name),
                    bank_branch = COALESCE(EXCLUDED.bank_branch, nominee.bank_branch),
                    account_number = COALESCE(EXCLUDED.account_number, nominee.account_number)
            """;
            jdbcTemplate.update(sql,
                    employee.getId(),
                    dto.getNomineeName(),
                    dto.getNomineeRelationship(),
                    dto.getNomineeNic(),
                    dto.getNomineePhone(),
                    dto.getNomineeAddress(),
                    dto.getNomineeBank(),
                    dto.getNomineeBranch(),
                    dto.getNomineeAccount()
            );
        } catch (Exception e) {
            System.err.println("Warning: Nominee upsert failed: " + e.getMessage());
        }
    }

    @Transactional
    public DeathRequestDto updateRequest(Long id, DeathRequestDto dto) {
        DeathRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("DeathRequest not found with id: " + id));

        try {
            if (dto.getEmployeeId() != null || dto.getEmployeeIdString() != null || dto.getEpfNumber() != null) {
                Employee employee = resolveEmployee(dto);
                request.setEmployee(employee);
            }
        } catch (Exception ignored) {
            // Keep existing employee if resolution fails during update
        }

        if (dto.getEmployeeName() != null) request.setEmployeeName(dto.getEmployeeName());
        if (dto.getEmployeeIdString() != null) request.setEmployeeIdString(dto.getEmployeeIdString());
        if (dto.getEmployeePhone() != null) request.setEmployeePhone(dto.getEmployeePhone());
        if (dto.getDateOfDeath() != null) request.setDateOfDeath(dto.getDateOfDeath());
        if (dto.getNatureOfDeath() != null) request.setNatureOfDeath(dto.getNatureOfDeath());
        if (dto.getRequesterName() != null) request.setRequesterName(dto.getRequesterName());
        if (dto.getRequesterBranch() != null) request.setRequesterBranch(dto.getRequesterBranch());
        if (dto.getRequesterDesignation() != null) request.setRequesterDesignation(dto.getRequesterDesignation());
        if (dto.getRequesterEmpId() != null) request.setRequesterEmpId(dto.getRequesterEmpId());
        if (dto.getRequesterNic() != null) request.setRequesterNic(dto.getRequesterNic());
        if (dto.getRequesterEmail() != null) request.setRequesterEmail(dto.getRequesterEmail());
        if (dto.getAddress() != null) request.setAddress(dto.getAddress());
        if (dto.getContactNumber() != null) request.setContactNumber(dto.getContactNumber());
        if (dto.getSpecialRemark() != null) request.setSpecialRemark(dto.getSpecialRemark());
        if (dto.getStatus() != null) request.setStatus(dto.getStatus());
        if (dto.getNomineeName() != null) request.setNomineeName(dto.getNomineeName());
        if (dto.getNomineeBank() != null) request.setNomineeBank(dto.getNomineeBank());
        if (dto.getNomineeBranch() != null) request.setNomineeBranch(dto.getNomineeBranch());
        if (dto.getNomineeAccount() != null) request.setNomineeAccount(dto.getNomineeAccount());
        if (dto.getDeathCertificateDoc() != null) request.setDeathCertificateDoc(dto.getDeathCertificateDoc());
        if (dto.getNomineeIdDoc() != null) request.setNomineeIdDoc(dto.getNomineeIdDoc());
        if (dto.getRequestLetterDoc() != null) request.setRequestLetterDoc(dto.getRequestLetterDoc());
        if (dto.getHrRemark() != null) request.setHrRemark(dto.getHrRemark());
        if (dto.getDirectorRemark() != null) request.setDirectorRemark(dto.getDirectorRemark());

        DeathRequest saved = repository.save(request);
        if (saved.getEmployee() != null) {
            updateNominee(saved.getEmployee(), dto);
        }
        return mapToDto(saved);
    }

    public void deleteRequest(Long id) {
        repository.deleteById(id);
    }

    public DeathRequestDto verifyRequest(Long id) {
        DeathRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus("VERIFIED_BY_HR");
        return mapToDto(repository.save(request));
    }

    public DeathRequestDto rejectRequest(Long id, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required.");
        }
        String cleanReason = reason.trim();
        if (cleanReason.startsWith("{") && cleanReason.contains("\"reason\"")) {
            try {
                com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(cleanReason);
                if (node.has("reason") && !node.get("reason").isNull()) {
                    cleanReason = node.get("reason").asText().trim();
                }
            } catch (Exception ignored) {}
        }
        if (cleanReason.isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required.");
        }
        DeathRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Death application not found with id: " + id));
        request.setStatus("REJECTED");
        request.setHrRemark(cleanReason);
        DeathRequest saved = repository.save(request);
        sendDeathStatusNotification(saved);
        return mapToDto(saved);
    }

    public DeathRequestDto submitToAdmin(Long id) {
        DeathRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus("PENDING_ADMIN");
        return mapToDto(repository.save(request));
    }

    @Transactional
    public DeathRequestDto updateStatus(Long id, String status, String remarks, String boardMeetingDate) {
        DeathRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus(status);
        if (remarks != null) {
            if ("VERIFIED_BY_HR".equalsIgnoreCase(status) || "PENDING_ADMIN".equalsIgnoreCase(status)) {
                request.setHrRemark(remarks);
            } else {
                request.setDirectorRemark(remarks);
            }
        }
        if (boardMeetingDate != null) request.setBoardMeetingDate(boardMeetingDate);
        DeathRequest saved = repository.save(request);
        sendDeathStatusNotification(saved);
        return mapToDto(saved);
    }

    private DeathRequestDto mapToDto(DeathRequest request) {
        Employee emp = request.getEmployee();
        Nominee nominee = null;
        if (emp != null && emp.getId() != null) {
            try {
                nominee = nomineeRepository.findByEmployeeId(emp.getId()).orElse(null);
            } catch (Exception ignored) {}
        }
        return DeathRequestDto.builder()
                .id(request.getId())
                .employeeId(emp != null ? emp.getId() : null)
                .employeeIdString(request.getEmployeeIdString() != null ? request.getEmployeeIdString() : (emp != null ? emp.getEmployeeCode() : null))
                .employeeName(request.getEmployeeName() != null ? request.getEmployeeName() : (emp != null ? emp.getFullName() : null))
                .employeePhone(request.getEmployeePhone() != null ? request.getEmployeePhone() : (emp != null ? emp.getPhoneNumber() : null))
                .epfNumber(emp != null ? emp.getEpfNumber() : null)
                .dateOfDeath(request.getDateOfDeath())
                .natureOfDeath(request.getNatureOfDeath())
                .requesterName(request.getRequesterName())
                .requesterBranch(request.getRequesterBranch())
                .requesterDesignation(request.getRequesterDesignation())
                .requesterEmpId(request.getRequesterEmpId())
                .requesterNic(request.getRequesterNic())
                .requesterEmail(request.getRequesterEmail())
                .address(request.getAddress())
                .contactNumber(request.getContactNumber())
                .specialRemark(request.getSpecialRemark())
                .status(request.getStatus())
                .nomineeName(request.getNomineeName())
                .nomineeBank(request.getNomineeBank())
                .nomineeBranch(request.getNomineeBranch())
                .nomineeAccount(request.getNomineeAccount())
                .nomineeRelationship(nominee != null ? nominee.getRelationship() : null)
                .nomineeNic(nominee != null ? nominee.getNic() : null)
                .nomineePhone(nominee != null ? nominee.getPhoneNo() : null)
                .nomineeAddress(nominee != null ? nominee.getAddress() : null)
                .deathCertificateDoc(request.getDeathCertificateDoc())
                .nomineeIdDoc(request.getNomineeIdDoc())
                .requestLetterDoc(request.getRequestLetterDoc())
                .hrRemark(request.getHrRemark())
                .directorRemark(request.getDirectorRemark())
                .boardMeetingDate(request.getBoardMeetingDate())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

    @Transactional
    public void executeDeathRequest(Long id) {
        DeathRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Death request not found"));

        if (!"APPROVED".equalsIgnoreCase(request.getStatus()) && !"Board Approved".equalsIgnoreCase(request.getStatus())) {
            throw new RuntimeException("Only APPROVED death requests can be executed.");
        }

        request.setStatus("EXECUTED");
        repository.save(request);

        Employee employee = request.getEmployee();
        java.util.List<UserAccount> accounts = userAccountRepository.findByEmployeeId(employee.getId());
        for (UserAccount account : accounts) {
            account.setActive(false);
            userAccountRepository.save(account);
        }
    }

    private String resolveRequesterEmail(DeathRequest request) {
        // 1. Direct explicit requester email stored on request
        if (request.getRequesterEmail() != null && !request.getRequesterEmail().trim().isEmpty()) {
            return request.getRequesterEmail().trim();
        }

        String code = request.getRequesterEmpId();
        if (code != null && !code.trim().isEmpty()) {
            code = code.trim();
            // 2. Employee code exact
            var emp = employeeRepository.findByEmployeeCode(code);
            if (emp.isPresent() && emp.get().getEmail() != null && !emp.get().getEmail().trim().isEmpty()) {
                return emp.get().getEmail().trim();
            }
            // 3. Employee code upper-case
            emp = employeeRepository.findByEmployeeCode(code.toUpperCase());
            if (emp.isPresent() && emp.get().getEmail() != null && !emp.get().getEmail().trim().isEmpty()) {
                return emp.get().getEmail().trim();
            }
            // 4. EPF number
            emp = employeeRepository.findByEpfNumber(code);
            if (emp.isPresent() && emp.get().getEmail() != null && !emp.get().getEmail().trim().isEmpty()) {
                return emp.get().getEmail().trim();
            }
            // 5. Numeric digits extraction (handles "EMP021" -> 21, "21" -> 21)
            try {
                String digits = code.replaceAll("[^0-9]", "");
                if (!digits.isEmpty()) {
                    Long numId = Long.parseLong(digits);
                    // Match employee by primary id
                    emp = employeeRepository.findById(numId);
                    if (emp.isPresent() && emp.get().getEmail() != null && !emp.get().getEmail().trim().isEmpty()) {
                        return emp.get().getEmail().trim();
                    }
                    // Match user_account by employeeId
                    var accounts = userAccountRepository.findByEmployeeId(numId);
                    for (var acc : accounts) {
                        if (acc.getEmail() != null && !acc.getEmail().trim().isEmpty()) {
                            return acc.getEmail().trim();
                        }
                    }
                }
            } catch (Exception ignored) {}

            // 6. User Account username
            var account = userAccountRepository.findByUserName(code);
            if (account.isPresent() && account.get().getEmail() != null && !account.get().getEmail().trim().isEmpty()) {
                return account.get().getEmail().trim();
            }
        }

        // 7. Requester NIC
        if (request.getRequesterNic() != null && !request.getRequesterNic().trim().isEmpty()) {
            var emp = employeeRepository.findByNicNumber(request.getRequesterNic().trim());
            if (emp.isPresent() && emp.get().getEmail() != null && !emp.get().getEmail().trim().isEmpty()) {
                return emp.get().getEmail().trim();
            }
        }

        // 8. Requester Name
        if (request.getRequesterName() != null && !request.getRequesterName().trim().isEmpty()) {
            String name = request.getRequesterName().trim();
            // 8a. Exact full name match in employee
            for (Employee e : employeeRepository.findAll()) {
                if (name.equalsIgnoreCase(e.getFullName()) && e.getEmail() != null && !e.getEmail().trim().isEmpty()) {
                    return e.getEmail().trim();
                }
            }
            // 8b. Word match in user_account (e.g. "Pasan" in "pasan_emp")
            String[] parts = name.split("\\s+");
            for (String part : parts) {
                if (part.length() >= 3) {
                    for (var acc : userAccountRepository.findAll()) {
                        if ((acc.getUserName() != null && acc.getUserName().toLowerCase().contains(part.toLowerCase())) ||
                            (acc.getEmail() != null && acc.getEmail().toLowerCase().contains(part.toLowerCase()))) {
                            if (acc.getEmail() != null && !acc.getEmail().trim().isEmpty()) {
                                return acc.getEmail().trim();
                            }
                        }
                    }
                }
            }
        }

        // 9. Authenticated user fallback
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null && !"anonymousUser".equalsIgnoreCase(auth.getName())) {
                var acc = userAccountRepository.findByUserName(auth.getName());
                if (acc.isPresent() && acc.get().getEmail() != null && !acc.get().getEmail().trim().isEmpty()) {
                    return acc.get().getEmail().trim();
                }
            }
        } catch (Exception ignored) {}

        // 10. Guaranteed fallback so notification is NEVER dropped
        return "rashmibimashaa@gmail.com";
    }

    private void sendDeathStatusNotification(DeathRequest request) {
        String status = request.getStatus();
        if ("APPROVED".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status) ||
            "Board Approved".equalsIgnoreCase(status) || "Board Rejected".equalsIgnoreCase(status)) {
            
            String requesterEmail = resolveRequesterEmail(request);
            String recipientName = (request.getRequesterName() != null && !request.getRequesterName().trim().isEmpty())
                    ? request.getRequesterName().trim()
                    : "Colleague";

            log.info("📧 Initiating death application [{}] notification for request ID {} to {} <{}> (HR Remark: {})",
                    status, request.getId(), recipientName, requesterEmail, request.getHrRemark());
            
            CompletableFuture.runAsync(() -> {
                try {
                    notificationService.sendDeathApplicationStatusUpdate(
                            recipientName,
                            requesterEmail,
                            request.getEmployeeName(),
                            status,
                            request.getHrRemark()
                    );
                    log.info("✅ Death application status update successfully dispatched to {}", requesterEmail);
                } catch (Exception e) {
                    log.error("❌ Failed to dispatch death application email to {}: {}", requesterEmail, e.getMessage(), e);
                }
            });
        }
    }
}
