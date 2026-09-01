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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeathRequestService {

    private final DeathRequestRepository repository;
    private final EmployeeRepository employeeRepository;
    private final NomineeRepository nomineeRepository;
    private final NotificationService notificationService;
    private final UserAccountRepository userAccountRepository;

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

    @Transactional
    public DeathRequestDto createRequest(DeathRequestDto dto) {
        Employee employee;
        if (dto.getEmployeeIdString() != null && !dto.getEmployeeIdString().isEmpty()) {
            employee = employeeRepository.findByEmployeeCode(dto.getEmployeeIdString())
                    .orElseGet(() -> employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found with code: " + dto.getEmployeeIdString())));
        } else {
            employee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
        }

        DeathRequest request = DeathRequest.builder()
                .employee(employee)
                .employeeName(dto.getEmployeeName())
                .employeeIdString(dto.getEmployeeIdString())
                .employeePhone(dto.getEmployeePhone())
                .dateOfDeath(dto.getDateOfDeath())
                .natureOfDeath(dto.getNatureOfDeath())
                .requesterName(dto.getRequesterName())
                .requesterBranch(dto.getRequesterBranch())
                .requesterDesignation(dto.getRequesterDesignation())
                .requesterEmpId(dto.getRequesterEmpId())
                .requesterNic(dto.getRequesterNic())
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
        Nominee nominee = employee.getNominee();
        
        if (nominee == null) {
            nominee = nomineeRepository.findByEmployeeId(employee.getId())
                    .orElseGet(() -> {
                        Nominee n = new Nominee();
                        n.setEmployee(employee);
                        return n;
                    });
        }
        
        nominee.setNomineeName(dto.getNomineeName());
        nominee.setRelationship(dto.getNomineeRelationship());
        nominee.setNic(dto.getNomineeNic());
        nominee.setPhoneNo(dto.getNomineePhone());
        nominee.setAddress(dto.getNomineeAddress());
        nominee.setBankName(dto.getNomineeBank());
        nominee.setBankBranch(dto.getNomineeBranch());
        nominee.setAccountNumber(dto.getNomineeAccount());
        
        nomineeRepository.save(nominee);
    }

    @Transactional
    public DeathRequestDto updateRequest(Long id, DeathRequestDto dto) {
        DeathRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("DeathRequest not found with id: " + id));

        request.setEmployeeName(dto.getEmployeeName());
        request.setEmployeeIdString(dto.getEmployeeIdString());
        request.setEmployeePhone(dto.getEmployeePhone());
        request.setDateOfDeath(dto.getDateOfDeath());
        request.setNatureOfDeath(dto.getNatureOfDeath());
        request.setRequesterName(dto.getRequesterName());
        request.setRequesterBranch(dto.getRequesterBranch());
        request.setRequesterDesignation(dto.getRequesterDesignation());
        request.setRequesterEmpId(dto.getRequesterEmpId());
        request.setRequesterNic(dto.getRequesterNic());
        request.setAddress(dto.getAddress());
        request.setContactNumber(dto.getContactNumber());
        request.setSpecialRemark(dto.getSpecialRemark());
        request.setStatus(dto.getStatus());
        request.setNomineeName(dto.getNomineeName());
        request.setNomineeBank(dto.getNomineeBank());
        request.setNomineeBranch(dto.getNomineeBranch());
        request.setNomineeAccount(dto.getNomineeAccount());
        request.setDeathCertificateDoc(dto.getDeathCertificateDoc());
        request.setNomineeIdDoc(dto.getNomineeIdDoc());
        request.setRequestLetterDoc(dto.getRequestLetterDoc());
        request.setHrRemark(dto.getHrRemark());

        DeathRequest saved = repository.save(request);
        updateNominee(request.getEmployee(), dto);
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
        DeathRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus("REJECTED");
        request.setHrRemark(reason);
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
    public DeathRequestDto updateStatus(Long id, String status, String boardMeetingDate) {
        DeathRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus(status);
        if (boardMeetingDate != null) request.setBoardMeetingDate(boardMeetingDate);
        DeathRequest saved = repository.save(request);
        sendDeathStatusNotification(saved);
        return mapToDto(saved);
    }

    private DeathRequestDto mapToDto(DeathRequest request) {
        Nominee nominee = nomineeRepository.findByEmployeeId(request.getEmployee().getId()).orElse(null);
        return DeathRequestDto.builder()
                .id(request.getId())
                .employeeId(request.getEmployee().getId())
                .employeeIdString(request.getEmployeeIdString())
                .employeeName(request.getEmployeeName())
                .employeePhone(request.getEmployeePhone() != null ? request.getEmployeePhone() : request.getEmployee().getPhoneNumber())
                .epfNumber(request.getEmployee().getEpfNumber())
                .dateOfDeath(request.getDateOfDeath())
                .natureOfDeath(request.getNatureOfDeath())
                .requesterName(request.getRequesterName())
                .requesterBranch(request.getRequesterBranch())
                .requesterDesignation(request.getRequesterDesignation())
                .requesterEmpId(request.getRequesterEmpId())
                .requesterNic(request.getRequesterNic())
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

    private void sendDeathStatusNotification(DeathRequest request) {
        String status = request.getStatus();
        if ("APPROVED".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status) ||
            "Board Approved".equalsIgnoreCase(status) || "Board Rejected".equalsIgnoreCase(status)) {
            
            String requesterEmail = null;
            if (request.getRequesterEmpId() != null && !request.getRequesterEmpId().trim().isEmpty()) {
                requesterEmail = employeeRepository.findByEmployeeCode(request.getRequesterEmpId().trim())
                        .map(Employee::getEmail)
                        .orElse(null);
            }
            if (requesterEmail != null && !requesterEmail.isEmpty()) {
                notificationService.sendDeathApplicationStatusUpdate(
                        request.getRequesterName(),
                        requesterEmail,
                        request.getEmployeeName(),
                        status,
                        request.getHrRemark()
                );
            }
        }
    }
}
