package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.AttendancePunchBatchRequest;
import com.hexaco.hrms.dto.AttendancePunchBatchResponse;
import com.hexaco.hrms.models.AttendanceDevice;
import com.hexaco.hrms.models.AttendanceDevicePunch;
import com.hexaco.hrms.repository.AttendanceDevicePunchRepository;
import com.hexaco.hrms.repository.AttendanceDeviceRepository;
import com.hexaco.hrms.service.AttendancePunchIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendancePunchIngestionServiceImpl implements AttendancePunchIngestionService {

    private final AttendanceDeviceRepository attendanceDeviceRepository;
    private final AttendanceDevicePunchRepository attendanceDevicePunchRepository;
    private final PlatformTransactionManager transactionManager;

    @Override
    public AttendancePunchBatchResponse ingestBatch(AttendancePunchBatchRequest request) {
        validateRequest(request);

        String deviceCode = request.getDeviceCode().trim();
        AttendanceDevice device = attendanceDeviceRepository.findByDeviceCodeIgnoreCase(deviceCode)
                .orElseThrow(() -> new RuntimeException("Attendance device not found with code: " + deviceCode));

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        int insertedCount = 0;
        int duplicateCount = 0;
        int failedCount = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < request.getPunches().size(); i++) {
            AttendancePunchBatchRequest.Punch punch = request.getPunches().get(i);
            try {
                validatePunch(punch);
                PunchResult result = transactionTemplate.execute(status -> savePunch(device, punch, status));
                if (PunchResult.DUPLICATE.equals(result)) {
                    duplicateCount++;
                } else {
                    insertedCount++;
                }
            } catch (Exception e) {
                failedCount++;
                errors.add("punches[" + i + "]: " + e.getMessage());
            }
        }

        return AttendancePunchBatchResponse.builder()
                .insertedCount(insertedCount)
                .duplicateCount(duplicateCount)
                .failedCount(failedCount)
                .errors(errors)
                .build();
    }

    private PunchResult savePunch(
            AttendanceDevice device,
            AttendancePunchBatchRequest.Punch punch,
            TransactionStatus status) {
        String sourceRecordKey = punch.getSourceRecordKey().trim();

        if (attendanceDevicePunchRepository.existsByAttendanceDevice_IdAndSourceRecordKey(device.getId(), sourceRecordKey)) {
            return PunchResult.DUPLICATE;
        }

        try {
            AttendanceDevicePunch entity = AttendanceDevicePunch.builder()
                    .attendanceDevice(device)
                    .terminalUserId(punch.getTerminalUserId())
                    .punchTime(punch.getPunchTime())
                    .sourceRecordKey(sourceRecordKey)
                    .rawPayload(trimToNull(punch.getRawPayload()))
                    .processed(false)
                    .build();

            attendanceDevicePunchRepository.saveAndFlush(entity);
            return PunchResult.INSERTED;
        } catch (DataIntegrityViolationException e) {
            status.setRollbackOnly();
            return PunchResult.DUPLICATE;
        } catch (RuntimeException e) {
            status.setRollbackOnly();
            throw e;
        }
    }

    private void validateRequest(AttendancePunchBatchRequest request) {
        if (request == null) {
            throw new RuntimeException("Request body is required");
        }
        if (request.getDeviceCode() == null || request.getDeviceCode().trim().isBlank()) {
            throw new RuntimeException("deviceCode is required");
        }
        if (request.getPunches() == null) {
            throw new RuntimeException("punches is required");
        }
    }

    private void validatePunch(AttendancePunchBatchRequest.Punch punch) {
        if (punch == null) {
            throw new RuntimeException("punch is required");
        }
        if (punch.getTerminalUserId() == null) {
            throw new RuntimeException("terminalUserId is required");
        }
        if (punch.getPunchTime() == null) {
            throw new RuntimeException("punchTime is required");
        }
        if (punch.getSourceRecordKey() == null || punch.getSourceRecordKey().trim().isBlank()) {
            throw new RuntimeException("sourceRecordKey is required");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private enum PunchResult {
        INSERTED,
        DUPLICATE
    }
}
