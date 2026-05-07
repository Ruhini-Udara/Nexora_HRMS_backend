package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.AttendanceAasFileImportResponse;
import com.hexaco.hrms.dto.AttendancePunchBatchRequest;
import com.hexaco.hrms.dto.AttendancePunchBatchResponse;
import com.hexaco.hrms.dto.AttendancePunchProcessResponse;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.service.AttendancePunchIngestionService;
import com.hexaco.hrms.service.AttendancePunchProcessingService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AttendanceAasFileImportServiceImplTest {

    private final AttendancePunchIngestionService ingestionService = mock(AttendancePunchIngestionService.class);
    private final AttendancePunchProcessingService processingService = mock(AttendancePunchProcessingService.class);
    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    private final AttendanceAasFileImportServiceImpl service =
            new AttendanceAasFileImportServiceImpl(ingestionService, processingService, employeeRepository);

    @Test
    public void importAasFileMapsRealAasCsvTimesAndProcessesPunches() {
        when(employeeRepository.findByEmployeeCode("EMP045")).thenReturn(Optional.of(employee(9001L)));
        when(ingestionService.ingestBatch(any(AttendancePunchBatchRequest.class))).thenReturn(
                AttendancePunchBatchResponse.builder()
                        .insertedCount(2)
                        .duplicateCount(0)
                        .failedCount(0)
                        .errors(List.of())
                        .build()
        );
        when(processingService.processUnprocessedPunches()).thenReturn(
                AttendancePunchProcessResponse.builder()
                        .processedPunchCount(2)
                        .summaryCreatedCount(1)
                        .summaryUpdatedCount(0)
                        .unknownUserCount(0)
                        .errors(List.of())
                        .build()
        );

        AttendanceAasFileImportResponse response = service.importAasFile(
                csvFile(realAasHeader() + "\n"
                        + "Company,Rashmi Bims,00EMP045,5/7/2026,Thursday,18:25,18:29,,,,,,,,,,,"),
                "DEVICE-001"
        );

        ArgumentCaptor<AttendancePunchBatchRequest> captor =
                ArgumentCaptor.forClass(AttendancePunchBatchRequest.class);
        verify(ingestionService).ingestBatch(captor.capture());
        verify(processingService).processUnprocessedPunches();

        AttendancePunchBatchRequest request = captor.getValue();
        assertEquals("DEVICE-001", request.getDeviceCode());
        assertEquals(2, request.getPunches().size());
        assertEquals(9001L, request.getPunches().get(0).getTerminalUserId());
        assertEquals(LocalDateTime.of(2026, 5, 7, 18, 25), request.getPunches().get(0).getPunchTime());
        assertEquals("DEVICE-001-9001-20260507182500", request.getPunches().get(0).getSourceRecordKey());
        assertEquals(LocalDateTime.of(2026, 5, 7, 18, 29), request.getPunches().get(1).getPunchTime());
        verify(employeeRepository).findByEmployeeCode("EMP045");
        assertEquals(2, response.getInsertedCount());
        assertEquals(0, response.getDuplicateCount());
        assertEquals(0, response.getFailedCount());
    }

    @Test
    public void importAasFileKeepsMalformedRowErrorsAndIngestsValidPunches() {
        when(employeeRepository.findByEmployeeCode("EMP045")).thenReturn(Optional.of(employee(45L)));
        when(employeeRepository.findByEmployeeCode("EMP046")).thenReturn(Optional.of(employee(46L)));
        when(employeeRepository.findByEmployeeCode("EMP047")).thenReturn(Optional.empty());
        when(ingestionService.ingestBatch(any(AttendancePunchBatchRequest.class))).thenReturn(
                AttendancePunchBatchResponse.builder()
                        .insertedCount(1)
                        .duplicateCount(0)
                        .failedCount(0)
                        .errors(List.of())
                        .build()
        );
        when(processingService.processUnprocessedPunches()).thenReturn(
                AttendancePunchProcessResponse.builder()
                        .processedPunchCount(1)
                        .summaryCreatedCount(1)
                        .summaryUpdatedCount(0)
                        .unknownUserCount(0)
                        .errors(List.of())
                        .build()
        );

        AttendanceAasFileImportResponse response = service.importAasFile(
                csvFile(realAasHeader() + "\n"
                        + "Company,Rashmi Bims,00EMP045,5/7/2026,Thursday,bad-time,,,,,,,,,,,,\n"
                        + "Company,Nimal Perera,00EMP046,5/7/2026,Thursday,08:15,,,,,,,,,,,,\n"
                        + "Company,Unknown Person,00EMP047,5/7/2026,Thursday,09:00,,,,,,,,,,,,\n"
                        + "Company,Missing Date,00EMP045,,Thursday,09:00,,,,,,,,,,,,"),
                "DEVICE-001"
        );

        ArgumentCaptor<AttendancePunchBatchRequest> captor =
                ArgumentCaptor.forClass(AttendancePunchBatchRequest.class);
        verify(ingestionService).ingestBatch(captor.capture());

        assertEquals(1, captor.getValue().getPunches().size());
        assertEquals(46L, captor.getValue().getPunches().get(0).getTerminalUserId());
        assertEquals(LocalDateTime.of(2026, 5, 7, 8, 15), captor.getValue().getPunches().get(0).getPunchTime());
        assertEquals(1, response.getInsertedCount());
        assertEquals(3, response.getFailedCount());
        assertEquals(3, response.getErrors().size());
        assertTrue(response.getErrors().get(0).startsWith("row 2 Time1:"));
        assertTrue(response.getErrors().get(1).contains("employee not found for Staff Code EMP047"));
        assertTrue(response.getErrors().get(2).contains("Date is required"));
    }

    @Test
    public void importAasFileIgnoresEmptyAndFooterRowsWithoutFailedCount() {
        when(employeeRepository.findByEmployeeCode("EMP045")).thenReturn(Optional.of(employee(45L)));
        when(ingestionService.ingestBatch(any(AttendancePunchBatchRequest.class))).thenReturn(
                AttendancePunchBatchResponse.builder()
                        .insertedCount(1)
                        .duplicateCount(0)
                        .failedCount(0)
                        .errors(List.of())
                        .build()
        );
        when(processingService.processUnprocessedPunches()).thenReturn(
                AttendancePunchProcessResponse.builder()
                        .processedPunchCount(1)
                        .summaryCreatedCount(1)
                        .summaryUpdatedCount(0)
                        .unknownUserCount(0)
                        .errors(List.of())
                        .build()
        );

        AttendanceAasFileImportResponse response = service.importAasFile(
                csvFile(realAasHeader() + "\n"
                        + "Company,Rashmi Bims,00EMP045,5/7/2026,Thursday,18:25,,,,,,,,,,,,\n"
                        + ",,,,,,,,,,,,,,,,,\n"
                        + "Report Total,,,,,,,,,,,,,,,,,\n"
                        + "Generated By AAS,,,,,,,,,,,,,,,,,\n"),
                "DEVICE-001"
        );

        ArgumentCaptor<AttendancePunchBatchRequest> captor =
                ArgumentCaptor.forClass(AttendancePunchBatchRequest.class);
        verify(ingestionService).ingestBatch(captor.capture());

        assertEquals(1, captor.getValue().getPunches().size());
        assertEquals(45L, captor.getValue().getPunches().get(0).getTerminalUserId());
        assertEquals(1, response.getInsertedCount());
        assertEquals(0, response.getDuplicateCount());
        assertEquals(0, response.getFailedCount());
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    public void importAasFileIncludesProcessingErrorsWithoutIncreasingFailedCount() {
        when(employeeRepository.findByEmployeeCode("EMP999")).thenReturn(Optional.of(employee(999L)));
        when(ingestionService.ingestBatch(any(AttendancePunchBatchRequest.class))).thenReturn(
                AttendancePunchBatchResponse.builder()
                        .insertedCount(1)
                        .duplicateCount(0)
                        .failedCount(0)
                        .errors(List.of())
                        .build()
        );
        when(processingService.processUnprocessedPunches()).thenReturn(
                AttendancePunchProcessResponse.builder()
                        .processedPunchCount(0)
                        .summaryCreatedCount(0)
                        .summaryUpdatedCount(0)
                        .unknownUserCount(1)
                        .errors(List.of("punchId=1 terminalUserId=999 has no matching employee fingerprintUserId"))
                        .build()
        );

        AttendanceAasFileImportResponse response = service.importAasFile(
                csvFile(realAasHeader() + "\n"
                        + "Company,Unknown Person,00EMP999,5/2/2026,Saturday,08:30,,,,,,,,,,,,"),
                "DEVICE-001"
        );

        assertEquals(1, response.getInsertedCount());
        assertEquals(0, response.getFailedCount());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    public void importAasFileRequiresCsvFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "aas.csv",
                "text/csv",
                new byte[0]
        );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.importAasFile(emptyFile, "DEVICE-001")
        );

        assertEquals("CSV file is required", exception.getMessage());
    }

    @Test
    public void importAasFileRequiresRealAasColumns() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> service.importAasFile(csvFile("UserID,Time\n101,2026-05-02 08:30:00\n"), "DEVICE-001")
        );

        assertTrue(exception.getMessage().contains("CSV must include Staff Code, Date, and Time1-Time12 columns"));
    }

    @Test
    public void importAasFileFailsEmployeeWithoutFingerprintUserId() {
        when(employeeRepository.findByEmployeeCode("EMP045")).thenReturn(Optional.of(employee(null)));
        when(ingestionService.ingestBatch(any(AttendancePunchBatchRequest.class))).thenReturn(
                AttendancePunchBatchResponse.builder()
                        .insertedCount(0)
                        .duplicateCount(0)
                        .failedCount(0)
                        .errors(List.of())
                        .build()
        );
        when(processingService.processUnprocessedPunches()).thenReturn(
                AttendancePunchProcessResponse.builder()
                        .processedPunchCount(0)
                        .summaryCreatedCount(0)
                        .summaryUpdatedCount(0)
                        .unknownUserCount(0)
                        .errors(List.of())
                        .build()
        );

        AttendanceAasFileImportResponse response = service.importAasFile(
                csvFile(realAasHeader() + "\n"
                        + "Company,Rashmi Bims,00EMP045,5/7/2026,Thursday,18:25,,,,,,,,,,,,"),
                "DEVICE-001"
        );

        assertEquals(0, response.getInsertedCount());
        assertEquals(1, response.getFailedCount());
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().get(0).contains("employee EMP045 has no fingerprintUserId"));
    }

    private MockMultipartFile csvFile(String content) {
        return new MockMultipartFile(
                "file",
                "aas.csv",
                "text/csv",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String realAasHeader() {
        return "Department,Name,Staff Code,Date,Week,Time1,Time2,Time3,Time4,Time5,Time6,"
                + "Time7,Time8,Time9,Time10,Time11,Time12,Remark";
    }

    private Employee employee(Long fingerprintUserId) {
        return Employee.builder()
                .fingerprintUserId(fingerprintUserId)
                .build();
    }
}
