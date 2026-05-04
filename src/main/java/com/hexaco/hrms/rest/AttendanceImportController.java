package com.hexaco.hrms.rest;

import com.hexaco.hrms.dto.AttendanceAasFileImportResponse;
import com.hexaco.hrms.service.AttendanceAasFileImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/attendance/import")
@RequiredArgsConstructor
public class AttendanceImportController {

    private final AttendanceAasFileImportService attendanceAasFileImportService;

    @PostMapping(value = "/aas-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttendanceAasFileImportResponse> importAasFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "deviceCode", required = false, defaultValue = "DEVICE-001") String deviceCode) {
        return ResponseEntity.ok(attendanceAasFileImportService.importAasFile(file, deviceCode));
    }
}
