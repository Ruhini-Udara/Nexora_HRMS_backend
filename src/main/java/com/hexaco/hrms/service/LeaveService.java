package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.MaternityLeaveDto;
import com.hexaco.hrms.dto.OverseasLeaveDto;
import com.hexaco.hrms.dto.NormalLeaveDto;
import com.hexaco.hrms.dto.LeaveImpactDto;

import java.util.List;
import java.util.Optional;

public interface LeaveService {
    java.util.List<Long> getEmployeesOnLeave(java.time.LocalDate date);
    
    // Overseas Leave Methods
    OverseasLeaveDto submitOverseasLeave(OverseasLeaveDto requestedLeave);
    Optional<OverseasLeaveDto> getOverseasLeaveById(Long id);
    List<OverseasLeaveDto> getAllOverseasLeaves();
    List<OverseasLeaveDto> getOverseasLeavesByStatus(String status);
    List<OverseasLeaveDto> getOverseasLeavesByEmployeeId(Long employeeId);
    LeaveImpactDto getOverseasLeaveImpact(Long leaveId);
    
    // Maternity Leave Methods
    MaternityLeaveDto submitMaternityLeave(MaternityLeaveDto requestedLeave);
    Optional<MaternityLeaveDto> getMaternityLeaveById(Long id);
    List<MaternityLeaveDto> getAllMaternityLeaves();
    List<MaternityLeaveDto> getMaternityLeavesByStatus(String status);
    List<MaternityLeaveDto> getMaternityLeavesByEmployeeId(Long employeeId);
    // Normal Leave Methods
    NormalLeaveDto submitNormalLeave(NormalLeaveDto requestedLeave);
    Optional<NormalLeaveDto> getNormalLeaveById(Long id);
    List<NormalLeaveDto> getAllNormalLeaves();
    List<NormalLeaveDto> getNormalLeavesByStatus(String status);
    List<NormalLeaveDto> getNormalLeavesByEmployeeId(Long employeeId);

    LeaveImpactDto getMaternityLeaveImpact(Long leaveId);
    LeaveImpactDto getNormalLeaveImpact(Long leaveId);
}
