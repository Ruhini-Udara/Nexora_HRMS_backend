package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.SupervisorReportAnalyticsDto;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.LeaveRequest;
import com.hexaco.hrms.models.ManualAttendance;
import com.hexaco.hrms.models.NormalLeave;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.repository.LeaveRequestRepository;
import com.hexaco.hrms.repository.ManualAttendanceRepository;
import com.hexaco.hrms.repository.NormalLeaveRepository;
import com.hexaco.hrms.service.SupervisorReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupervisorReportServiceImpl implements SupervisorReportService {

    private final EmployeeRepository employeeRepository;
    private final ManualAttendanceRepository manualAttendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final NormalLeaveRepository normalLeaveRepository;

    @Override
    public SupervisorReportAnalyticsDto getSupervisorReportAnalytics(
            Long supervisorId,
            String period,
            String department,
            String branch
    ) {
        log.info("Calculating supervisor reports analytics: supervisorId={}, period={}, dept={}, branch={}",
                supervisorId, period, department, branch);

        // 1. Resolve Team Employees
        List<Employee> team;
        if (supervisorId != null && supervisorId > 0) {
            team = employeeRepository.findByReportingOfficerId(supervisorId);
            if (team.isEmpty()) {
                team = employeeRepository.findAll();
            }
        } else {
            team = employeeRepository.findAll();
        }

        // Apply filters
        if (department != null && !department.equalsIgnoreCase("All") && !department.isBlank()) {
            team = team.stream()
                    .filter(e -> e.getDepartment() != null && e.getDepartment().equalsIgnoreCase(department))
                    .collect(Collectors.toList());
        }
        if (branch != null && !branch.equalsIgnoreCase("All") && !branch.isBlank()) {
            team = team.stream()
                    .filter(e -> e.getBranch() != null && e.getBranch().equalsIgnoreCase(branch))
                    .collect(Collectors.toList());
        }

        Set<Long> teamEmployeeIds = team.stream().map(Employee::getId).collect(Collectors.toSet());
        int teamSize = Math.max(team.size(), 1);

        // 2. Resolve Date Range
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        if ("7d".equalsIgnoreCase(period) || "Last 7 Days".equalsIgnoreCase(period)) {
            startDate = today.minusDays(7);
        } else if ("month".equalsIgnoreCase(period) || "This Month".equalsIgnoreCase(period)) {
            startDate = today.withDayOfMonth(1);
        } else if ("90d".equalsIgnoreCase(period) || "Last 90 Days".equalsIgnoreCase(period)) {
            startDate = today.minusDays(90);
        } else if ("ytd".equalsIgnoreCase(period) || "Year to Date".equalsIgnoreCase(period)) {
            startDate = today.withDayOfYear(1);
        } else {
            // Default: 30 days
            startDate = today.minusDays(30);
        }

        // 3. Query Real Attendance Records from DB
        List<ManualAttendance> attendances = manualAttendanceRepository.findByAttendanceDateBetween(startDate, today)
                .stream()
                .filter(a -> a.getEmployee() != null && teamEmployeeIds.contains(a.getEmployee().getId()))
                .collect(Collectors.toList());

        // 4. Calculate Attendance Trends (7 Graduated Time Buckets)
        long totalDays = Math.max(1, ChronoUnit.DAYS.between(startDate, today) + 1);
        int numBuckets = 7;
        double daysPerBucket = (double) totalDays / numBuckets;

        List<SupervisorReportAnalyticsDto.AttendanceTrendItemDto> trendItems = new ArrayList<>();
        double firstHalfSum = 0;
        double secondHalfSum = 0;

        for (int i = 0; i < numBuckets; i++) {
            LocalDate bucketStart = startDate.plusDays((long) Math.floor(i * daysPerBucket));
            LocalDate bucketEnd = startDate.plusDays((long) Math.floor((i + 1) * daysPerBucket) - 1);
            if (bucketEnd.isAfter(today) || i == numBuckets - 1) {
                bucketEnd = today;
            }

            final LocalDate bStart = bucketStart;
            final LocalDate bEnd = bucketEnd;

            long bucketDays = Math.max(1, ChronoUnit.DAYS.between(bStart, bEnd) + 1);
            long possiblePresences = (long) teamSize * bucketDays;

            long presentCount = attendances.stream()
                    .filter(a -> !a.getAttendanceDate().isBefore(bStart) && !a.getAttendanceDate().isAfter(bEnd))
                    .filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus()))
                    .count();

            // Calculate percentage based strictly on actual attendance
            double rate = 0.0;
            if (possiblePresences > 0 && presentCount > 0) {
                rate = ((double) presentCount / possiblePresences) * 100.0;
            }
            rate = Math.min(100.0, Math.max(0.0, rate));

            int heightPercent = (int) Math.round(rate);
            boolean isCurrent = (i == numBuckets - 1);

            String label = isCurrent ? "Today" : ("Day " + (Math.round(i * daysPerBucket) + 1) + "-" + Math.round((i + 1) * daysPerBucket));

            trendItems.add(SupervisorReportAnalyticsDto.AttendanceTrendItemDto.builder()
                    .label(label)
                    .presentPercentage(Math.round(rate * 10.0) / 10.0)
                    .presentCount((int) presentCount)
                    .totalCount((int) possiblePresences)
                    .heightPercent(heightPercent)
                    .isCurrent(isCurrent)
                    .build());

            if (i < numBuckets / 2) {
                firstHalfSum += rate;
            } else {
                secondHalfSum += rate;
            }
        }

        double firstHalfAvg = (numBuckets / 2 > 0) ? (firstHalfSum / (numBuckets / 2)) : 0.0;
        double secondHalfAvg = (numBuckets - (numBuckets / 2) > 0) ? (secondHalfSum / (numBuckets - (numBuckets / 2))) : 0.0;
        double growth = secondHalfAvg - firstHalfAvg;
        String trendGrowth;
        if (Double.compare(growth, 0.0) == 0) {
            trendGrowth = "0.0%";
        } else {
            trendGrowth = (growth > 0 ? "+" : "") + String.format(Locale.US, "%.1f%%", growth);
        }

        // 5. Query Real Leave Distribution from DB
        List<NormalLeave> leaves = normalLeaveRepository.findAll().stream()
                .filter(l -> l.getEmployee() != null && teamEmployeeIds.contains(l.getEmployee().getId()))
                .collect(Collectors.toList());

        int sick = 0;
        int annual = 0;
        int casual = 0;

        for (NormalLeave l : leaves) {
            String typeName = l.getLeaveType() != null ? l.getLeaveType().getLeaveTypeName().toLowerCase() : "";
            int duration = l.getTotalDays() != null ? l.getTotalDays() : 1;

            if (typeName.contains("sick") || typeName.contains("medical")) {
                sick += duration;
            } else if (typeName.contains("annual")) {
                annual += duration;
            } else {
                casual += duration;
            }
        }

        int totalLeaves = sick + annual + casual;

        SupervisorReportAnalyticsDto.LeaveDistributionDto leaveDistribution =
                SupervisorReportAnalyticsDto.LeaveDistributionDto.builder()
                        .sick(sick)
                        .annual(annual)
                        .casual(casual)
                        .total(totalLeaves)
                        .build();

        // 6. Calculate Real Overtime by Department from DB
        Map<String, BigDecimal> deptOvertimeMap = new LinkedHashMap<>();
        for (Employee e : team) {
            String dept = e.getDepartment() != null ? e.getDepartment() : "General";
            deptOvertimeMap.putIfAbsent(dept, BigDecimal.ZERO);
        }

        BigDecimal totalOvertime = BigDecimal.ZERO;
        for (ManualAttendance a : attendances) {
            if (a.getOvertimeHours() != null && a.getOvertimeHours().compareTo(BigDecimal.ZERO) > 0) {
                String dept = (a.getEmployee() != null && a.getEmployee().getDepartment() != null)
                        ? a.getEmployee().getDepartment()
                        : "General";

                BigDecimal current = deptOvertimeMap.getOrDefault(dept, BigDecimal.ZERO);
                deptOvertimeMap.put(dept, current.add(a.getOvertimeHours()));
                totalOvertime = totalOvertime.add(a.getOvertimeHours());
            }
        }

        List<SupervisorReportAnalyticsDto.OvertimeDeptDto> overtimeInsights = new ArrayList<>();
        double maxHours = deptOvertimeMap.values().stream()
                .mapToDouble(BigDecimal::doubleValue)
                .max()
                .orElse(0.0);

        for (Map.Entry<String, BigDecimal> entry : deptOvertimeMap.entrySet()) {
            double hrs = entry.getValue().doubleValue();
            int pct = maxHours > 0 ? (int) Math.round((hrs / maxHours) * 100) : 0;
            overtimeInsights.add(SupervisorReportAnalyticsDto.OvertimeDeptDto.builder()
                    .department(entry.getKey())
                    .hours(hrs)
                    .percentage(pct)
                    .build());
        }

        overtimeInsights.sort((a, b) -> Double.compare(b.getHours(), a.getHours()));
        if (overtimeInsights.size() > 5) {
            overtimeInsights = overtimeInsights.subList(0, 5);
        }

        return SupervisorReportAnalyticsDto.builder()
                .totalTeamMembers(team.size())
                .totalOvertimeHours(totalOvertime.setScale(1, RoundingMode.HALF_UP))
                .trendGrowth(trendGrowth)
                .attendanceTrends(trendItems)
                .leaveDistribution(leaveDistribution)
                .overtimeInsights(overtimeInsights)
                .build();
    }
}
