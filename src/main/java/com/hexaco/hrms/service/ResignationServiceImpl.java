package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.ResignationDto;
import com.hexaco.hrms.models.Resignation;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.repository.ResignationRepository;
import com.hexaco.hrms.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResignationServiceImpl implements ResignationService {

    private final ResignationRepository repository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public ResignationDto createResignation(ResignationDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + dto.getEmployeeId()));

        Resignation resignation = Resignation.builder()
                .employee(employee)
                .employeeName(dto.getEmployeeName() != null ? dto.getEmployeeName() : employee.getFullName())
                .epfNumber(dto.getEpfNumber() != null ? dto.getEpfNumber() : employee.getEpfNumber())
                .designation(dto.getDesignation() != null ? dto.getDesignation() : (employee.getDesignation() != null ? employee.getDesignation().getDesignationName() : null))
                .branch(dto.getBranch() != null ? dto.getBranch() : employee.getDepartment())
                .resignationDate(dto.getResignationDate())
                .lastWorkingDate(dto.getLastWorkingDate())
                .obligationDetails(dto.getObligationDetails())
                .reason(dto.getReason())
                .specialRemark(dto.getSpecialRemark())
                .status(dto.getStatus() != null ? dto.getStatus() : "NEW")
                .resignationLetterDoc(dto.getResignationLetterDoc())
                .clearanceLetterDoc(dto.getClearanceLetterDoc())
                .handoverChecklistDoc(dto.getHandoverChecklistDoc())
                .hrRemark(dto.getHrRemark())
                .directorRemark(dto.getDirectorRemark())
                .boardMeetingDate(dto.getBoardMeetingDate())
                .build();

        Resignation saved = repository.save(resignation);
        return mapToDto(saved);
    }

    @Override
    public List<ResignationDto> getAllResignations() {
        return repository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResignationDto> getResignationsByEmployeeId(Long employeeId) {
        return repository.findByEmployeeId(employeeId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ResignationDto updateResignationStatus(Long id, String status, String remarks, String boardMeetingDate) {
        Resignation resignation = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resignation request not found with id: " + id));
        
        resignation.setStatus(status);
        if (remarks != null) {
            // Depending on status, we might update hrRemark or directorRemark
            if (status.contains("REJECTED") || status.equals("VERIFIED_BY_HR")) {
                resignation.setHrRemark(remarks);
            } else {
                resignation.setDirectorRemark(remarks);
            }
        }
        if (boardMeetingDate != null) {
            resignation.setBoardMeetingDate(boardMeetingDate);
        }
        
        Resignation updated = repository.save(resignation);
        return mapToDto(updated);
    }

    @Override
    public ResignationDto getResignationById(Long id) {
        Resignation resignation = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resignation request not found with id: " + id));
        return mapToDto(resignation);
    }

    private ResignationDto mapToDto(Resignation resignation) {
        return ResignationDto.builder()
                .id(resignation.getId())
                .employeeId(resignation.getEmployee().getId())
                .employeeName(resignation.getEmployeeName())
                .epfNumber(resignation.getEpfNumber())
                .designation(resignation.getDesignation())
                .branch(resignation.getBranch())
                .resignationDate(resignation.getResignationDate())
                .lastWorkingDate(resignation.getLastWorkingDate())
                .obligationDetails(resignation.getObligationDetails())
                .reason(resignation.getReason())
                .specialRemark(resignation.getSpecialRemark())
                .status(resignation.getStatus())
                .resignationLetterDoc(resignation.getResignationLetterDoc())
                .clearanceLetterDoc(resignation.getClearanceLetterDoc())
                .handoverChecklistDoc(resignation.getHandoverChecklistDoc())
                .hrRemark(resignation.getHrRemark())
                .directorRemark(resignation.getDirectorRemark())
                .boardMeetingDate(resignation.getBoardMeetingDate())
                .createdAt(resignation.getCreatedAt())
                .updatedAt(resignation.getUpdatedAt())
                .build();
    }
}
