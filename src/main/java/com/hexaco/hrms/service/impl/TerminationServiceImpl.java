package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.TerminationDto;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.Termination;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.repository.TerminationRepository;
import com.hexaco.hrms.repository.UserAccountRepository;
import com.hexaco.hrms.models.UserAccount;
import com.hexaco.hrms.service.NotificationService;
import com.hexaco.hrms.service.TerminationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TerminationServiceImpl implements TerminationService {

    private final TerminationRepository repository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;
    private final UserAccountRepository userAccountRepository;

    @Override
    @Transactional
    public TerminationDto createTermination(TerminationDto dto) {
        Employee employee;
        if (dto.getEmployeeId() != null) {
            employee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + dto.getEmployeeId()));
        } else if (dto.getEpfNumber() != null && !dto.getEpfNumber().trim().isEmpty()) {
            employee = employeeRepository.findByEpfNumber(dto.getEpfNumber())
                    .orElseThrow(() -> new RuntimeException("Employee not found with EPF: " + dto.getEpfNumber()));
        } else {
            throw new RuntimeException("Employee ID or EPF Number must be provided");
        }

        Termination termination = Termination.builder()
                .employee(employee)
                .employeeName(dto.getEmployeeName() != null ? dto.getEmployeeName() : employee.getFullName())
                .epfNumber(dto.getEpfNumber() != null ? dto.getEpfNumber() : employee.getEpfNumber())
                .branch(dto.getBranch() != null ? dto.getBranch() : employee.getDepartment())
                .type(dto.getType())
                .initiationDate(dto.getInitiationDate())
                .effectiveDate(dto.getEffectiveDate())
                .reason(dto.getReason())
                .specialRemark(dto.getSpecialRemark())
                .status(dto.getStatus() != null ? dto.getStatus() : "NEW")
                .requestForTerminationDoc(dto.getRequestForTerminationDoc())
                .loanClearanceLetterDoc(dto.getLoanClearanceLetterDoc())
                .otherDocumentDoc(dto.getOtherDocumentDoc())
                .hrRemark(dto.getHrRemark())
                .directorRemark(dto.getDirectorRemark())
                .boardMeetingDate(dto.getBoardMeetingDate())
                .build();

        Termination saved = repository.save(termination);
        return mapToDto(saved);
    }

    @Override
    public List<TerminationDto> getAllTerminations() {
        return repository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TerminationDto> getTerminationsByEmployeeId(Long employeeId) {
        return repository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public TerminationDto getTerminationById(Long id) {
        Termination termination = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Termination request not found with id: " + id));
        return mapToDto(termination);
    }

    @Override
    @Transactional
    public TerminationDto updateTerminationStatus(Long id, String status, String remarks, String boardMeetingDate) {
        Termination termination = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Termination request not found with id: " + id));

        termination.setStatus(status);
        if (remarks != null) {
            if ("VERIFIED_BY_HR".equalsIgnoreCase(status) || "PENDING_ADMIN".equalsIgnoreCase(status)) {
                termination.setHrRemark(remarks);
            } else {
                termination.setDirectorRemark(remarks);
            }
        }
        if (boardMeetingDate != null) {
            termination.setBoardMeetingDate(boardMeetingDate);
        }

        Termination updated = repository.save(termination);

        // Send email notifications:
        String empEmail = null;
        String empName = updated.getEmployeeName();
        if (updated.getEmployee() != null) {
            if (empName == null || empName.trim().isEmpty()) {
                empName = updated.getEmployee().getFullName();
            }
            if (updated.getEmployee().getEmail() != null && !updated.getEmployee().getEmail().trim().isEmpty()) {
                empEmail = updated.getEmployee().getEmail().trim();
            }
        }
        if (empEmail == null && updated.getEpfNumber() != null) {
            empEmail = employeeRepository.findByEpfNumber(updated.getEpfNumber())
                    .map(Employee::getEmail)
                    .orElse(null);
        }

        // 1. When approved by board: notify the relevant employee
        if ("APPROVED".equalsIgnoreCase(status) || "Board Approved".equalsIgnoreCase(status)) {
            if (empEmail != null && !empEmail.isEmpty()) {
                log.info("Sending termination approval notification to employee: {} <{}>", empName, empEmail);
                notificationService.sendTerminationStatusUpdate(
                        empName,
                        empEmail,
                        status,
                        remarks
                );
            } else {
                log.warn("⚠️ No employee email found for termination #{} - cannot send approval email", updated.getId());
            }
        } 
        // 2. When rejected by board: notify the relevant employee AND HR users
        else if ("REJECTED".equalsIgnoreCase(status) || "Board Rejected".equalsIgnoreCase(status)) {
            // (a) Send rejection notice to employee
            if (empEmail != null && !empEmail.isEmpty()) {
                log.info("Sending termination rejection notification to employee: {} <{}>", empName, empEmail);
                notificationService.sendTerminationStatusUpdate(
                        empName,
                        empEmail,
                        status,
                        remarks
                );
            } else {
                log.warn("⚠️ No employee email found for termination #{} - cannot send employee rejection email", updated.getId());
            }

            // (b) Send rejection notice to HR users with stated reason
            log.info("Termination request #{} was rejected by board. Notifying HR users with stated reason.", updated.getId());
            notifyHrOfTerminationRejection(updated, remarks);
        }

        return mapToDto(updated);
    }

    private void notifyHrOfTerminationRejection(Termination termination, String remarks) {
        String empName = termination.getEmployeeName() != null ? termination.getEmployeeName()
                : (termination.getEmployee() != null ? termination.getEmployee().getFullName() : "Employee");
        String epf = termination.getEpfNumber() != null ? termination.getEpfNumber()
                : (termination.getEmployee() != null ? termination.getEmployee().getEpfNumber() : "N/A");
        String branch = termination.getBranch() != null ? termination.getBranch()
                : (termination.getEmployee() != null ? termination.getEmployee().getDepartment() : "N/A");

        java.util.List<UserAccount> hrAccounts = new java.util.ArrayList<>();
        try {
            hrAccounts.addAll(userAccountRepository.findByRoleRoleName("ROLE_HR"));
            hrAccounts.addAll(userAccountRepository.findByRoleRoleName("HR"));
        } catch (Exception e) {
            log.error("Failed to query HR users for termination rejection notification: {}", e.getMessage());
        }

        java.util.Set<String> notifiedEmails = new java.util.HashSet<>();

        for (UserAccount hr : hrAccounts) {
            if (!hr.isActive()) continue;
            String targetEmail = null;
            String recipientName = "HR Team";
            if (hr.getEmployee() != null) {
                if (hr.getEmployee().getFullName() != null && !hr.getEmployee().getFullName().trim().isEmpty()) {
                    recipientName = hr.getEmployee().getFullName();
                }
                if (hr.getEmployee().getEmail() != null && !hr.getEmployee().getEmail().trim().isEmpty()) {
                    targetEmail = hr.getEmployee().getEmail().trim();
                }
            }
            if (targetEmail == null && hr.getEmail() != null && !hr.getEmail().trim().isEmpty()) {
                targetEmail = hr.getEmail().trim();
            }

            if (targetEmail != null && !notifiedEmails.contains(targetEmail.toLowerCase())) {
                notifiedEmails.add(targetEmail.toLowerCase());
                log.info("Dispatching termination rejection notice to HR user: {} <{}>", recipientName, targetEmail);
                notificationService.sendTerminationRejectionToHr(
                        recipientName,
                        targetEmail,
                        empName,
                        epf,
                        branch,
                        remarks
                );
            }
        }

        // If no HR users found or none notified, send to fallback HR email
        if (notifiedEmails.isEmpty()) {
            log.warn("No active HR accounts found. Sending rejection email to fallback hr@nexora.com");
            notificationService.sendTerminationRejectionToHr(
                    "HR Team",
                    "hr@nexora.com",
                    empName,
                    epf,
                    branch,
                    remarks
            );
        }
    }

    @Override
    @Transactional
    public TerminationDto updateTermination(Long id, TerminationDto dto) {
        Termination termination = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Termination request not found with id: " + id));

        if (dto.getEmployeeId() != null && (termination.getEmployee() == null || !dto.getEmployeeId().equals(termination.getEmployee().getId()))) {
            Employee employee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found with id: " + dto.getEmployeeId()));
            termination.setEmployee(employee);
            termination.setEmployeeName(dto.getEmployeeName() != null ? dto.getEmployeeName() : employee.getFullName());
            termination.setEpfNumber(dto.getEpfNumber() != null ? dto.getEpfNumber() : employee.getEpfNumber());
            termination.setBranch(dto.getBranch() != null ? dto.getBranch() : employee.getDepartment());
        } else if (dto.getEpfNumber() != null && (termination.getEmployee() == null || !dto.getEpfNumber().equals(termination.getEmployee().getEpfNumber()))) {
            employeeRepository.findByEpfNumber(dto.getEpfNumber()).ifPresent(emp -> {
                termination.setEmployee(emp);
                if (dto.getEmployeeName() == null) termination.setEmployeeName(emp.getFullName());
                if (dto.getBranch() == null) termination.setBranch(emp.getDepartment());
            });
            termination.setEpfNumber(dto.getEpfNumber());
            if (dto.getEmployeeName() != null) termination.setEmployeeName(dto.getEmployeeName());
            if (dto.getBranch() != null) termination.setBranch(dto.getBranch());
        } else {
            if (dto.getEmployeeName() != null) termination.setEmployeeName(dto.getEmployeeName());
            if (dto.getEpfNumber() != null) termination.setEpfNumber(dto.getEpfNumber());
            if (dto.getBranch() != null) termination.setBranch(dto.getBranch());
        }

        if (dto.getInitiationDate() != null) termination.setInitiationDate(dto.getInitiationDate());
        if (dto.getEffectiveDate() != null) termination.setEffectiveDate(dto.getEffectiveDate());
        if (dto.getType() != null) termination.setType(dto.getType());
        if (dto.getReason() != null) termination.setReason(dto.getReason());
        if (dto.getSpecialRemark() != null) termination.setSpecialRemark(dto.getSpecialRemark());
        if (dto.getStatus() != null) termination.setStatus(dto.getStatus());
        if (dto.getHrRemark() != null) termination.setHrRemark(dto.getHrRemark());
        if (dto.getDirectorRemark() != null) termination.setDirectorRemark(dto.getDirectorRemark());
        if (dto.getBoardMeetingDate() != null) termination.setBoardMeetingDate(dto.getBoardMeetingDate());

        if (dto.getRequestForTerminationDoc() != null) termination.setRequestForTerminationDoc(dto.getRequestForTerminationDoc());
        if (dto.getLoanClearanceLetterDoc() != null) termination.setLoanClearanceLetterDoc(dto.getLoanClearanceLetterDoc());
        if (dto.getOtherDocumentDoc() != null) termination.setOtherDocumentDoc(dto.getOtherDocumentDoc());

        return mapToDto(repository.save(termination));
    }

    @Override
    @Transactional
    public void executeTermination(Long id) {
        Termination termination = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Termination request not found"));

        if (!"APPROVED".equalsIgnoreCase(termination.getStatus()) && !"Board Approved".equalsIgnoreCase(termination.getStatus())) {
            throw new RuntimeException("Only APPROVED termination requests can be executed.");
        }

        termination.setStatus("EXECUTED");
        repository.save(termination);

        Employee employee = termination.getEmployee();
        java.util.List<UserAccount> accounts = userAccountRepository.findByEmployeeId(employee.getId());
        for (UserAccount account : accounts) {
            account.setActive(false);
            userAccountRepository.save(account);
        }
    }

    private TerminationDto mapToDto(Termination termination) {
        return TerminationDto.builder()
                .id(termination.getId())
                .employeeId(termination.getEmployee().getId())
                .employeeName(termination.getEmployeeName())
                .epfNumber(termination.getEpfNumber())
                .branch(termination.getBranch())
                .type(termination.getType())
                .initiationDate(termination.getInitiationDate())
                .effectiveDate(termination.getEffectiveDate())
                .reason(termination.getReason())
                .specialRemark(termination.getSpecialRemark())
                .status(termination.getStatus())
                .requestForTerminationDoc(termination.getRequestForTerminationDoc())
                .loanClearanceLetterDoc(termination.getLoanClearanceLetterDoc())
                .otherDocumentDoc(termination.getOtherDocumentDoc())
                .hrRemark(termination.getHrRemark())
                .directorRemark(termination.getDirectorRemark())
                .boardMeetingDate(termination.getBoardMeetingDate())
                .createdAt(termination.getCreatedAt())
                .updatedAt(termination.getUpdatedAt())
                .build();
    }
}
