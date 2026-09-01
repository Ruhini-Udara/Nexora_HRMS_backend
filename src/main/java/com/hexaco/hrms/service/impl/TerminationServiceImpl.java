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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
            if (status.contains("REJECTED") || status.equals("VERIFIED_BY_HR")) {
                termination.setHrRemark(remarks);
            } else {
                termination.setDirectorRemark(remarks);
            }
        }
        if (boardMeetingDate != null) {
            termination.setBoardMeetingDate(boardMeetingDate);
        }

        Termination updated = repository.save(termination);

        // Send email notification on final approval or rejection
        if ("APPROVED".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status) ||
            "Board Approved".equalsIgnoreCase(status) || "Board Rejected".equalsIgnoreCase(status)) {
            notificationService.sendTerminationStatusUpdate(
                    updated.getEmployee().getFullName(),
                    updated.getEmployee().getEmail(),
                    status,
                    remarks
            );
        }

        return mapToDto(updated);
    }

    @Override
    @Transactional
    public TerminationDto updateTermination(Long id, TerminationDto dto) {
        Termination termination = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Termination request not found with id: " + id));

        termination.setInitiationDate(dto.getInitiationDate());
        termination.setEffectiveDate(dto.getEffectiveDate());
        termination.setType(dto.getType());
        termination.setReason(dto.getReason());
        termination.setSpecialRemark(dto.getSpecialRemark());
        termination.setStatus(dto.getStatus() != null ? dto.getStatus() : termination.getStatus());

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
