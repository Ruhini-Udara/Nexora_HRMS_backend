package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.CarryForwardBatchDetailDto;
import com.hexaco.hrms.dto.CarryForwardBatchDto;
import com.hexaco.hrms.dto.CarryForwardEntryDto;
import com.hexaco.hrms.models.CarryForwardBatch;
import com.hexaco.hrms.models.CarryForwardEntry;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.repository.CarryForwardBatchRepository;
import com.hexaco.hrms.repository.CarryForwardEntryRepository;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.service.CarryForwardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarryForwardServiceImpl implements CarryForwardService {

    private final CarryForwardBatchRepository batchRepository;
    private final CarryForwardEntryRepository entryRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<CarryForwardBatchDto> getAllBatches() {
        List<CarryForwardBatch> batches = batchRepository.findAllByOrderByCreatedAtDesc();
        return batches.stream().map(batch -> CarryForwardBatchDto.builder()
                .id(batch.getId())
                .year(batch.getYear())
                .status(batch.getStatus())
                .submittedBy(batch.getSubmittedBy())
                .approvedBy(batch.getApprovedBy())
                .entriesCount(batch.getEntries() != null ? batch.getEntries().size() : 0)
                .createdAt(batch.getCreatedAt())
                .build()).collect(Collectors.toList());
    }

    @Override
    public CarryForwardBatchDetailDto getBatchDetails(String batchId) {
        CarryForwardBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        List<CarryForwardEntry> entries = entryRepository.findByBatchId(batchId);
        
        // Group by location
        Map<String, List<CarryForwardEntry>> grouped = entries.stream()
                .collect(Collectors.groupingBy(e -> e.getLocation() != null ? e.getLocation() : "Unknown"));

        List<CarryForwardBatchDetailDto.LocationGroupDto> locationDtos = new ArrayList<>();
        
        grouped.forEach((location, locEntries) -> {
            List<CarryForwardEntryDto> entryDtos = locEntries.stream().map(e -> CarryForwardEntryDto.builder()
                    .empId(e.getEmployee().getEmployeeCode())
                    .name(e.getEmployee().getFullName())
                    .department(e.getEmployee().getDepartment())
                    .carriedForwardDays(e.getCarriedForwardDays())
                    .remarks(e.getRemarks())
                    .build()).collect(Collectors.toList());
                    
            locationDtos.add(CarryForwardBatchDetailDto.LocationGroupDto.builder()
                    .locationName(location)
                    .entries(entryDtos)
                    .build());
        });

        return CarryForwardBatchDetailDto.builder()
                .id(batch.getId())
                .year(batch.getYear())
                .status(batch.getStatus())
                .locations(locationDtos)
                .build();
    }

    @Override
    @Transactional
    public CarryForwardBatchDetailDto uploadBatch(MultipartFile file, Integer year, String submittedBy) {
        String batchId = "CF-" + year + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        
        CarryForwardBatch batch = CarryForwardBatch.builder()
                .id(batchId)
                .year(year)
                .status("DRAFT")
                .submittedBy(submittedBy)
                .build();
                
        batch = batchRepository.save(batch);
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // skip header
                    continue;
                }
                String[] values = line.split(",");
                if (values.length >= 3) {
                    String empCode = values[0].trim();
                    String location = values[1].trim();
                    Integer days = Integer.parseInt(values[2].trim());
                    String remarks = values.length > 3 ? values[3].trim() : "";
                    
                    Optional<Employee> empOpt = employeeRepository.findByEmployeeCode(empCode);
                    if (empOpt.isPresent()) {
                        CarryForwardEntry entry = CarryForwardEntry.builder()
                                .batch(batch)
                                .employee(empOpt.get())
                                .location(location)
                                .carriedForwardDays(days)
                                .remarks(remarks)
                                .build();
                        entryRepository.save(entry);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing file", e);
        }
        
        return getBatchDetails(batchId);
    }

    @Override
    @Transactional
    public void updateBatchStatus(String batchId, String status, String approvedBy) {
        CarryForwardBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));
                
        batch.setStatus(status);
        if ("APPROVED".equals(status)) {
            batch.setApprovedBy(approvedBy);
            // Here we would also update the actual leave balances of the employees
        }
        
        batchRepository.save(batch);
    }
}
