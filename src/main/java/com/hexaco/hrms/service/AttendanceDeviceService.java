package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.AttendanceDeviceDto;

import java.util.List;

public interface AttendanceDeviceService {
    AttendanceDeviceDto createDevice(AttendanceDeviceDto dto);
    List<AttendanceDeviceDto> getAllDevices();
    List<AttendanceDeviceDto> getActiveDevicesForIngestion();
    AttendanceDeviceDto updateDevice(Long id, AttendanceDeviceDto dto);
}
