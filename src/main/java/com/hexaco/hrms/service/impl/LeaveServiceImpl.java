package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.MaternityLeaveDto;
import com.hexaco.hrms.dto.OverseasLeaveDto;
import com.hexaco.hrms.dto.NormalLeaveDto;
import com.hexaco.hrms.dto.LeaveImpactDto;
import com.hexaco.hrms.models.*;
import com.hexaco.hrms.repository.*;
import com.hexaco.hrms.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final OverseasLeaveRepository overseasLeaveRepository;
    private final MaternityLeaveRepository maternityLeaveRepository;
    private final NormalLeaveRepository normalLeaveRepository;
    private final EmployeeRepository employeeRepository;
    private final UserAccountRepository userAccountRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    
    // Status Constants to avoid hard-coding
    private static final String STATUS_PENDING_HR = "PENDING_HR_APPROVAL";
    private static final String STATUS_PENDING_ADMIN = "PENDING_ADMIN_APPROVAL";

    @Override
    public java.util.List<Long> getEmployeesOnLeave(java.time.LocalDate date) {
        return leaveRequestRepository.findApprovedLeaveEmployeeIdsByDate(date);
    }

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

    @Override
    public NormalLeaveDto submitNormalLeave(NormalLeaveDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
                .orElseThrow(() -> new RuntimeException("LeaveType not found"));

        NormalLeave requestedLeave = NormalLeave.builder()
                .branch(dto.getBranch())
                .contactNumber(dto.getContactNumber())
                .build();

        // Map parent class fields
        requestedLeave.setEmployee(employee);
        requestedLeave.setLeaveType(leaveType);
        requestedLeave.setFromDate(dto.getFromDate());
        requestedLeave.setEndDate(dto.getEndDate());
        requestedLeave.setTotalDays(dto.getTotalDays());
        requestedLeave.setReason(dto.getReason());

        // Smart Routing Logic
        if (dto.getTotalDays() >= 3) {
            requestedLeave.setStatus(STATUS_PENDING_HR);
        } else {
            requestedLeave.setStatus("PENDING_SUPERVISOR_APPROVAL");
        }

        return mapToNormalDto(normalLeaveRepository.save(requestedLeave));
    }

    @Override
    public Optional<NormalLeaveDto> getNormalLeaveById(Long id) {
        return normalLeaveRepository.findById(id).map(this::mapToNormalDto);
    }

    @Override
    public List<NormalLeaveDto> getAllNormalLeaves() {
        return normalLeaveRepository.findAll().stream().map(this::mapToNormalDto).collect(Collectors.toList());
    }

    @Override
    public List<NormalLeaveDto> getNormalLeavesByStatus(String status) {
        return normalLeaveRepository.findByStatus(status).stream().map(this::mapToNormalDto).collect(Collectors.toList());
    }

    @Override
    public List<NormalLeaveDto> getNormalLeavesByEmployeeId(Long employeeId) {
        return normalLeaveRepository.findByEmployeeId(employeeId).stream().map(this::mapToNormalDto).collect(Collectors.toList());
    }

    public LeaveImpactDto getOverseasLeaveImpact(Long leaveId) {
        OverseasLeave leave = overseasLeaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Overseas leave not found"));
        return calculateLeaveImpact(leave.getEmployee(), leave.getFromDate(), leave.getEndDate(), leave.getTotalDays(), leave.getId());
    }

    @Override
    public LeaveImpactDto getMaternityLeaveImpact(Long leaveId) {
        MaternityLeave leave = maternityLeaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Maternity leave not found"));
        return calculateLeaveImpact(leave.getEmployee(), leave.getFromDate(), leave.getEndDate(), leave.getTotalDays(), leave.getId());
    }

    @Override
    public LeaveImpactDto getNormalLeaveImpact(Long leaveId) {
        NormalLeave leave = normalLeaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Normal leave not found"));
        return calculateLeaveImpact(leave.getEmployee(), leave.getFromDate(), leave.getEndDate(), leave.getTotalDays(), leave.getId());
    }

    private LeaveImpactDto calculateLeaveImpact(Employee employee, java.time.LocalDate fromDate, java.time.LocalDate endDate, int totalDays, Long excludeLeaveId) {
        String department = employee.getDepartment();
        if (department == null) {
            department = "";
        }

        long departmentEmployees = employeeRepository.findByDepartmentIgnoreCase(department).size();
        
        long overlappingOverseas = overseasLeaveRepository.countOverlappingLeavesByDepartment(department, fromDate, endDate, excludeLeaveId);
        long overlappingMaternity = maternityLeaveRepository.countOverlappingLeavesByDepartment(department, fromDate, endDate, excludeLeaveId);
        long overlappingNormal = normalLeaveRepository.countOverlappingLeavesByDepartment(department, fromDate, endDate, excludeLeaveId);
        
        long alreadyOnLeave = overlappingOverseas + overlappingMaternity + overlappingNormal;
        long availableAfterApproval = departmentEmployees - alreadyOnLeave - 1; // subtract 1 for the requesting employee

        if (availableAfterApproval < 0) availableAfterApproval = 0;
        if (departmentEmployees == 0) departmentEmployees = 1; // prevent division by zero

        double availabilityPercentage = ((double) availableAfterApproval / departmentEmployees) * 100.0;
        
        String riskLevel = "High Risk";
        if (availabilityPercentage >= 80) {
            riskLevel = "Low Risk";
        } else if (availabilityPercentage >= 60) {
            riskLevel = "Medium Risk";
        }

        return LeaveImpactDto.builder()
                .employeeName(formatEmployeeName(employee))
                .department(department.isEmpty() ? "Unassigned" : department)
                .leaveDuration(totalDays)
                .departmentEmployees(departmentEmployees)
                .alreadyOnLeave(alreadyOnLeave)
                .availableAfterApproval(availableAfterApproval)
                .availabilityPercentage(availabilityPercentage)
                .riskLevel(riskLevel)
                .build();
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
                .employeeName(formatEmployeeName(leave.getEmployee()))
                .employeeCode(leave.getEmployee().getEmployeeCode())
                .epfNumber(leave.getEmployee().getEpfNumber())
                .department(leave.getEmployee().getDepartment())
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
                .employeeName(formatEmployeeName(leave.getEmployee()))
                .employeeCode(leave.getEmployee().getEmployeeCode())
                .epfNumber(leave.getEmployee().getEpfNumber())
                .department(leave.getEmployee().getDepartment())
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

    private NormalLeaveDto mapToNormalDto(NormalLeave leave) {
        // Look up leave balance for the employee in the current year
        int currentYear = LocalDate.now().getYear();
        Optional<LeaveBalance> balanceOpt = leaveBalanceRepository
                .findByEmployeeIdAndYear(leave.getEmployee().getId(), currentYear);

        int annualRemaining = 0;
        int sickRemaining   = 0;  // medical leave shown as "Sick" on supervisor UI
        int casualRemaining = 0;

        if (balanceOpt.isPresent()) {
            LeaveBalance lb = balanceOpt.get();
            int annualQuota  = lb.getAnnualLeaveQuota()  != null ? lb.getAnnualLeaveQuota()  : 0;
            int annualUsed   = lb.getAnnualLeaveUsed()   != null ? lb.getAnnualLeaveUsed()   : 0;
            int medicalQuota = lb.getMedicalLeaveQuota() != null ? lb.getMedicalLeaveQuota() : 0;
            int medicalUsed  = lb.getMedicalLeaveUsed()  != null ? lb.getMedicalLeaveUsed()  : 0;
            int casualQuota  = lb.getCasualLeaveQuota()  != null ? lb.getCasualLeaveQuota()  : 0;
            int casualUsed   = lb.getCasualLeaveUsed()   != null ? lb.getCasualLeaveUsed()   : 0;

            annualRemaining = Math.max(0, annualQuota  - annualUsed);
            sickRemaining   = Math.max(0, medicalQuota - medicalUsed);
            casualRemaining = Math.max(0, casualQuota  - casualUsed);
        }

        return NormalLeaveDto.builder()
                .id(leave.getId())
                .employeeId(leave.getEmployee().getId())
                .employeeName(formatEmployeeName(leave.getEmployee()))
                .employeeCode(leave.getEmployee().getEmployeeCode())
                .epfNumber(leave.getEmployee().getEpfNumber())
                .department(leave.getEmployee().getDepartment())
                .leaveTypeId(leave.getLeaveType().getId())
                .leaveTypeName(leave.getLeaveType().getLeaveTypeName())
                .fromDate(leave.getFromDate())
                .endDate(leave.getEndDate())
                .totalDays(leave.getTotalDays())
                .reason(leave.getReason())
                .status(leave.getStatus())
                .branch(leave.getBranch())
                .contactNumber(leave.getContactNumber())
                .createdAt(leave.getCreatedAt())
                .updatedAt(leave.getUpdatedAt())
                .annualLeaveRemaining(annualRemaining)
                .sickLeaveRemaining(sickRemaining)
                .casualLeaveRemaining(casualRemaining)
                .build();
    }

    private String formatEmployeeName(Employee employee) {
        if (employee == null) return "N/A";
        String fullName = employee.getFullName();
        String surname = employee.getSurname();
        
        if (fullName == null) return surname != null ? surname : "N/A";
        if (surname == null) return fullName;
        
        // If fullName already contains the surname (case insensitive), just use fullName
        if (fullName.toLowerCase().contains(surname.toLowerCase())) {
            return fullName;
        }
        
        return fullName + " " + surname;
    }
}
