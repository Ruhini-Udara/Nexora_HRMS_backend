package com.hexaco.hrms.service.impl;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ApprovalServiceImplTest {

    private final ApprovalServiceImpl approvalService = new ApprovalServiceImpl(null, null, null, null, null);

    @Test
    public void testCalculateNextOverseasStatus_EmployeeFlow() {
        // Given a normal employee request at HR stage
        String next = approvalService.calculateNextOverseasStatus("PENDING_HR_APPROVAL", "APPROVE", "ROLE_EMPLOYEE");
        
        // Then it should move to Admin stage
        assertEquals("PENDING_ADMIN_APPROVAL", next);
    }

    @Test
    public void testCalculateNextOverseasStatus_AdminPersonalFlow() {
        // Given an Admin's personal request at HR stage
        String next = approvalService.calculateNextOverseasStatus("PENDING_HR_APPROVAL", "APPROVE", "ROLE_ADMIN");
        
        // Then it should skip Admin Board Agenda and go to Director
        assertEquals("PENDING_DIRECTOR_REVIEW", next);
    }

    @Test
    public void testCalculateNextOverseasStatus_DirectorPersonalFlow() {
        // Given a Director's personal request at Admin stage
        String next = approvalService.calculateNextOverseasStatus("PENDING_ADMIN_APPROVAL", "APPROVE", "ROLE_DIRECTOR");
        
        // Then it should be finalized immediately
        assertEquals("APPROVED", next);
    }

    @Test
    public void testCalculateNextMaternityStatus_Rejection() {
        // Given any request
        String next = approvalService.calculateNextMaternityStatus("PENDING_HR_APPROVAL", "REJECT", "ROLE_EMPLOYEE");
        
        // Then it should be rejected
        assertEquals("REJECTED", next);
    }
}
