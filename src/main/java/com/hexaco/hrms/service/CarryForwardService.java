package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.CarryForwardBatchDto;
import com.hexaco.hrms.dto.CarryForwardBatchDetailDto;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface CarryForwardService {
    List<CarryForwardBatchDto> getAllBatches();
    CarryForwardBatchDetailDto getBatchDetails(String batchId);
    CarryForwardBatchDetailDto uploadBatch(MultipartFile file, Integer year, String submittedBy);
    void updateBatchStatus(String batchId, String status, String approvedBy);
}
