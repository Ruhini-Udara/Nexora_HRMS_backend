package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.EmployeeDashboardDto;
import com.hexaco.hrms.dto.RecentRequestItemDto;
import com.hexaco.hrms.models.*;
import com.hexaco.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeDashboardService {

    private final AttendanceDailySummaryRepository attendanceRepo;
    private final LeaveBalanceRepository leaveBalanceRepo;
    private final TrainingRequestRepository trainingReqRepo;
    private final NormalLeaveRepository normalLeaveRepo;
    private final OverseasLeaveRepository overseasLeaveRepo;
    private final MaternityLeaveRepository maternityLeaveRepo;
    private final TransferRequestRepository transferReqRepo;
    private final WelfareRequestRepository welfareReqRepo;
    private final ManualAttendanceRepository manualAttendanceRepo;
    private final EmployeeRepository employeeRepo;

    private boolean isPending(String status) {
        if (status == null) return false;
        String s = status.toUpperCase();
        return s.contains("PENDING") || s.equals("SUBMITTED");
    }

    public EmployeeDashboardDto getEmployeeDashboard(Long employeeId) {
        LocalDate today = LocalDate.now();
        int year = today.getYear();

        // 1. Attendance Status
        String attendanceStatus = "Not Checked In";
        String attendanceTime = null;
        Optional<AttendanceDailySummary> summaryOpt = attendanceRepo.findByEmployeeIdAndAttendanceDate(employeeId, today);
        Optional<ManualAttendance> manualOpt = manualAttendanceRepo.findByEmployeeIdAndAttendanceDate(employeeId, today);

        if (summaryOpt.isPresent() && summaryOpt.get().getCheckInTime() != null) {
            attendanceStatus = "Checked In";
            LocalDateTime checkIn = summaryOpt.get().getCheckInTime();
            String amPm = checkIn.getHour() >= 12 ? "PM" : "AM";
            int hour = checkIn.getHour() % 12;
            if (hour == 0) hour = 12;
            attendanceTime = String.format("at %d:%02d %s", hour, checkIn.getMinute(), amPm);
        } else if (manualOpt.isPresent() && "APPROVED".equalsIgnoreCase(manualOpt.get().getApprovalStatus()) && manualOpt.get().getInTime() != null) {
            attendanceStatus = "Checked In";
            java.time.LocalTime checkIn = manualOpt.get().getInTime();
            String amPm = checkIn.getHour() >= 12 ? "PM" : "AM";
            int hour = checkIn.getHour() % 12;
            if (hour == 0) hour = 12;
            attendanceTime = String.format("at %d:%02d %s", hour, checkIn.getMinute(), amPm);
        }

        // 2. Leave Balance
        int totalLeaveBalance = 0;
        Optional<LeaveBalance> balanceOpt = leaveBalanceRepo.findByEmployeeIdAndLeaveYear(employeeId, year);
        if (balanceOpt.isPresent()) {
            LeaveBalance lb = balanceOpt.get();
            int annual = (lb.getAnnualLeaveQuota() != null ? lb.getAnnualLeaveQuota() : 0) - (lb.getAnnualLeaveUsed() != null ? lb.getAnnualLeaveUsed() : 0);
            int casual = (lb.getCasualLeaveQuota() != null ? lb.getCasualLeaveQuota() : 0) - (lb.getCasualLeaveUsed() != null ? lb.getCasualLeaveUsed() : 0);
            int medical = (lb.getMedicalLeaveQuota() != null ? lb.getMedicalLeaveQuota() : 0) - (lb.getMedicalLeaveUsed() != null ? lb.getMedicalLeaveUsed() : 0);
            totalLeaveBalance = Math.max(0, annual) + Math.max(0, casual) + Math.max(0, medical);
        }

        // 3. Active Training Programs
        List<TrainingRequest> trainings = trainingReqRepo.findByEmployeeId(employeeId);
        int activeTrainingPrograms = (int) trainings.stream()
                .filter(t -> "Approved".equalsIgnoreCase(t.getStatus()))
                .count();

        // 4. Recent Requests & Pending Count
        List<RecentRequestItemDto> allRequests = new ArrayList<>();
        int pendingCount = 0;

        List<NormalLeave> normalLeaves = normalLeaveRepo.findByEmployeeId(employeeId);
        for (NormalLeave r : normalLeaves) {
            allRequests.add(new RecentRequestItemDto("Normal Leave", r.getCreatedAt(), r.getStatus()));
            if (isPending(r.getStatus())) pendingCount++;
        }

        List<OverseasLeave> overseasLeaves = overseasLeaveRepo.findByEmployeeId(employeeId);
        for (OverseasLeave r : overseasLeaves) {
            allRequests.add(new RecentRequestItemDto("Overseas Leave", r.getCreatedAt(), r.getStatus()));
            if (isPending(r.getStatus())) pendingCount++;
        }

        List<MaternityLeave> maternityLeaves = maternityLeaveRepo.findByEmployeeId(employeeId);
        for (MaternityLeave r : maternityLeaves) {
            allRequests.add(new RecentRequestItemDto("Maternity Leave", r.getCreatedAt(), r.getStatus()));
            if (isPending(r.getStatus())) pendingCount++;
        }

        List<TransferRequest> transferRequests = transferReqRepo.findByEmployeeId(employeeId);
        for (TransferRequest r : transferRequests) {
            allRequests.add(new RecentRequestItemDto("Transfer Request", r.getCreatedAt(), r.getStatus()));
            if (isPending(r.getStatus())) pendingCount++;
        }

        List<WelfareRequest> welfareRequests = welfareReqRepo.findByEmployeeId(employeeId);
        for (WelfareRequest r : welfareRequests) {
            allRequests.add(new RecentRequestItemDto("Welfare Request", r.getCreatedAt(), r.getStatus()));
            if (isPending(r.getStatus())) pendingCount++;
        }

        for (TrainingRequest r : trainings) {
            allRequests.add(new RecentRequestItemDto("Training Request", r.getCreatedAt(), r.getStatus()));
            if (isPending(r.getStatus())) pendingCount++;
        }

        // Sort and limit recent requests
        allRequests.sort(Comparator.comparing(RecentRequestItemDto::getDateSubmitted).reversed());
        List<RecentRequestItemDto> recentRequests = allRequests.size() > 5 ? allRequests.subList(0, 5) : allRequests;

        // Shift info from employee designation
        String shiftName = null;
        String shiftStartTime = null;
        String shiftEndTime = null;
        Optional<Employee> empOpt = employeeRepo.findById(employeeId);
        if (empOpt.isPresent() && empOpt.get().getDesignation() != null && empOpt.get().getDesignation().getShift() != null) {
            Shift shift = empOpt.get().getDesignation().getShift();
            shiftName = shift.getName();
            if (shift.getStartTime() != null) {
                shiftStartTime = shift.getStartTime().toString();
            }
            if (shift.getEndTime() != null) {
                shiftEndTime = shift.getEndTime().toString();
            }
        }

        return EmployeeDashboardDto.builder()
                .attendanceStatus(attendanceStatus)
                .attendanceTime(attendanceTime)
                .leaveBalance(totalLeaveBalance)
                .activeTrainingPrograms(activeTrainingPrograms)
                .pendingRequestsCount(pendingCount)
                .recentRequests(recentRequests)
                .shiftName(shiftName)
                .shiftStartTime(shiftStartTime)
                .shiftEndTime(shiftEndTime)
                .build();
    }

    public List<RecentRequestItemDto> getAllEmployeeRequests(Long employeeId) {
        List<RecentRequestItemDto> allRequests = new ArrayList<>();

        List<NormalLeave> normalLeaves = normalLeaveRepo.findByEmployeeId(employeeId);
        for (NormalLeave r : normalLeaves) {
            allRequests.add(new RecentRequestItemDto("Normal Leave", r.getCreatedAt(), r.getStatus()));
        }

        List<OverseasLeave> overseasLeaves = overseasLeaveRepo.findByEmployeeId(employeeId);
        for (OverseasLeave r : overseasLeaves) {
            allRequests.add(new RecentRequestItemDto("Overseas Leave", r.getCreatedAt(), r.getStatus()));
        }

        List<MaternityLeave> maternityLeaves = maternityLeaveRepo.findByEmployeeId(employeeId);
        for (MaternityLeave r : maternityLeaves) {
            allRequests.add(new RecentRequestItemDto("Maternity Leave", r.getCreatedAt(), r.getStatus()));
        }

        List<TransferRequest> transferRequests = transferReqRepo.findByEmployeeId(employeeId);
        for (TransferRequest r : transferRequests) {
            allRequests.add(new RecentRequestItemDto("Transfer Request", r.getCreatedAt(), r.getStatus()));
        }

        List<WelfareRequest> welfareRequests = welfareReqRepo.findByEmployeeId(employeeId);
        for (WelfareRequest r : welfareRequests) {
            allRequests.add(new RecentRequestItemDto("Welfare Request", r.getCreatedAt(), r.getStatus()));
        }

        List<TrainingRequest> trainings = trainingReqRepo.findByEmployeeId(employeeId);
        for (TrainingRequest r : trainings) {
            allRequests.add(new RecentRequestItemDto("Training Request", r.getCreatedAt(), r.getStatus()));
        }

        allRequests.sort(Comparator.comparing(RecentRequestItemDto::getDateSubmitted).reversed());
        return allRequests;
    }
}
