package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.TransferRequestDto;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.TransferRequest;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.repository.TransferRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferRequestService {

    // Service for handling transfer requests
    private final TransferRequestRepository transferRequestRepository;

    private final EmployeeRepository employeeRepository;

    @Transactional
    public TransferRequestDto createRequest(TransferRequestDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        TransferRequest request = TransferRequest.builder()
                .employee(employee)
                .currentBranch(dto.getCurrentBranch())
                .targetBranch(dto.getTargetBranch())
                .transferType(dto.getTransferType())
                .reason(dto.getReason())
                .requestDate(dto.getRequestDate())
                .expectedDate(dto.getExpectedDate())
                .status(dto.getStatus() == null ? "SUBMITTED" : dto.getStatus())
                .justificationDocumentPath(dto.getJustificationDocumentPath())
                .proofDocumentPath(dto.getProofDocumentPath())
                .build();

        TransferRequest saved = transferRequestRepository.save(request);
        return mapToDto(saved);
    }

    public List<TransferRequestDto> getAllRequests() {
        return transferRequestRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<TransferRequestDto> getRequestsByEmployee(Long employeeId) {
        return transferRequestRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransferRequestDto updateStatus(Long id, String status, String remarks, String boardMeetingDate) {
        TransferRequest request = transferRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer request not found"));
        request.setStatus(status);
        if (remarks != null) request.setHrRemark(remarks);
        if (boardMeetingDate != null) request.setBoardMeetingDate(boardMeetingDate);
        return mapToDto(transferRequestRepository.save(request));
    }

    @Transactional
    public TransferRequestDto updateRequest(Long id, TransferRequestDto dto) {
        TransferRequest request = transferRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer request not found"));
        
        request.setCurrentBranch(dto.getCurrentBranch());
        request.setTargetBranch(dto.getTargetBranch());
        request.setTransferType(dto.getTransferType());
        request.setReason(dto.getReason());
        request.setExpectedDate(dto.getExpectedDate());
        request.setStatus(dto.getStatus() == null ? request.getStatus() : dto.getStatus());
        request.setJustificationDocumentPath(dto.getJustificationDocumentPath());
        request.setProofDocumentPath(dto.getProofDocumentPath());
        
        return mapToDto(transferRequestRepository.save(request));
    }

    private TransferRequestDto mapToDto(TransferRequest request) {
        return TransferRequestDto.builder()
                .id(request.getId())
                .employeeId(request.getEmployee().getId())
                .employeeName(request.getEmployee().getFullName())
                .epfNumber(request.getEmployee().getEpfNumber())
                .designation(request.getEmployee().getDesignation() != null ? request.getEmployee().getDesignation().getDesignationName() : "")
                .branch(request.getEmployee().getDepartment())
                .currentBranch(request.getCurrentBranch())
                .targetBranch(request.getTargetBranch())
                .transferType(request.getTransferType())
                .reason(request.getReason())
                .requestDate(request.getRequestDate())
                .expectedDate(request.getExpectedDate())
                .status(request.getStatus())
                .hrRemark(request.getHrRemark())
                .boardMeetingDate(request.getBoardMeetingDate())
                .justificationDocumentPath(request.getJustificationDocumentPath())
                .proofDocumentPath(request.getProofDocumentPath())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
