package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.models.Approval;
import com.hexaco.hrms.repository.ApprovalRepository;
import com.hexaco.hrms.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository approvalRepository;

    @Override
    public Approval saveApproval(Approval approval) {
        // Automatically stamp the decision date before saving
        if (approval.getDecisionDate() == null) {
            approval.setDecisionDate(LocalDateTime.now());
        }
        return approvalRepository.save(approval);
    }

    @Override
    public List<Approval> getApprovalsByRef(Long refId, String refType) {
        return approvalRepository.findByRefIdAndRefType(refId, refType);
    }
}
