package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.AttendanceDeviceDto;
import com.hexaco.hrms.models.AttendanceDevice;
import com.hexaco.hrms.repository.AttendanceDeviceRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AttendanceDeviceServiceImplTest {

    private final AttendanceDeviceRepository attendanceDeviceRepository = mock(AttendanceDeviceRepository.class);
    private final AttendanceDeviceServiceImpl attendanceDeviceService =
            new AttendanceDeviceServiceImpl(attendanceDeviceRepository);

    @Test
    public void createDeviceDefaultsActiveAndStoresMockConfiguration() {
        AttendanceDeviceDto request = new AttendanceDeviceDto();
        request.setDeviceCode("MOCK-AAS-001");
        request.setName("Mock AAS Device");
        request.setSourceType(AttendanceDevice.SourceType.MOCK);
        request.setMachineId(1);

        when(attendanceDeviceRepository.findByDeviceCodeIgnoreCase("MOCK-AAS-001")).thenReturn(Optional.empty());
        when(attendanceDeviceRepository.save(any(AttendanceDevice.class))).thenAnswer(invocation -> {
            AttendanceDevice device = invocation.getArgument(0);
            device.setId(1L);
            return device;
        });

        AttendanceDeviceDto created = attendanceDeviceService.createDevice(request);

        assertEquals("MOCK-AAS-001", created.getDeviceCode());
        assertEquals(AttendanceDevice.SourceType.MOCK, created.getSourceType());
        assertEquals(1, created.getMachineId());
        assertEquals(true, created.getActive());
    }

    @Test
    public void getActiveDevicesForIngestionExcludesInactiveDevices() {
        AttendanceDevice activeDevice = AttendanceDevice.builder()
                .id(1L)
                .deviceCode("MOCK-AAS-001")
                .name("Mock AAS Device")
                .sourceType(AttendanceDevice.SourceType.MOCK)
                .machineId(1)
                .active(true)
                .build();

        when(attendanceDeviceRepository.findByActiveTrue()).thenReturn(List.of(activeDevice));

        List<AttendanceDeviceDto> activeDevices = attendanceDeviceService.getActiveDevicesForIngestion();

        assertEquals(1, activeDevices.size());
        assertEquals("MOCK-AAS-001", activeDevices.get(0).getDeviceCode());
        assertFalse(activeDevices.stream().anyMatch(device -> Boolean.FALSE.equals(device.getActive())));
    }

    @Test
    public void createDeviceRejectsDuplicateDeviceCode() {
        AttendanceDeviceDto request = new AttendanceDeviceDto();
        request.setDeviceCode("MOCK-AAS-001");
        request.setName("Mock AAS Device");
        request.setSourceType(AttendanceDevice.SourceType.MOCK);

        when(attendanceDeviceRepository.findByDeviceCodeIgnoreCase("MOCK-AAS-001"))
                .thenReturn(Optional.of(AttendanceDevice.builder().id(1L).deviceCode("MOCK-AAS-001").build()));

        assertThrows(RuntimeException.class, () -> attendanceDeviceService.createDevice(request));
    }
}
