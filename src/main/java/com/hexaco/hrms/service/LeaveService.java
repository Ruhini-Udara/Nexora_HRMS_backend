package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.MaternityLeaveDto;
import com.hexaco.hrms.dto.OverseasLeaveDto;

import java.util.List;
import java.util.Optional;

public interface LeaveService {
    
    // Overseas Leave Methods
    OverseasLeaveDto submitOverseasLeave(OverseasLeaveDto requestedLeave);
    Optional<OverseasLeaveDto> getOverseasLeaveById(Long id);
    List<OverseasLeaveDto> getAllOverseasLeaves();
    List<OverseasLeaveDto> getOverseasLeavesByStatus(String status);
    List<OverseasLeaveDto> getOverseasLeavesByEmployeeId(Long employeeId);
    
    // Maternity Leave Methods
    MaternityLeaveDto submitMaternityLeave(MaternityLeaveDto requestedLeave);
    Optional<MaternityLeaveDto> getMaternityLeaveById(Long id);
    List<MaternityLeaveDto> getAllMaternityLeaves();
    List<MaternityLeaveDto> getMaternityLeavesByStatus(String status);
    List<MaternityLeaveDto> getMaternityLeavesByEmployeeId(Long employeeId);
}
