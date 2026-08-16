package com.hexaco.hrms.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hexaco.hrms.dto.AttendanceDeviceDto;
import com.hexaco.hrms.models.AttendanceDevice;
import com.hexaco.hrms.service.AttendanceDeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AttendanceDeviceControllerTest {

    private final AttendanceDeviceService attendanceDeviceService = mock(AttendanceDeviceService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AttendanceDeviceController(attendanceDeviceService)).build();
    }

    @Test
    public void createListAndUpdateMockDeviceThroughApi() throws Exception {
        AttendanceDeviceDto mockDevice = mockDevice();
        AttendanceDeviceDto inactiveDevice = mockDevice();
        inactiveDevice.setId(2L);
        inactiveDevice.setDeviceCode("MOCK-AAS-002");
        inactiveDevice.setActive(false);

        when(attendanceDeviceService.createDevice(any(AttendanceDeviceDto.class))).thenReturn(mockDevice);
        when(attendanceDeviceService.getAllDevices()).thenReturn(List.of(mockDevice, inactiveDevice));
        when(attendanceDeviceService.updateDevice(eq(1L), any(AttendanceDeviceDto.class))).thenReturn(inactiveDevice);

        mockMvc.perform(post("/api/attendance/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockDevice)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deviceCode").value("MOCK-AAS-001"))
                .andExpect(jsonPath("$.sourceType").value("MOCK"))
                .andExpect(jsonPath("$.machineId").value(1));

        mockMvc.perform(get("/api/attendance/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceCode").value("MOCK-AAS-001"))
                .andExpect(jsonPath("$[1].active").value(false));

        mockMvc.perform(patch("/api/attendance/devices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    private AttendanceDeviceDto mockDevice() {
        AttendanceDeviceDto dto = new AttendanceDeviceDto();
        dto.setId(1L);
        dto.setDeviceCode("MOCK-AAS-001");
        dto.setName("Mock AAS Device");
        dto.setSourceType(AttendanceDevice.SourceType.MOCK);
        dto.setMachineId(1);
        dto.setActive(true);
        return dto;
    }
}
