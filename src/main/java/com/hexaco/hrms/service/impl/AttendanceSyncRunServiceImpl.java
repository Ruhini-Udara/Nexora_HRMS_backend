package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.AttendanceSyncRunDto;
import com.hexaco.hrms.models.AttendanceDevice;
import com.hexaco.hrms.models.AttendanceSyncRun;
import com.hexaco.hrms.repository.AttendanceSyncRunRepository;
import com.hexaco.hrms.service.AttendanceSyncRunService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceSyncRunServiceImpl implements AttendanceSyncRunService {

    private final AttendanceSyncRunRepository attendanceSyncRunRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceSyncRunDto> getSyncRuns() {
        return attendanceSyncRunRepository.findAllByOrderByStartedAtDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    private AttendanceSyncRunDto toDto(AttendanceSyncRun syncRun) {
        AttendanceDevice device = syncRun.getAttendanceDevice();
        AttendanceSyncRunDto dto = new AttendanceSyncRunDto();
        dto.setId(syncRun.getId());
        dto.setDeviceCode(device != null ? device.getDeviceCode() : null);
        dto.setDeviceName(device != null ? device.getName() : null);
        dto.setStartedAt(syncRun.getStartedAt());
        dto.setCompletedAt(syncRun.getCompletedAt());
        dto.setReceivedCount(syncRun.getReceivedCount());
        dto.setInsertedCount(syncRun.getInsertedCount());
        dto.setDuplicateCount(syncRun.getDuplicateCount());
        dto.setFailedCount(syncRun.getFailedCount());
        dto.setStatus(syncRun.getStatus() != null ? syncRun.getStatus().name() : null);
        dto.setMessage(syncRun.getMessage());
        return dto;
    }
}
