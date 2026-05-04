package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.AttendanceAasFileImportResponse;
import com.hexaco.hrms.dto.AttendancePunchBatchRequest;
import com.hexaco.hrms.dto.AttendancePunchBatchResponse;
import com.hexaco.hrms.dto.AttendancePunchProcessResponse;
import com.hexaco.hrms.service.AttendanceAasFileImportService;
import com.hexaco.hrms.service.AttendancePunchIngestionService;
import com.hexaco.hrms.service.AttendancePunchProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceAasFileImportServiceImpl implements AttendanceAasFileImportService {

    private static final String DEFAULT_DEVICE_CODE = "DEVICE-001";
    private static final DateTimeFormatter SOURCE_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final List<DateTimeFormatter> TIME_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    );

    private final AttendancePunchIngestionService attendancePunchIngestionService;
    private final AttendancePunchProcessingService attendancePunchProcessingService;

    @Override
    public AttendanceAasFileImportResponse importAasFile(MultipartFile file, String deviceCode) {
        validateFile(file);

        String resolvedDeviceCode = resolveDeviceCode(deviceCode);
        List<String> errors = new ArrayList<>();
        List<AttendancePunchBatchRequest.Punch> punches = parseCsv(file, resolvedDeviceCode, errors);
        int csvFailedCount = errors.size();

        AttendancePunchBatchRequest request = new AttendancePunchBatchRequest();
        request.setDeviceCode(resolvedDeviceCode);
        request.setPunches(punches);

        AttendancePunchBatchResponse ingestionResponse = attendancePunchIngestionService.ingestBatch(request);
        AttendancePunchProcessResponse processingResponse = attendancePunchProcessingService.processUnprocessedPunches();

        if (ingestionResponse.getErrors() != null) {
            errors.addAll(ingestionResponse.getErrors());
        }
        if (processingResponse.getErrors() != null) {
            errors.addAll(processingResponse.getErrors());
        }

        return AttendanceAasFileImportResponse.builder()
                .insertedCount(ingestionResponse.getInsertedCount())
                .duplicateCount(ingestionResponse.getDuplicateCount())
                .failedCount(csvFailedCount + ingestionResponse.getFailedCount())
                .errors(errors)
                .build();
    }

    private List<AttendancePunchBatchRequest.Punch> parseCsv(
            MultipartFile file,
            String deviceCode,
            List<String> errors) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new RuntimeException("CSV file is empty");
            }

            List<String> headers = parseCsvLine(headerLine).stream()
                    .map(this::stripBom)
                    .map(String::trim)
                    .toList();
            int userIdIndex = headers.indexOf("UserID");
            int timeIndex = headers.indexOf("Time");
            if (userIdIndex < 0 || timeIndex < 0) {
                throw new RuntimeException("CSV must include UserID and Time columns");
            }

            List<AttendancePunchBatchRequest.Punch> punches = new ArrayList<>();
            String line;
            int rowNumber = 1;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.trim().isBlank()) {
                    continue;
                }

                try {
                    List<String> columns = parseCsvLine(line);
                    String userIdValue = getColumn(columns, userIdIndex, "UserID");
                    String timeValue = getColumn(columns, timeIndex, "Time");
                    Long terminalUserId = Long.valueOf(userIdValue.trim());
                    LocalDateTime punchTime = parseTime(timeValue.trim());

                    AttendancePunchBatchRequest.Punch punch = new AttendancePunchBatchRequest.Punch();
                    punch.setTerminalUserId(terminalUserId);
                    punch.setPunchTime(punchTime);
                    punch.setSourceRecordKey(deviceCode + "-" + terminalUserId + "-"
                            + punchTime.format(SOURCE_KEY_FORMATTER));
                    punch.setRawPayload("{\"source\":\"AAS CSV\",\"row\":" + rowNumber + "}");
                    punches.add(punch);
                } catch (Exception e) {
                    errors.add("row " + rowNumber + ": " + e.getMessage());
                }
            }

            return punches;
        } catch (Exception e) {
            throw new RuntimeException("Unable to read AAS CSV file: " + e.getMessage());
        }
    }

    private LocalDateTime parseTime(String value) {
        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (Exception ignored) {
            }
        }
        throw new RuntimeException("Time must be ISO format or yyyy-MM-dd HH:mm:ss");
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        values.add(current.toString());
        return values;
    }

    private String getColumn(List<String> columns, int index, String columnName) {
        if (index >= columns.size()) {
            throw new RuntimeException(columnName + " is required");
        }

        String value = columns.get(index);
        if (value == null || value.trim().isBlank()) {
            throw new RuntimeException(columnName + " is required");
        }

        return value;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("CSV file is required");
        }
    }

    private String resolveDeviceCode(String deviceCode) {
        if (deviceCode == null || deviceCode.trim().isBlank()) {
            return DEFAULT_DEVICE_CODE;
        }
        return deviceCode.trim();
    }

    private String stripBom(String value) {
        if (value != null && value.startsWith("\uFEFF")) {
            return value.substring(1);
        }
        return value;
    }
}
