package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.AttendancePunchBatchRequest;
import com.hexaco.hrms.dto.AttendancePunchBatchResponse;

public interface AttendancePunchIngestionService {
    AttendancePunchBatchResponse ingestBatch(AttendancePunchBatchRequest request);
}
