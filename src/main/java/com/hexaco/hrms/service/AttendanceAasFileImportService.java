package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.AttendanceAasFileImportResponse;
import org.springframework.web.multipart.MultipartFile;

public interface AttendanceAasFileImportService {
    AttendanceAasFileImportResponse importAasFile(MultipartFile file, String deviceCode);
}
