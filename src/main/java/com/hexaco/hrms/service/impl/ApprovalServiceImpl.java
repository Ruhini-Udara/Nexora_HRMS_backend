package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.models.Approval;
import com.hexaco.hrms.models.MaternityLeave;
import com.hexaco.hrms.models.OverseasLeave;
import com.hexaco.hrms.repository.ApprovalRepository;
import com.hexaco.hrms.repository.MaternityLeaveRepository;
import com.hexaco.hrms.repository.OverseasLeaveRepository;
import com.hexaco.hrms.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final OverseasLeaveRepository overseasLeaveRepository;
    private final MaternityLeaveRepository maternityLeaveRepository;
    private final com.hexaco.hrms.service.NotificationService notificationService;

    @Override
    public Approval saveApproval(Approval approval) {
        // Automatically stamp the decision date before saving
        if (approval.getDecisionDate() == null) {
            approval.setDecisionDate(LocalDateTime.now());
        }
        
        Approval savedApproval = approvalRepository.save(approval);
        
        // --- Intelligent State Machine Logic ---
        if ("OVERSEAS_LEAVE".equals(approval.getRefType())) {
            Optional<OverseasLeave> leaveOpt = overseasLeaveRepository.findById(approval.getRefId());
            if (leaveOpt.isPresent()) {
                OverseasLeave leave = leaveOpt.get();
                String oldStatus = leave.getStatus();
                String newStatus = calculateNextStatus(oldStatus, approval.getDecision());
                leave.setStatus(newStatus);
                overseasLeaveRepository.save(leave);

                // Trigger Notification if status reached a final state or was rejected
                if ("APPROVED".equals(newStatus) || "REJECTED".equals(newStatus)) {
                    notificationService.sendLeaveStatusUpdate(
                        leave.getEmployee().getFullName(), 
                        leave.getEmail(),
                        leave.getContactNumber(),
                        "Overseas Leave", 
                        newStatus, 
                        approval.getRemark()
                    );
                }
            }
        } else if ("MATERNITY_LEAVE".equals(approval.getRefType())) {
            Optional<MaternityLeave> leaveOpt = maternityLeaveRepository.findById(approval.getRefId());
            if (leaveOpt.isPresent()) {
                MaternityLeave leave = leaveOpt.get();
                String oldStatus = leave.getStatus();
                String newStatus = calculateNextStatus(oldStatus, approval.getDecision());
                leave.setStatus(newStatus);
                maternityLeaveRepository.save(leave);

                // Trigger Notification if status reached a final state or was rejected
                if ("APPROVED".equals(newStatus) || "REJECTED".equals(newStatus)) {
                    notificationService.sendLeaveStatusUpdate(
                        leave.getEmployee().getFullName(), 
                        leave.getEmail(),
                        leave.getContactNumber(),
                        "Maternity Leave", 
                        newStatus, 
                        approval.getRemark()
                    );
                }
            }
        }

        return savedApproval;
    }

    private String calculateNextStatus(String currentStatus, String decision) {
        if ("REJECTED".equalsIgnoreCase(decision)) {
            return "REJECTED";
        }

        if ("APPROVED".equalsIgnoreCase(decision)) {
            if ("PENDING_HR_APPROVAL".equals(currentStatus)) {
                return "PENDING_ADMIN_APPROVAL";
            } else if ("PENDING_ADMIN_APPROVAL".equals(currentStatus)) {
                // Admin approves → goes to Board Meeting Agenda, NOT directly to Director
                return "ADMIN_APPROVED";
            } else if ("ADMIN_APPROVED".equals(currentStatus)) {
                // Admin selects from Board Agenda and sends to Director for review
                return "PENDING_DIRECTOR_REVIEW";
            } else if ("PENDING_DIRECTOR_REVIEW".equals(currentStatus)) {
                return "APPROVED"; // Final state
            }
        }

        return currentStatus;
    }

    @Override
    public List<Approval> getApprovalsByRef(Long refId, String refType) {
        return approvalRepository.findByRefIdAndRefType(refId, refType);
    }
}
