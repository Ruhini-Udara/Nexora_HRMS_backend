package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.models.MaternityLeave;
import com.hexaco.hrms.models.OverseasLeave;
import com.hexaco.hrms.repository.MaternityLeaveRepository;
import com.hexaco.hrms.repository.OverseasLeaveRepository;
import com.hexaco.hrms.repository.UserAccountRepository;
import com.hexaco.hrms.models.UserAccount;
import com.hexaco.hrms.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final OverseasLeaveRepository overseasLeaveRepository;
    private final MaternityLeaveRepository maternityLeaveRepository;
    private final com.hexaco.hrms.repository.EmployeeRepository employeeRepository;
    private final UserAccountRepository userAccountRepository;
    
    // Status Constants to avoid hard-coding
    private static final String STATUS_PENDING_HR = "PENDING_HR_APPROVAL";
    private static final String STATUS_PENDING_ADMIN = "PENDING_ADMIN_APPROVAL";

    @Override
    public OverseasLeave submitOverseasLeave(OverseasLeave requestedLeave) {
        // Smart Routing Logic:
        // Rationale: If the employee submitting the request already has an HR role,
        // we skip the 'PENDING_HR_APPROVAL' step and move directly to Admin approval.
        if (requestedLeave.getEmployee() != null && requestedLeave.getEmployee().getId() != null) {
            boolean isHr = false;
            java.util.List<UserAccount> accounts = userAccountRepository.findByEmployeeId(requestedLeave.getEmployee().getId());
            for (UserAccount acc : accounts) {
                if (acc.getRole() != null) {
                    String role = acc.getRole().getRoleName();
                    if ("ROLE_HR".equalsIgnoreCase(role) || "HR".equalsIgnoreCase(role)) {
                        isHr = true;
                        break;
                    }
                }
            }
            
            if (isHr) {
                requestedLeave.setStatus(STATUS_PENDING_ADMIN);
            } else {
                requestedLeave.setStatus(STATUS_PENDING_HR);
            }
        } else {
            requestedLeave.setStatus(STATUS_PENDING_HR);
        }
        return overseasLeaveRepository.save(requestedLeave);
    }

    @Override
    public Optional<OverseasLeave> getOverseasLeaveById(Long id) {
        return overseasLeaveRepository.findById(id);
    }

    @Override
    public List<OverseasLeave> getAllOverseasLeaves() {
        return overseasLeaveRepository.findAll();
    }

    @Override
    public List<OverseasLeave> getOverseasLeavesByStatus(String status) {
        return overseasLeaveRepository.findByStatus(status);
    }

    @Override
    public List<OverseasLeave> getOverseasLeavesByEmployeeId(Long employeeId) {
        return overseasLeaveRepository.findByEmployeeId(employeeId);
    }

    @Override
    public MaternityLeave submitMaternityLeave(MaternityLeave requestedLeave) {
        // Smart Routing Logic:
        // Rationale: Similar to Overseas, HR employees skip their own verification layer.
        if (requestedLeave.getEmployee() != null && requestedLeave.getEmployee().getId() != null) {
            boolean isHr = false;
            java.util.List<UserAccount> accounts = userAccountRepository.findByEmployeeId(requestedLeave.getEmployee().getId());
            for (UserAccount acc : accounts) {
                if (acc.getRole() != null) {
                    String role = acc.getRole().getRoleName();
                    if ("ROLE_HR".equalsIgnoreCase(role) || "HR".equalsIgnoreCase(role)) {
                        isHr = true;
                        break;
                    }
                }
            }
            
            if (isHr) {
                requestedLeave.setStatus(STATUS_PENDING_ADMIN);
            } else {
                requestedLeave.setStatus(STATUS_PENDING_HR);
            }
        } else {
            requestedLeave.setStatus(STATUS_PENDING_HR);
        }
        return maternityLeaveRepository.save(requestedLeave);
    }

    @Override
    public Optional<MaternityLeave> getMaternityLeaveById(Long id) {
        return maternityLeaveRepository.findById(id);
    }

    @Override
    public List<MaternityLeave> getAllMaternityLeaves() {
        return maternityLeaveRepository.findAll();
    }

    @Override
    public List<MaternityLeave> getMaternityLeavesByStatus(String status) {
        return maternityLeaveRepository.findByStatus(status);
    }

    @Override
    public List<MaternityLeave> getMaternityLeavesByEmployeeId(Long employeeId) {
        return maternityLeaveRepository.findByEmployeeId(employeeId);
    }
}
