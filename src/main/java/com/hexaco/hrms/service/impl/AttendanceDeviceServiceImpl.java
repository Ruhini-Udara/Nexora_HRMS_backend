package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.AttendanceDeviceDto;
import com.hexaco.hrms.models.AttendanceDevice;
import com.hexaco.hrms.repository.AttendanceDeviceRepository;
import com.hexaco.hrms.service.AttendanceDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceDeviceServiceImpl implements AttendanceDeviceService {

    private final AttendanceDeviceRepository attendanceDeviceRepository;

    @Override
    @Transactional
    public AttendanceDeviceDto createDevice(AttendanceDeviceDto dto) {
        validateRequiredCreateFields(dto);

        String deviceCode = dto.getDeviceCode().trim();
        attendanceDeviceRepository.findByDeviceCodeIgnoreCase(deviceCode)
                .ifPresent(existing -> {
                    throw new RuntimeException("Attendance device already exists with code: " + deviceCode);
                });

        AttendanceDevice device = AttendanceDevice.builder()
                .deviceCode(deviceCode)
                .name(dto.getName().trim())
                .sourceType(dto.getSourceType())
                .ipAddress(trimToNull(dto.getIpAddress()))
                .port(dto.getPort())
                .machineId(dto.getMachineId())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .lastSyncAt(dto.getLastSyncAt())
                .build();

        return toDto(attendanceDeviceRepository.save(device));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDeviceDto> getAllDevices() {
        return attendanceDeviceRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDeviceDto> getActiveDevicesForIngestion() {
        return attendanceDeviceRepository.findByActiveTrue()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public AttendanceDeviceDto updateDevice(Long id, AttendanceDeviceDto dto) {
        AttendanceDevice device = attendanceDeviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attendance device not found with id: " + id));

        if (dto.getDeviceCode() != null) {
            String deviceCode = dto.getDeviceCode().trim();
            if (deviceCode.isBlank()) {
                throw new RuntimeException("deviceCode cannot be blank");
            }
            attendanceDeviceRepository.findByDeviceCodeIgnoreCase(deviceCode)
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new RuntimeException("Attendance device already exists with code: " + deviceCode);
                    });
            device.setDeviceCode(deviceCode);
        }
        if (dto.getName() != null) {
            String name = dto.getName().trim();
            if (name.isBlank()) {
                throw new RuntimeException("name cannot be blank");
            }
            device.setName(name);
        }
        if (dto.getSourceType() != null) {
            device.setSourceType(dto.getSourceType());
        }
        if (dto.getIpAddress() != null) {
            device.setIpAddress(trimToNull(dto.getIpAddress()));
        }
        if (dto.getPort() != null) {
            device.setPort(dto.getPort());
        }
        if (dto.getMachineId() != null) {
            device.setMachineId(dto.getMachineId());
        }
        if (dto.getActive() != null) {
            device.setActive(dto.getActive());
        }
        if (dto.getLastSyncAt() != null) {
            device.setLastSyncAt(dto.getLastSyncAt());
        }

        return toDto(attendanceDeviceRepository.save(device));
    }

    private void validateRequiredCreateFields(AttendanceDeviceDto dto) {
        if (dto.getDeviceCode() == null || dto.getDeviceCode().trim().isBlank()) {
            throw new RuntimeException("deviceCode is required");
        }
        if (dto.getName() == null || dto.getName().trim().isBlank()) {
            throw new RuntimeException("name is required");
        }
        if (dto.getSourceType() == null) {
            throw new RuntimeException("sourceType is required");
        }
    }

    private AttendanceDeviceDto toDto(AttendanceDevice device) {
        AttendanceDeviceDto dto = new AttendanceDeviceDto();
        dto.setId(device.getId());
        dto.setDeviceCode(device.getDeviceCode());
        dto.setName(device.getName());
        dto.setSourceType(device.getSourceType());
        dto.setIpAddress(device.getIpAddress());
        dto.setPort(device.getPort());
        dto.setMachineId(device.getMachineId());
        dto.setActive(device.getActive());
        dto.setLastSyncAt(device.getLastSyncAt());
        dto.setCreatedAt(device.getCreatedAt());
        return dto;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
