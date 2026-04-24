package com.hexaco.hrms.service;

import com.hexaco.hrms.models.MaternityLeave;
import com.hexaco.hrms.models.OverseasLeave;

import java.util.List;
import java.util.Optional;

public interface LeaveService {
    
    // Overseas Leave Methods
    OverseasLeave submitOverseasLeave(OverseasLeave requestedLeave);
    Optional<OverseasLeave> getOverseasLeaveById(Long id);
    List<OverseasLeave> getAllOverseasLeaves();
    List<OverseasLeave> getOverseasLeavesByStatus(String status);
    List<OverseasLeave> getOverseasLeavesByEmployeeId(Long employeeId);
    
    // Maternity Leave Methods
    MaternityLeave submitMaternityLeave(MaternityLeave requestedLeave);
    Optional<MaternityLeave> getMaternityLeaveById(Long id);
    List<MaternityLeave> getAllMaternityLeaves();
    List<MaternityLeave> getMaternityLeavesByStatus(String status);
    List<MaternityLeave> getMaternityLeavesByEmployeeId(Long employeeId);
}
