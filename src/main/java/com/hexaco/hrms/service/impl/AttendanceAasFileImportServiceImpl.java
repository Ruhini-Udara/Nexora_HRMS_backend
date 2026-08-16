package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.AttendanceAasFileImportResponse;
import com.hexaco.hrms.dto.AttendancePunchBatchRequest;
import com.hexaco.hrms.dto.AttendancePunchBatchResponse;
import com.hexaco.hrms.dto.AttendancePunchProcessResponse;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.service.AttendanceAasFileImportService;
import com.hexaco.hrms.service.AttendancePunchIngestionService;
import com.hexaco.hrms.service.AttendancePunchProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AttendanceAasFileImportServiceImpl implements AttendanceAasFileImportService {

    private static final String DEFAULT_DEVICE_CODE = "DEVICE-001";
    private static final DateTimeFormatter SOURCE_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("M/d/uuuu")
    );
    private static final List<DateTimeFormatter> PUNCH_TIME_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("H:mm:ss")
    );

    private final AttendancePunchIngestionService attendancePunchIngestionService;
    private final AttendancePunchProcessingService attendancePunchProcessingService;
    private final EmployeeRepository employeeRepository;

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
            int staffCodeIndex = findHeaderIndex(headers, "Staff Code");
            int dateIndex = findHeaderIndex(headers, "Date");
            List<TimeColumn> timeColumns = findTimeColumns(headers);
            if (staffCodeIndex < 0 || dateIndex < 0 || timeColumns.isEmpty()) {
                throw new RuntimeException("CSV must include Staff Code, Date, and Time1-Time12 columns");
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
                    if (isIgnorableExportRow(columns, staffCodeIndex, dateIndex, timeColumns)) {
                        continue;
                    }

                    String staffCodeValue = getColumn(columns, staffCodeIndex, "Staff Code");
                    String dateValue = getColumn(columns, dateIndex, "Date");
                    Long terminalUserId = resolveTerminalUserId(staffCodeValue);
                    LocalDate attendanceDate = parseDate(dateValue.trim());
                    boolean hasTimeValue = false;

                    for (TimeColumn timeColumn : timeColumns) {
                        String timeValue = getOptionalColumn(columns, timeColumn.index());
                        if (timeValue == null || timeValue.trim().isBlank()) {
                            continue;
                        }

                        hasTimeValue = true;
                        try {
                            LocalDateTime punchTime = LocalDateTime.of(
                                    attendanceDate,
                                    parsePunchTime(timeValue.trim())
                            );
                            punches.add(buildPunch(deviceCode, terminalUserId, punchTime, rowNumber, timeColumn.name()));
                        } catch (Exception e) {
                            errors.add("row " + rowNumber + " " + timeColumn.name() + ": " + e.getMessage());
                        }
                    }

                    if (!hasTimeValue) {
                        errors.add("row " + rowNumber + ": at least one Time1-Time12 value is required");
                    }
                } catch (Exception e) {
                    errors.add("row " + rowNumber + ": " + e.getMessage());
                }
            }

            return punches;
        } catch (Exception e) {
            throw new RuntimeException("Unable to read AAS CSV file: " + e.getMessage());
        }
    }

    private AttendancePunchBatchRequest.Punch buildPunch(
            String deviceCode,
            Long terminalUserId,
            LocalDateTime punchTime,
            int rowNumber,
            String timeColumnName) {
        AttendancePunchBatchRequest.Punch punch = new AttendancePunchBatchRequest.Punch();
        punch.setTerminalUserId(terminalUserId);
        punch.setPunchTime(punchTime);
        punch.setSourceRecordKey(deviceCode + "-" + terminalUserId + "-"
                + punchTime.format(SOURCE_KEY_FORMATTER));
        punch.setRawPayload("{\"source\":\"AAS CSV\",\"row\":" + rowNumber
                + ",\"timeColumn\":\"" + timeColumnName + "\"}");
        return punch;
    }

    private LocalDate parseDate(String value) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (Exception ignored) {
            }
        }
        throw new RuntimeException("Date must be in M/d/yyyy format");
    }

    private LocalTime parsePunchTime(String value) {
        for (DateTimeFormatter formatter : PUNCH_TIME_FORMATTERS) {
            try {
                return LocalTime.parse(value, formatter);
            } catch (Exception ignored) {
            }
        }
        throw new RuntimeException("time must be in H:mm or H:mm:ss format");
    }

    private Long resolveTerminalUserId(String staffCodeValue) {
        String normalizedStaffCode = normalizeStaffCode(staffCodeValue);
        Employee employee = employeeRepository.findByEmployeeCode(normalizedStaffCode)
                .orElseThrow(() -> new RuntimeException("employee not found for Staff Code " + normalizedStaffCode));
        if (employee.getFingerprintUserId() == null) {
            throw new RuntimeException("employee " + normalizedStaffCode + " has no fingerprintUserId");
        }
        return employee.getFingerprintUserId();
    }

    private String normalizeStaffCode(String value) {
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        while (normalized.startsWith("0") && normalized.length() > 1) {
            normalized = normalized.substring(1);
        }
        if (normalized.isBlank()) {
            throw new RuntimeException("Staff Code is required");
        }
        return normalized;
    }

    private int findHeaderIndex(List<String> headers, String headerName) {
        for (int i = 0; i < headers.size(); i++) {
            if (headerName.equalsIgnoreCase(headers.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private List<TimeColumn> findTimeColumns(List<String> headers) {
        List<TimeColumn> timeColumns = new ArrayList<>();
        for (int sequence = 1; sequence <= 12; sequence++) {
            String columnName = "Time" + sequence;
            int index = findHeaderIndex(headers, columnName);
            if (index >= 0) {
                timeColumns.add(new TimeColumn(index, columnName));
            }
        }
        return timeColumns;
    }

    private boolean isIgnorableExportRow(
            List<String> columns,
            int staffCodeIndex,
            int dateIndex,
            List<TimeColumn> timeColumns) {
        return isBlankColumn(columns, staffCodeIndex)
                && isBlankColumn(columns, dateIndex)
                && timeColumns.stream().allMatch(timeColumn -> isBlankColumn(columns, timeColumn.index()));
    }

    private boolean isBlankColumn(List<String> columns, int index) {
        String value = getOptionalColumn(columns, index);
        return value == null || value.trim().isBlank();
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

    private String getOptionalColumn(List<String> columns, int index) {
        if (index >= columns.size()) {
            return null;
        }
        return columns.get(index);
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

    private record TimeColumn(int index, String name) {
    }
}
