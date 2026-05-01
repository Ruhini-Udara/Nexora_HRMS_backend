package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.AttendancePunchProcessResponse;
import com.hexaco.hrms.models.AttendanceDailySummary;
import com.hexaco.hrms.models.AttendanceDevicePunch;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.repository.AttendanceDailySummaryRepository;
import com.hexaco.hrms.repository.AttendanceDevicePunchRepository;
import com.hexaco.hrms.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AttendancePunchProcessingServiceImplTest {

    private final AttendanceDevicePunchRepository punchRepository = mock(AttendanceDevicePunchRepository.class);
    private final AttendanceDailySummaryRepository summaryRepository = mock(AttendanceDailySummaryRepository.class);
    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    private final AttendancePunchProcessingServiceImpl service =
            new AttendancePunchProcessingServiceImpl(punchRepository, summaryRepository, employeeRepository);

    @Test
    public void processKnownPunchesCreatesDailySummaryAndMarksPunchesProcessed() {
        Employee employee = employee(1L, 101L);
        LocalDate date = LocalDate.of(2026, 5, 1);
        AttendanceDevicePunch firstPunch = punch(1L, 101L, LocalDateTime.of(2026, 5, 1, 8, 15));
        AttendanceDevicePunch lastPunch = punch(2L, 101L, LocalDateTime.of(2026, 5, 1, 17, 45));

        when(punchRepository.findByProcessedFalseOrderByPunchTimeAsc()).thenReturn(List.of(firstPunch, lastPunch));
        when(employeeRepository.findByFingerprintUserId(101L)).thenReturn(Optional.of(employee));
        when(summaryRepository.findByEmployeeIdAndAttendanceDate(1L, date)).thenReturn(Optional.empty());
        when(summaryRepository.save(any(AttendanceDailySummary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendancePunchProcessResponse response = service.processUnprocessedPunches();

        assertEquals(2, response.getProcessedPunchCount());
        assertEquals(1, response.getSummaryCreatedCount());
        assertEquals(0, response.getSummaryUpdatedCount());
        assertEquals(0, response.getUnknownUserCount());
        assertTrue(response.getErrors().isEmpty());
        assertTrue(firstPunch.getProcessed());
        assertTrue(lastPunch.getProcessed());

        verify(summaryRepository).save(any(AttendanceDailySummary.class));
        verify(punchRepository).saveAll(anyList());
    }

    @Test
    public void processSinglePunchCreatesCheckInOnlySummary() {
        Employee employee = employee(1L, 101L);
        LocalDate date = LocalDate.of(2026, 5, 1);
        LocalDateTime punchTime = LocalDateTime.of(2026, 5, 1, 8, 15);
        AttendanceDevicePunch onlyPunch = punch(1L, 101L, punchTime);

        when(punchRepository.findByProcessedFalseOrderByPunchTimeAsc()).thenReturn(List.of(onlyPunch));
        when(employeeRepository.findByFingerprintUserId(101L)).thenReturn(Optional.of(employee));
        when(summaryRepository.findByEmployeeIdAndAttendanceDate(1L, date)).thenReturn(Optional.empty());
        when(summaryRepository.save(any(AttendanceDailySummary.class))).thenAnswer(invocation -> {
            AttendanceDailySummary summary = invocation.getArgument(0);
            assertEquals(punchTime, summary.getCheckInTime());
            assertNull(summary.getCheckOutTime());
            return summary;
        });

        AttendancePunchProcessResponse response = service.processUnprocessedPunches();

        assertEquals(1, response.getProcessedPunchCount());
        assertEquals(1, response.getSummaryCreatedCount());
        assertEquals(0, response.getSummaryUpdatedCount());
        assertTrue(onlyPunch.getProcessed());
    }

    @Test
    public void processExistingSummaryUpdatesSafelyWithoutCreatingDuplicateSummary() {
        Employee employee = employee(1L, 101L);
        LocalDate date = LocalDate.of(2026, 5, 1);
        AttendanceDailySummary existingSummary = AttendanceDailySummary.builder()
                .id(1L)
                .employee(employee)
                .attendanceDate(date)
                .checkInTime(LocalDateTime.of(2026, 5, 1, 8, 30))
                .build();
        AttendanceDevicePunch laterPunch = punch(2L, 101L, LocalDateTime.of(2026, 5, 1, 17, 45));

        when(punchRepository.findByProcessedFalseOrderByPunchTimeAsc()).thenReturn(List.of(laterPunch), List.of());
        when(employeeRepository.findByFingerprintUserId(101L)).thenReturn(Optional.of(employee));
        when(summaryRepository.findByEmployeeIdAndAttendanceDate(1L, date)).thenReturn(Optional.of(existingSummary));
        when(summaryRepository.save(any(AttendanceDailySummary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendancePunchProcessResponse firstRun = service.processUnprocessedPunches();
        AttendancePunchProcessResponse secondRun = service.processUnprocessedPunches();

        assertEquals(1, firstRun.getProcessedPunchCount());
        assertEquals(0, firstRun.getSummaryCreatedCount());
        assertEquals(1, firstRun.getSummaryUpdatedCount());
        assertEquals(LocalDateTime.of(2026, 5, 1, 8, 30), existingSummary.getCheckInTime());
        assertEquals(LocalDateTime.of(2026, 5, 1, 17, 45), existingSummary.getCheckOutTime());

        assertEquals(0, secondRun.getProcessedPunchCount());
        assertEquals(0, secondRun.getSummaryCreatedCount());
        assertEquals(0, secondRun.getSummaryUpdatedCount());

        verify(summaryRepository, times(1)).save(any(AttendanceDailySummary.class));
    }

    @Test
    public void processUnknownTerminalUserDoesNotCrashAndLeavesPunchUnprocessed() {
        AttendanceDevicePunch unknownPunch = punch(1L, 999L, LocalDateTime.of(2026, 5, 1, 8, 15));

        when(punchRepository.findByProcessedFalseOrderByPunchTimeAsc()).thenReturn(List.of(unknownPunch));
        when(employeeRepository.findByFingerprintUserId(999L)).thenReturn(Optional.empty());

        AttendancePunchProcessResponse response = service.processUnprocessedPunches();

        assertEquals(0, response.getProcessedPunchCount());
        assertEquals(0, response.getSummaryCreatedCount());
        assertEquals(0, response.getSummaryUpdatedCount());
        assertEquals(1, response.getUnknownUserCount());
        assertEquals(1, response.getErrors().size());
        assertFalse(unknownPunch.getProcessed());

        verify(summaryRepository, never()).save(any(AttendanceDailySummary.class));
        verify(punchRepository, never()).saveAll(anyList());
    }

    private Employee employee(Long id, Long fingerprintUserId) {
        return Employee.builder()
                .id(id)
                .fingerprintUserId(fingerprintUserId)
                .build();
    }

    private AttendanceDevicePunch punch(Long id, Long terminalUserId, LocalDateTime punchTime) {
        return AttendanceDevicePunch.builder()
                .id(id)
                .terminalUserId(terminalUserId)
                .punchTime(punchTime)
                .sourceRecordKey("SRC-" + id)
                .processed(false)
                .build();
    }
}
