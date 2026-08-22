package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.DashboardAnalyticsDto;
import com.hexaco.hrms.models.MaternityLeave;
import com.hexaco.hrms.models.OverseasLeave;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.repository.MaternityLeaveRepository;
import com.hexaco.hrms.repository.OverseasLeaveRepository;
import com.hexaco.hrms.repository.TrainingEventRepository;
import com.hexaco.hrms.repository.AttendanceDailySummaryRepository;
import com.hexaco.hrms.models.TrainingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final OverseasLeaveRepository overseasLeaveRepository;
    private final MaternityLeaveRepository maternityLeaveRepository;
    private final TrainingEventRepository trainingEventRepository;
    private final AttendanceDailySummaryRepository attendanceRepo;

    public DashboardAnalyticsDto getAnalytics() {
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        LocalDate today = LocalDate.now();
        LocalDate sixMonthsFromNow = today.plusMonths(6);
        LocalDate oneMonthFromNow = today.plusMonths(1);

        // Delayed Approvals (Combined from overseas and maternity where status = SUBMITTED and created < 2 days ago)
        long delayedOverseas = overseasLeaveRepository.findByStatusAndCreatedAtBefore("PENDING_HR_APPROVAL", twoDaysAgo).size();
        long delayedMaternity = maternityLeaveRepository.findByStatusAndCreatedAtBefore("PENDING_HR_APPROVAL", twoDaysAgo).size();

        // Pending Leaves
        long pendingOverseas = overseasLeaveRepository.countPending();
        long pendingMaternity = maternityLeaveRepository.countPending();

        // Upcoming Expiries & Returns
        List<OverseasLeave> expiringPassports = overseasLeaveRepository.findUpcomingPassportExpiries(today, sixMonthsFromNow);
        List<MaternityLeave> returningMaternity = maternityLeaveRepository.findUpcomingMaternityReturns(today, oneMonthFromNow);

        // Department Counts
        Map<String, Long> deptEmpCount = new HashMap<>();
        List<Object[]> empCounts = employeeRepository.countEmployeesByDepartment();
        for (Object[] row : empCounts) {
            String dept = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            deptEmpCount.put(dept != null ? dept : "Unassigned", count);
        }

        // Department Leave Impact (Combining Overseas and Maternity)
        Map<String, Long> deptLeaveImpact = new HashMap<>();
        List<Object[]> overseasImpact = overseasLeaveRepository.countApprovedLeavesByDepartment();
        for (Object[] row : overseasImpact) {
            String dept = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            deptLeaveImpact.put(dept != null ? dept : "Unassigned", count);
        }
        
        List<Object[]> maternityImpact = maternityLeaveRepository.countApprovedLeavesByDepartment();
        for (Object[] row : maternityImpact) {
            String dept = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            deptLeaveImpact.merge(dept != null ? dept : "Unassigned", count, Long::sum);
        }

        // New HR Stats
        long totalStaff = employeeRepository.count();
        
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        long newHiresThisWeek = employeeRepository.findAll().stream()
                .filter(e -> e.getCreatedAt() != null && e.getCreatedAt().isAfter(sevenDaysAgo))
                .count();
        
        List<TrainingEvent> allEvents = trainingEventRepository.findAll();
        long activeTrainingPrograms = allEvents.stream()
                .filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus()) || "SCHEDULED".equalsIgnoreCase(e.getStatus()))
                .count();
                
        long trainingsFinishingSoon = allEvents.stream()
                .filter(e -> e.getEndDate() != null && e.getEndDate().isAfter(today) && e.getEndDate().isBefore(today.plusDays(7)))
                .count();
                
        // Attendance
        long presentTodayCount = attendanceRepo.countByAttendanceDate(today);
        String attendancePercentage = "0";
        if (totalStaff > 0) {
            double percentage = ((double) presentTodayCount / totalStaff) * 100;
            attendancePercentage = String.format("%.1f", percentage);
        }

        return DashboardAnalyticsDto.builder()
                .presentToday((int) presentTodayCount)
                .lateToday(0)    // Dummy for now
                .pendingOverseas(pendingOverseas)
                .pendingMaternity(pendingMaternity)
                .delayedApprovals(delayedOverseas + delayedMaternity)
                .totalStaff(totalStaff)
                .newHiresThisWeek(newHiresThisWeek)
                .activeTrainingPrograms(activeTrainingPrograms)
                .trainingsFinishingSoon(trainingsFinishingSoon)
                .attendancePercentage(attendancePercentage + "%")
                .passportExpiryAlerts(expiringPassports.stream()
                        .map(l -> new DashboardAnalyticsDto.PassportExpiryAlert(
                                l.getEmployee().getFullName(),
                                l.getPassportNumber(),
                                l.getPassportExpDate().toString()))
                        .collect(Collectors.toList()))
                .upcomingMaternityReturns(returningMaternity.stream()
                        .map(l -> new DashboardAnalyticsDto.MaternityReturnAlert(
                                l.getEmployee().getFullName(),
                                l.getEndDate().plusDays(1).toString()))
                        .collect(Collectors.toList()))
                .departmentEmployeeCount(deptEmpCount)
                .departmentLeaveImpact(deptLeaveImpact)
                .build();
    }
}
