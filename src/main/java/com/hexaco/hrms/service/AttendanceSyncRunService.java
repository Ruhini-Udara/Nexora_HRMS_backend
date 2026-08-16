package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.AttendanceSyncRunDto;

import java.util.List;

public interface AttendanceSyncRunService {
    List<AttendanceSyncRunDto> getSyncRuns();
}
