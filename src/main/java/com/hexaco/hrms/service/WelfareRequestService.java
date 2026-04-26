package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.WelfareRequestDto;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.WelfareRequest;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.repository.WelfareRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class WelfareRequestService {

    private final WelfareRequestRepository welfareRequestRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public WelfareRequestDto createRequest(WelfareRequestDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        WelfareRequest request = WelfareRequest.builder()
                .employee(employee)
                .requestDate(LocalDate.now())
                .welfareType(dto.getWelfareType())
                .amount(dto.getAmount())
                .status(dto.getStatus() == null ? "Pending" : dto.getStatus())
                .remarks(dto.getRemarks())
                .supportingDocument(dto.getSupportingDocument())
                .build();

        WelfareRequest saved = welfareRequestRepository.save(request);
        return mapToDto(saved);
    }

    public List<WelfareRequestDto> getAllRequests() {
        return welfareRequestRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<WelfareRequestDto> getRequestsByEmployee(Long employeeId) {
        return welfareRequestRepository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public WelfareRequestDto updateStatus(Long id, String status, String remarks) {
        WelfareRequest request = welfareRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Welfare request not found"));
        request.setStatus(status);
        if (remarks != null) {
            request.setRemarks(remarks);
        }
        return mapToDto(welfareRequestRepository.save(request));
    }

    @Transactional
    public WelfareRequestDto updateRequest(Long id, WelfareRequestDto dto) {
        WelfareRequest request = welfareRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Welfare request not found"));
        request.setRequestDate(LocalDate.now());
        request.setWelfareType(dto.getWelfareType());
        request.setAmount(dto.getAmount());
        request.setRemarks(dto.getRemarks());
        request.setSupportingDocument(dto.getSupportingDocument());
        request.setStatus(dto.getStatus() == null ? request.getStatus() : dto.getStatus());
        return mapToDto(welfareRequestRepository.save(request));
    }

    private WelfareRequestDto mapToDto(WelfareRequest request) {
        String initials = "";
        if (request.getEmployee().getFullName() != null && request.getEmployee().getSurname() != null) {
            initials = request.getEmployee().getFullName().substring(0, 1) + request.getEmployee().getSurname().substring(0, 1);
        }

        return WelfareRequestDto.builder()
                .id(request.getId())
                .employeeId(request.getEmployee().getId())
                .employeeName(request.getEmployee().getFullName())
                .email(request.getEmployee().getEmail())
                .role(request.getEmployee().getDesignation() != null ? request.getEmployee().getDesignation().getDesignationName() : "")
                .initials(initials.toUpperCase())
                .requestDate(request.getRequestDate())
                .welfareType(request.getWelfareType())
                .amount(request.getAmount())
                .status(request.getStatus())
                .remarks(request.getRemarks())
                .supportingDocument(request.getSupportingDocument())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
