package com.hexaco.hrms.dto;

import com.hexaco.hrms.models.AttendanceDevice;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttendanceDeviceDto {
    private Long id;
    private String deviceCode;
    private String name;
    private AttendanceDevice.SourceType sourceType;
    private String ipAddress;
    private Integer port;
    private Integer machineId;
    private Boolean active;
    private LocalDateTime lastSyncAt;
    private LocalDateTime createdAt;
}
