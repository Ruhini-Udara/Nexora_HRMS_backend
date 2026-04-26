package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.DeathRequestDto;
import com.hexaco.hrms.models.DeathRequest;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.repository.DeathRequestRepository;
import com.hexaco.hrms.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeathRequestService {

    private final DeathRequestRepository repository;
    private final EmployeeRepository employeeRepository;

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

    public DeathRequestDto createRequest(DeathRequestDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        DeathRequest request = DeathRequest.builder()
                .employee(employee)
                .employeeName(dto.getEmployeeName())
                .dateOfDeath(dto.getDateOfDeath())
                .natureOfDeath(dto.getNatureOfDeath())
                .requesterName(dto.getRequesterName())
                .requesterBranch(dto.getRequesterBranch())
                .requesterDesignation(dto.getRequesterDesignation())
                .requesterEmpId(dto.getRequesterEmpId())
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

        return mapToDto(repository.save(request));
    }

    public DeathRequestDto updateRequest(Long id, DeathRequestDto dto) {
        DeathRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("DeathRequest not found with id: " + id));

        request.setEmployeeName(dto.getEmployeeName());
        request.setDateOfDeath(dto.getDateOfDeath());
        request.setNatureOfDeath(dto.getNatureOfDeath());
        request.setRequesterName(dto.getRequesterName());
        request.setRequesterBranch(dto.getRequesterBranch());
        request.setRequesterDesignation(dto.getRequesterDesignation());
        request.setRequesterEmpId(dto.getRequesterEmpId());
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

        return mapToDto(repository.save(request));
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
        return mapToDto(repository.save(request));
    }

    public DeathRequestDto submitToAdmin(Long id) {
        DeathRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus("PENDING_ADMIN");
        return mapToDto(repository.save(request));
    }

    private DeathRequestDto mapToDto(DeathRequest request) {
        return DeathRequestDto.builder()
                .id(request.getId())
                .employeeId(request.getEmployee().getId())
                .employeeName(request.getEmployeeName())
                .epfNumber(request.getEmployee().getEpfNumber())
                .dateOfDeath(request.getDateOfDeath())
                .natureOfDeath(request.getNatureOfDeath())
                .requesterName(request.getRequesterName())
                .requesterBranch(request.getRequesterBranch())
                .requesterDesignation(request.getRequesterDesignation())
                .requesterEmpId(request.getRequesterEmpId())
                .address(request.getAddress())
                .contactNumber(request.getContactNumber())
                .specialRemark(request.getSpecialRemark())
                .status(request.getStatus())
                .nomineeName(request.getNomineeName())
                .nomineeBank(request.getNomineeBank())
                .nomineeBranch(request.getNomineeBranch())
                .nomineeAccount(request.getNomineeAccount())
                .deathCertificateDoc(request.getDeathCertificateDoc())
                .nomineeIdDoc(request.getNomineeIdDoc())
                .requestLetterDoc(request.getRequestLetterDoc())
                .hrRemark(request.getHrRemark())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
