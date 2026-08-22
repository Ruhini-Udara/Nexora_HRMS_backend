package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.DirectorDashboardDto;
import com.hexaco.hrms.models.*;
import com.hexaco.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorDashboardService {

    private final AttendanceDailySummaryRepository attendanceRepo;
    private final EmployeeRepository employeeRepo;
    
    private final NormalLeaveRepository normalLeaveRepo;
    private final OverseasLeaveRepository overseasLeaveRepo;
    private final MaternityLeaveRepository maternityLeaveRepo;
    private final TransferRequestRepository transferReqRepo;
    private final ResignationRepository resignationRepo;
    private final DeathRequestRepository deathReqRepo;

    public DirectorDashboardDto getDirectorDashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        String pendingStatus = "PENDING_DIRECTOR_REVIEW";

        // 1. Pending & Urgent Approvals
        long pendingApprovalsCount = 0;
        long urgentApprovalsCount = 0;

        // Normal Leaves
        List<NormalLeave> normalLeaves = normalLeaveRepo.findByStatus(pendingStatus);
        pendingApprovalsCount += normalLeaves.size();
        urgentApprovalsCount += normalLeaves.stream().filter(r -> r.getCreatedAt().isBefore(threeDaysAgo)).count();

        // Overseas Leaves
        List<OverseasLeave> overseasLeaves = overseasLeaveRepo.findByStatus(pendingStatus);
        pendingApprovalsCount += overseasLeaves.size();
        urgentApprovalsCount += overseasLeaves.stream().filter(r -> r.getCreatedAt().isBefore(threeDaysAgo)).count();

        // Maternity Leaves
        List<MaternityLeave> maternityLeaves = maternityLeaveRepo.findByStatus(pendingStatus);
        pendingApprovalsCount += maternityLeaves.size();
        urgentApprovalsCount += maternityLeaves.stream().filter(r -> r.getCreatedAt().isBefore(threeDaysAgo)).count();

        // Transfer Requests
        List<TransferRequest> transferRequests = transferReqRepo.findByStatus(pendingStatus);
        pendingApprovalsCount += transferRequests.size();
        urgentApprovalsCount += transferRequests.stream().filter(r -> r.getCreatedAt().isBefore(threeDaysAgo)).count();

        // Resignation Requests
        List<Resignation> resignations = resignationRepo.findByStatus(pendingStatus);
        pendingApprovalsCount += resignations.size();
        urgentApprovalsCount += resignations.stream().filter(r -> r.getCreatedAt().isBefore(threeDaysAgo)).count();

        // Death Requests
        List<DeathRequest> deathRequests = deathReqRepo.findByStatus(pendingStatus);
        pendingApprovalsCount += deathRequests.size();
        urgentApprovalsCount += deathRequests.stream().filter(r -> r.getCreatedAt().isBefore(threeDaysAgo)).count();

        // 2. Company Attendance
        long totalEmployees = employeeRepo.count();
        long presentToday = attendanceRepo.countByAttendanceDate(today);
        String attendancePercentage = "0%";
        if (totalEmployees > 0) {
            long percentage = Math.round((double) presentToday / totalEmployees * 100);
            attendancePercentage = percentage + "%";
        }

        // 3. Total Employees
        // (already fetched in totalEmployees)

        return DirectorDashboardDto.builder()
                .pendingApprovalsCount(pendingApprovalsCount)
                .urgentApprovalsCount(urgentApprovalsCount)
                .companyAttendancePercentage(attendancePercentage)
                .totalEmployeesCount(totalEmployees)
                .build();
    }
}
