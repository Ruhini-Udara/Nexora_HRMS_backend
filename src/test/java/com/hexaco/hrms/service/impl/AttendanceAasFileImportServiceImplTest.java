package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.AttendanceAasFileImportResponse;
import com.hexaco.hrms.dto.AttendancePunchBatchRequest;
import com.hexaco.hrms.dto.AttendancePunchBatchResponse;
import com.hexaco.hrms.dto.AttendancePunchProcessResponse;
import com.hexaco.hrms.service.AttendancePunchIngestionService;
import com.hexaco.hrms.service.AttendancePunchProcessingService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

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
    private final AttendanceAasFileImportServiceImpl service =
            new AttendanceAasFileImportServiceImpl(ingestionService, processingService);

    @Test
    public void importAasFileMapsCsvRowsAndProcessesPunches() {
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
                csvFile("UserID,Time\n101,2026-05-02 08:30:00\n101,2026-05-02T17:45:00\n"),
                "DEVICE-001"
        );

        ArgumentCaptor<AttendancePunchBatchRequest> captor =
                ArgumentCaptor.forClass(AttendancePunchBatchRequest.class);
        verify(ingestionService).ingestBatch(captor.capture());
        verify(processingService).processUnprocessedPunches();

        AttendancePunchBatchRequest request = captor.getValue();
        assertEquals("DEVICE-001", request.getDeviceCode());
        assertEquals(2, request.getPunches().size());
        assertEquals(101L, request.getPunches().get(0).getTerminalUserId());
        assertEquals(LocalDateTime.of(2026, 5, 2, 8, 30), request.getPunches().get(0).getPunchTime());
        assertEquals("DEVICE-001-101-20260502083000", request.getPunches().get(0).getSourceRecordKey());
        assertEquals(2, response.getInsertedCount());
        assertEquals(0, response.getDuplicateCount());
        assertEquals(0, response.getFailedCount());
    }

    @Test
    public void importAasFileKeepsRowErrorsAndIngestsValidRows() {
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
                csvFile("UserID,Time\n101,not-a-date\n102,2026-05-02 17:45:00\n"),
                "DEVICE-001"
        );

        ArgumentCaptor<AttendancePunchBatchRequest> captor =
                ArgumentCaptor.forClass(AttendancePunchBatchRequest.class);
        verify(ingestionService).ingestBatch(captor.capture());

        assertEquals(1, captor.getValue().getPunches().size());
        assertEquals(102L, captor.getValue().getPunches().get(0).getTerminalUserId());
        assertEquals(1, response.getInsertedCount());
        assertEquals(1, response.getFailedCount());
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().get(0).startsWith("row 2:"));
    }

    @Test
    public void importAasFileIncludesProcessingErrorsWithoutIncreasingFailedCount() {
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
                csvFile("UserID,Time\n999,2026-05-02 08:30:00\n"),
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

    private MockMultipartFile csvFile(String content) {
        return new MockMultipartFile(
                "file",
                "aas.csv",
                "text/csv",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }
}
