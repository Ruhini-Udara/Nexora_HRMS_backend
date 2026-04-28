package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.MaternityLeaveDto;
import com.hexaco.hrms.dto.OverseasLeaveDto;
import com.hexaco.hrms.models.*;
import com.hexaco.hrms.repository.*;
import com.hexaco.hrms.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final OverseasLeaveRepository overseasLeaveRepository;
    private final MaternityLeaveRepository maternityLeaveRepository;
    private final EmployeeRepository employeeRepository;
    private final UserAccountRepository userAccountRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    
    // Status Constants to avoid hard-coding
    private static final String STATUS_PENDING_HR = "PENDING_HR_APPROVAL";
    private static final String STATUS_PENDING_ADMIN = "PENDING_ADMIN_APPROVAL";

    @Override
    public OverseasLeaveDto submitOverseasLeave(OverseasLeaveDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
                .orElseThrow(() -> new RuntimeException("LeaveType not found"));

        OverseasLeave requestedLeave = OverseasLeave.builder()
                .passportNumber(dto.getPassportNumber())
                .passportExpDate(dto.getPassportExpDate())
                .branch(dto.getBranch())
                .contactNumber(dto.getContactNumber())
                .email(dto.getEmail())
                .specialRemark(dto.getSpecialRemark())
                .build();

        // Map parent class fields
        requestedLeave.setEmployee(employee);
        requestedLeave.setLeaveType(leaveType);
        requestedLeave.setFromDate(dto.getFromDate());
        requestedLeave.setEndDate(dto.getEndDate());
        requestedLeave.setTotalDays(dto.getTotalDays());
        requestedLeave.setReason(dto.getReason());

        // Smart Routing Logic
        if (isHrEmployee(employee.getId())) {
            requestedLeave.setStatus(STATUS_PENDING_ADMIN);
        } else {
            requestedLeave.setStatus(STATUS_PENDING_HR);
        }

        return mapToOverseasDto(overseasLeaveRepository.save(requestedLeave));
    }

    @Override
    public Optional<OverseasLeaveDto> getOverseasLeaveById(Long id) {
        return overseasLeaveRepository.findById(id).map(this::mapToOverseasDto);
    }

    @Override
    public List<OverseasLeaveDto> getAllOverseasLeaves() {
        return overseasLeaveRepository.findAll().stream().map(this::mapToOverseasDto).collect(Collectors.toList());
    }

    @Override
    public List<OverseasLeaveDto> getOverseasLeavesByStatus(String status) {
        return overseasLeaveRepository.findByStatus(status).stream().map(this::mapToOverseasDto).collect(Collectors.toList());
    }

    @Override
    public List<OverseasLeaveDto> getOverseasLeavesByEmployeeId(Long employeeId) {
        return overseasLeaveRepository.findByEmployeeId(employeeId).stream().map(this::mapToOverseasDto).collect(Collectors.toList());
    }

    @Override
    public MaternityLeaveDto submitMaternityLeave(MaternityLeaveDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
                .orElseThrow(() -> new RuntimeException("LeaveType not found"));

        MaternityLeave requestedLeave = MaternityLeave.builder()
                .childNumber(dto.getChildNumber())
                .employeeType(dto.getEmployeeType())
                .branch(dto.getBranch())
                .contactNumber(dto.getContactNumber())
                .email(dto.getEmail())
                .specialRemark(dto.getSpecialRemark())
                .build();

        // Map parent class fields
        requestedLeave.setEmployee(employee);
        requestedLeave.setLeaveType(leaveType);
        requestedLeave.setFromDate(dto.getFromDate());
        requestedLeave.setEndDate(dto.getEndDate());
        requestedLeave.setTotalDays(dto.getTotalDays());
        requestedLeave.setReason(dto.getReason());

        // Smart Routing Logic
        if (isHrEmployee(employee.getId())) {
            requestedLeave.setStatus(STATUS_PENDING_ADMIN);
        } else {
            requestedLeave.setStatus(STATUS_PENDING_HR);
        }

        return mapToMaternityDto(maternityLeaveRepository.save(requestedLeave));
    }

    @Override
    public Optional<MaternityLeaveDto> getMaternityLeaveById(Long id) {
        return maternityLeaveRepository.findById(id).map(this::mapToMaternityDto);
    }

    @Override
    public List<MaternityLeaveDto> getAllMaternityLeaves() {
        return maternityLeaveRepository.findAll().stream().map(this::mapToMaternityDto).collect(Collectors.toList());
    }

    @Override
    public List<MaternityLeaveDto> getMaternityLeavesByStatus(String status) {
        return maternityLeaveRepository.findByStatus(status).stream().map(this::mapToMaternityDto).collect(Collectors.toList());
    }

    @Override
    public List<MaternityLeaveDto> getMaternityLeavesByEmployeeId(Long employeeId) {
        return maternityLeaveRepository.findByEmployeeId(employeeId).stream().map(this::mapToMaternityDto).collect(Collectors.toList());
    }

    private boolean isHrEmployee(Long employeeId) {
        List<UserAccount> accounts = userAccountRepository.findByEmployeeId(employeeId);
        for (UserAccount acc : accounts) {
            if (acc.getRole() != null) {
                String role = acc.getRole().getRoleName();
                if ("ROLE_HR".equalsIgnoreCase(role) || "HR".equalsIgnoreCase(role)) {
                    return true;
                }
            }
        }
        return false;
    }

    private OverseasLeaveDto mapToOverseasDto(OverseasLeave leave) {
        return OverseasLeaveDto.builder()
                .id(leave.getId())
                .employeeId(leave.getEmployee().getId())
                .employeeName(leave.getEmployee().getFullName() + " " + leave.getEmployee().getSurname())
                .epfNumber(leave.getEmployee().getEpfNumber())
                .leaveTypeId(leave.getLeaveType().getId())
                .leaveTypeName(leave.getLeaveType().getLeaveTypeName())
                .fromDate(leave.getFromDate())
                .endDate(leave.getEndDate())
                .totalDays(leave.getTotalDays())
                .reason(leave.getReason())
                .status(leave.getStatus())
                .passportNumber(leave.getPassportNumber())
                .passportExpDate(leave.getPassportExpDate())
                .branch(leave.getBranch())
                .contactNumber(leave.getContactNumber())
                .email(leave.getEmail())
                .specialRemark(leave.getSpecialRemark())
                .createdAt(leave.getCreatedAt())
                .updatedAt(leave.getUpdatedAt())
                .build();
    }

    private MaternityLeaveDto mapToMaternityDto(MaternityLeave leave) {
        return MaternityLeaveDto.builder()
                .id(leave.getId())
                .employeeId(leave.getEmployee().getId())
                .employeeName(leave.getEmployee().getFullName() + " " + leave.getEmployee().getSurname())
                .epfNumber(leave.getEmployee().getEpfNumber())
                .leaveTypeId(leave.getLeaveType().getId())
                .leaveTypeName(leave.getLeaveType().getLeaveTypeName())
                .fromDate(leave.getFromDate())
                .endDate(leave.getEndDate())
                .totalDays(leave.getTotalDays())
                .reason(leave.getReason())
                .status(leave.getStatus())
                .childNumber(leave.getChildNumber())
                .employeeType(leave.getEmployeeType())
                .branch(leave.getBranch())
                .contactNumber(leave.getContactNumber())
                .email(leave.getEmail())
                .specialRemark(leave.getSpecialRemark())
                .createdAt(leave.getCreatedAt())
                .updatedAt(leave.getUpdatedAt())
                .build();
    }
}
