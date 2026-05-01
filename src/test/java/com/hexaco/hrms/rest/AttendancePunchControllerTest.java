package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.AttendancePunchProcessResponse;
import com.hexaco.hrms.service.AttendancePunchIngestionService;
import com.hexaco.hrms.service.AttendancePunchProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AttendancePunchControllerTest {

    private final AttendancePunchIngestionService attendancePunchIngestionService =
            mock(AttendancePunchIngestionService.class);
    private final AttendancePunchProcessingService attendancePunchProcessingService =
            mock(AttendancePunchProcessingService.class);
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new AttendancePunchController(attendancePunchIngestionService, attendancePunchProcessingService)
        ).build();
    }

    @Test
    public void processPunchesReturnsProcessingSummary() throws Exception {
        AttendancePunchProcessResponse response = AttendancePunchProcessResponse.builder()
                .processedPunchCount(2)
                .summaryCreatedCount(1)
                .summaryUpdatedCount(0)
                .unknownUserCount(1)
                .errors(List.of("punchId=3 terminalUserId=999 has no matching employee fingerprintUserId"))
                .build();

        when(attendancePunchProcessingService.processUnprocessedPunches()).thenReturn(response);

        mockMvc.perform(post("/api/attendance/punches/process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processedPunchCount").value(2))
                .andExpect(jsonPath("$.summaryCreatedCount").value(1))
                .andExpect(jsonPath("$.summaryUpdatedCount").value(0))
                .andExpect(jsonPath("$.unknownUserCount").value(1))
                .andExpect(jsonPath("$.errors[0]").value("punchId=3 terminalUserId=999 has no matching employee fingerprintUserId"));
    }
}
