package com.hexaco.hrms.service;

import com.hexaco.hrms.models.Approval;

import java.util.List;

public interface ApprovalService {
    Approval saveApproval(Approval approval);
    List<Approval> getApprovalsByRef(Long refId, String refType);
}
