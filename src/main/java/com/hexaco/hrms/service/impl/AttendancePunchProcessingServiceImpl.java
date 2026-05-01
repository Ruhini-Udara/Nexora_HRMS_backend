package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.AttendancePunchProcessResponse;
import com.hexaco.hrms.models.AttendanceDailySummary;
import com.hexaco.hrms.models.AttendanceDevicePunch;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.repository.AttendanceDailySummaryRepository;
import com.hexaco.hrms.repository.AttendanceDevicePunchRepository;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.service.AttendancePunchProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendancePunchProcessingServiceImpl implements AttendancePunchProcessingService {

    private final AttendanceDevicePunchRepository attendanceDevicePunchRepository;
    private final AttendanceDailySummaryRepository attendanceDailySummaryRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public AttendancePunchProcessResponse processUnprocessedPunches() {
        List<AttendanceDevicePunch> unprocessedPunches =
                attendanceDevicePunchRepository.findByProcessedFalseOrderByPunchTimeAsc();

        Map<Long, Optional<Employee>> employeeCache = new HashMap<>();
        Map<SummaryKey, SummaryGroup> groupedPunches = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        int unknownUserCount = 0;

        for (AttendanceDevicePunch punch : unprocessedPunches) {
            Optional<Employee> employeeOptional = employeeCache.computeIfAbsent(
                    punch.getTerminalUserId(),
                    employeeRepository::findByFingerprintUserId
            );

            if (employeeOptional.isEmpty()) {
                unknownUserCount++;
                errors.add("punchId=" + punch.getId()
                        + " terminalUserId=" + punch.getTerminalUserId()
                        + " has no matching employee fingerprintUserId");
                continue;
            }

            Employee employee = employeeOptional.get();
            LocalDate attendanceDate = punch.getPunchTime().toLocalDate();
            SummaryKey key = new SummaryKey(employee.getId(), attendanceDate);

            groupedPunches
                    .computeIfAbsent(key, ignored -> new SummaryGroup(employee, attendanceDate))
                    .addPunch(punch);
        }

        int processedPunchCount = 0;
        int summaryCreatedCount = 0;
        int summaryUpdatedCount = 0;
        List<AttendanceDevicePunch> punchesToMarkProcessed = new ArrayList<>();

        for (SummaryGroup group : groupedPunches.values()) {
            Optional<AttendanceDailySummary> existingSummary =
                    attendanceDailySummaryRepository.findByEmployeeIdAndAttendanceDate(
                            group.employee().getId(),
                            group.attendanceDate()
                    );

            AttendanceDailySummary summary;
            if (existingSummary.isPresent()) {
                summary = existingSummary.get();
                summaryUpdatedCount++;
            } else {
                summary = AttendanceDailySummary.builder()
                        .employee(group.employee())
                        .attendanceDate(group.attendanceDate())
                        .build();
                summaryCreatedCount++;
            }

            SummaryTimeline timeline = buildTimeline(summary, group.punches());
            summary.setCheckInTime(timeline.checkInTime());
            summary.setCheckOutTime(timeline.checkOutTime());
            attendanceDailySummaryRepository.save(summary);

            for (AttendanceDevicePunch punch : group.punches()) {
                punch.setProcessed(true);
                punchesToMarkProcessed.add(punch);
            }
            processedPunchCount += group.punches().size();
        }

        if (!punchesToMarkProcessed.isEmpty()) {
            attendanceDevicePunchRepository.saveAll(punchesToMarkProcessed);
        }

        return AttendancePunchProcessResponse.builder()
                .processedPunchCount(processedPunchCount)
                .summaryCreatedCount(summaryCreatedCount)
                .summaryUpdatedCount(summaryUpdatedCount)
                .unknownUserCount(unknownUserCount)
                .errors(errors)
                .build();
    }

    private SummaryTimeline buildTimeline(AttendanceDailySummary summary, List<AttendanceDevicePunch> punches) {
        List<LocalDateTime> times = new ArrayList<>();

        if (summary.getCheckInTime() != null) {
            times.add(summary.getCheckInTime());
        }
        if (summary.getCheckOutTime() != null) {
            times.add(summary.getCheckOutTime());
        }
        for (AttendanceDevicePunch punch : punches) {
            times.add(punch.getPunchTime());
        }

        times.sort(LocalDateTime::compareTo);

        LocalDateTime checkInTime = times.get(0);
        LocalDateTime checkOutTime = times.size() > 1 ? times.get(times.size() - 1) : null;

        return new SummaryTimeline(checkInTime, checkOutTime);
    }

    private record SummaryKey(Long employeeId, LocalDate attendanceDate) {
    }

    private record SummaryTimeline(LocalDateTime checkInTime, LocalDateTime checkOutTime) {
    }

    private record SummaryGroup(Employee employee, LocalDate attendanceDate, List<AttendanceDevicePunch> punches) {
        private SummaryGroup(Employee employee, LocalDate attendanceDate) {
            this(employee, attendanceDate, new ArrayList<>());
        }

        private void addPunch(AttendanceDevicePunch punch) {
            punches.add(punch);
        }
    }
}
