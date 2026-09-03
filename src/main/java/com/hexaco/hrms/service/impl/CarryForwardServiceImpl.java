package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.CarryForwardBatchDetailDto;
import com.hexaco.hrms.dto.CarryForwardBatchDto;
import com.hexaco.hrms.dto.CarryForwardEntryDto;
import com.hexaco.hrms.models.CarryForwardBatch;
import com.hexaco.hrms.models.CarryForwardEntry;
import com.hexaco.hrms.models.Employee;
import com.hexaco.hrms.models.LeaveBalance;
import com.hexaco.hrms.models.UserAccount;
import com.hexaco.hrms.repository.CarryForwardBatchRepository;
import com.hexaco.hrms.repository.CarryForwardEntryRepository;
import com.hexaco.hrms.repository.EmployeeRepository;
import com.hexaco.hrms.repository.LeaveBalanceRepository;
import com.hexaco.hrms.repository.UserAccountRepository;
import com.hexaco.hrms.service.CarryForwardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarryForwardServiceImpl implements CarryForwardService {

    private final CarryForwardBatchRepository batchRepository;
    private final CarryForwardEntryRepository entryRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final UserAccountRepository userAccountRepository;

    private static final BigDecimal DEFAULT_DAILY_RATE = new BigDecimal("2500.00");

    @Override
    public List<CarryForwardBatchDto> getAllBatches(String userEmail) {
        List<CarryForwardBatch> batches = batchRepository.findAllByOrderByCreatedAtDesc();
        
        return batches.stream().map(batch -> {
            List<CarryForwardEntry> entries = entryRepository.findByBatchId(batch.getId());
            int totalDays = entries.stream().mapToInt(e -> e.getCarriedForwardDays() != null ? e.getCarriedForwardDays() : 0).sum();
            BigDecimal totalCalc = entries.stream()
                    .map(e -> e.getCalculatedAmount() != null ? e.getCalculatedAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalPaid = entries.stream()
                    .map(e -> e.getPaidAmount() != null ? e.getPaidAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return CarryForwardBatchDto.builder()
                    .id(batch.getId())
                    .year(batch.getYear())
                    .status(batch.getStatus())
                    .submittedBy(batch.getSubmittedBy())
                    .approvedBy(batch.getApprovedBy())
                    .financeReferenceId(batch.getFinanceReferenceId())
                    .financeStatus(batch.getFinanceStatus())
                    .auditedBy(batch.getAuditedBy())
                    .auditedAt(batch.getAuditedAt())
                    .entriesCount(entries.size())
                    .totalCarriedDays(totalDays)
                    .totalCalculatedAmount(totalCalc)
                    .totalPaidAmount(totalPaid)
                    .createdAt(batch.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public CarryForwardBatchDetailDto getBatchDetails(String batchId, String userEmail) {
        CarryForwardBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found with ID: " + batchId));

        List<CarryForwardEntry> entries = entryRepository.findByBatchId(batchId);

        // Group entries by employee.branch
        Map<String, List<CarryForwardEntry>> grouped = entries.stream()
                .collect(Collectors.groupingBy(e -> {
                    if (e.getEmployee() != null && e.getEmployee().getBranch() != null && !e.getEmployee().getBranch().isBlank()) {
                        return e.getEmployee().getBranch().trim();
                    }
                    return "Head Office";
                }));

        List<CarryForwardBatchDetailDto.BranchGroupDto> branchDtos = new ArrayList<>();

        BigDecimal batchTotalCalc = BigDecimal.ZERO;
        BigDecimal batchTotalPaid = BigDecimal.ZERO;
        BigDecimal batchTotalAdj = BigDecimal.ZERO;
        int batchTotalDays = 0;

        for (Map.Entry<String, List<CarryForwardEntry>> entryGroup : grouped.entrySet()) {
            String branchName = entryGroup.getKey();
            List<CarryForwardEntry> branchEntries = entryGroup.getValue();

            int branchDays = 0;
            BigDecimal branchCalc = BigDecimal.ZERO;
            BigDecimal branchPaid = BigDecimal.ZERO;
            BigDecimal branchAdj = BigDecimal.ZERO;
            boolean allVerified = !branchEntries.isEmpty();
            String verifiedBy = null;
            LocalDateTime verifiedAt = null;

            List<CarryForwardEntryDto> entryDtos = new ArrayList<>();

            for (CarryForwardEntry e : branchEntries) {
                int days = e.getCarriedForwardDays() != null ? e.getCarriedForwardDays() : 0;
                BigDecimal calc = e.getCalculatedAmount() != null ? e.getCalculatedAmount() : BigDecimal.ZERO;
                BigDecimal paid = e.getPaidAmount() != null ? e.getPaidAmount() : BigDecimal.ZERO;
                BigDecimal adj = e.getAdjustmentAmount() != null ? e.getAdjustmentAmount() : BigDecimal.ZERO;

                branchDays += days;
                branchCalc = branchCalc.add(calc);
                branchPaid = branchPaid.add(paid);
                branchAdj = branchAdj.add(adj);

                if (!Boolean.TRUE.equals(e.getIsBranchVerified())) {
                    allVerified = false;
                } else {
                    verifiedBy = e.getBranchVerifiedBy();
                    verifiedAt = e.getBranchVerifiedAt();
                }

                String empBranch = (e.getEmployee() != null && e.getEmployee().getBranch() != null)
                        ? e.getEmployee().getBranch() : "Head Office";

                entryDtos.add(CarryForwardEntryDto.builder()
                        .id(e.getId())
                        .empId(e.getEmployee() != null ? e.getEmployee().getEmployeeCode() : "N/A")
                        .name(e.getEmployee() != null ? e.getEmployee().getFullName() : "Unknown")
                        .department(e.getEmployee() != null ? e.getEmployee().getDepartment() : "General")
                        .branch(empBranch)
                        .carriedForwardDays(days)
                        .dailyRate(e.getDailyRate())
                        .calculatedAmount(calc)
                        .paidAmount(paid)
                        .isBranchVerified(e.getIsBranchVerified())
                        .branchVerifiedBy(e.getBranchVerifiedBy())
                        .branchVerifiedAt(e.getBranchVerifiedAt())
                        .actualDays(e.getActualDays())
                        .actualAmount(e.getActualAmount())
                        .adjustmentAmount(e.getAdjustmentAmount())
                        .auditStatus(e.getAuditStatus())
                        .adjustmentReason(e.getAdjustmentReason())
                        .payrollApplied(e.getPayrollApplied())
                        .auditedBy(e.getAuditedBy())
                        .auditedAt(e.getAuditedAt())
                        .remarks(e.getRemarks())
                        .build());
            }

            branchDtos.add(CarryForwardBatchDetailDto.BranchGroupDto.builder()
                    .branchName(branchName)
                    .employeeCount(branchEntries.size())
                    .totalCarriedDays(branchDays)
                    .totalCalculatedAmount(branchCalc)
                    .totalPaidAmount(branchPaid)
                    .totalAdjustmentAmount(branchAdj)
                    .isBranchVerified(allVerified)
                    .branchVerifiedBy(verifiedBy)
                    .branchVerifiedAt(verifiedAt)
                    .entries(entryDtos)
                    .build());

            batchTotalDays += branchDays;
            batchTotalCalc = batchTotalCalc.add(branchCalc);
            batchTotalPaid = batchTotalPaid.add(branchPaid);
            batchTotalAdj = batchTotalAdj.add(branchAdj);
        }

        // Sort branches alphabetically
        branchDtos.sort(Comparator.comparing(CarryForwardBatchDetailDto.BranchGroupDto::getBranchName));

        return CarryForwardBatchDetailDto.builder()
                .id(batch.getId())
                .year(batch.getYear())
                .status(batch.getStatus())
                .submittedBy(batch.getSubmittedBy())
                .approvedBy(batch.getApprovedBy())
                .createdAt(batch.getCreatedAt())
                .financeReferenceId(batch.getFinanceReferenceId())
                .financeStatus(batch.getFinanceStatus())
                .sentToFinanceAt(batch.getSentToFinanceAt())
                .auditedBy(batch.getAuditedBy())
                .auditedAt(batch.getAuditedAt())
                .totalEmployees(entries.size())
                .totalCarriedDays(batchTotalDays)
                .totalCalculatedAmount(batchTotalCalc)
                .totalPaidAmount(batchTotalPaid)
                .totalAdjustmentAmount(batchTotalAdj)
                .branches(branchDtos)
                .build();
    }

    @Override
    @Transactional
    public CarryForwardBatchDetailDto uploadBatch(MultipartFile file, Integer year, String submittedBy) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded CSV file cannot be null or empty.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.toLowerCase().endsWith(".csv") && !originalFilename.toLowerCase().endsWith(".txt"))) {
            throw new IllegalArgumentException("Invalid file format. Only CSV (.csv) and Text (.txt) files are supported.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum allowed limit of 5MB.");
        }

        if (year == null || year < 2000 || year > 2100) {
            throw new IllegalArgumentException("Invalid year: " + year + ". Year must be between 2000 and 2100.");
        }

        String submitter = (submittedBy != null && !submittedBy.isBlank()) ? submittedBy.trim() : "HR Admin";
        String batchId = "CF-" + year + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        CarryForwardBatch batch = CarryForwardBatch.builder()
                .id(batchId)
                .year(year)
                .status("DRAFT")
                .submittedBy(submitter)
                .build();

        batch = batchRepository.save(batch);

        List<String> parseErrors = new ArrayList<>();
        int validRows = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null) {
                lineNum++;
                if (lineNum == 1 && (line.toLowerCase().contains("employee") || line.toLowerCase().contains("code") || line.toLowerCase().contains("days"))) {
                    continue; // Skip header line
                }
                if (line.trim().isEmpty()) continue;

                String[] values = line.split(",");
                if (values.length < 2) {
                    parseErrors.add("Line " + lineNum + ": Expected at least 2 comma-separated columns (EmployeeCode, CarryForwardDays).");
                    continue;
                }

                String empCode = values[0].trim();
                if (empCode.isEmpty()) {
                    parseErrors.add("Line " + lineNum + ": Employee code cannot be empty.");
                    continue;
                }

                int days;
                try {
                    days = Integer.parseInt(values[1].trim());
                    if (days <= 0 || days > 45) {
                        parseErrors.add("Line " + lineNum + " (" + empCode + "): Carried days must be between 1 and 45 (got " + days + ").");
                        continue;
                    }
                } catch (NumberFormatException nfe) {
                    parseErrors.add("Line " + lineNum + " (" + empCode + "): Invalid number format for carried days.");
                    continue;
                }

                BigDecimal rate = DEFAULT_DAILY_RATE;
                if (values.length >= 3 && !values[2].trim().isEmpty()) {
                    try {
                        BigDecimal parsedRate = new BigDecimal(values[2].trim());
                        if (parsedRate.compareTo(BigDecimal.ZERO) > 0) {
                            rate = parsedRate;
                        }
                    } catch (Exception ignored) {}
                }
                String remarks = values.length >= 4 ? values[3].trim() : "Imported from branch report";

                Optional<Employee> empOpt = employeeRepository.findByEmployeeCode(empCode);
                if (empOpt.isEmpty()) {
                    parseErrors.add("Line " + lineNum + ": Employee code '" + empCode + "' not found in employee master records.");
                    continue;
                }

                Employee emp = empOpt.get();
                BigDecimal calcAmount = rate.multiply(BigDecimal.valueOf(days));

                CarryForwardEntry entry = CarryForwardEntry.builder()
                        .batch(batch)
                        .employee(emp)
                        .carriedForwardDays(days)
                        .dailyRate(rate)
                        .calculatedAmount(calcAmount)
                        .isBranchVerified(false)
                        .auditStatus("PENDING_AUDIT")
                        .payrollApplied(false)
                        .remarks(remarks)
                        .build();
                entryRepository.save(entry);
                validRows++;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing carry forward batch file: " + e.getMessage(), e);
        }

        if (validRows == 0) {
            batchRepository.delete(batch);
            String detail = parseErrors.isEmpty() ? "File contains no employee data rows." : String.join("; ", parseErrors);
            throw new IllegalArgumentException("No valid employee records could be imported from the CSV file. Details: " + detail);
        }

        return getBatchDetails(batchId, null);
    }

    @Override
    @Transactional
    public CarryForwardBatchDetailDto generateBatchFromLeaveBalance(
            Integer year,
            String submittedBy,
            boolean includeAnnual,
            boolean includeCasual,
            boolean includeMedical,
            Integer annualCap,
            Integer casualCap,
            Integer medicalCap) {
        
        // 1. Validation: Year
        if (year == null || year < 2000 || year > 2100) {
            throw new IllegalArgumentException("Invalid year: " + year + ". Year must be between 2000 and 2100.");
        }

        // 2. Validation: At least one leave category must be active
        if (!includeAnnual && !includeCasual && !includeMedical) {
            throw new IllegalArgumentException("At least one leave type (Annual, Casual, or Medical) must be selected to generate a carry forward batch.");
        }

        // 3. Validation: Max Caps
        if (includeAnnual && (annualCap != null && (annualCap < 1 || annualCap > 30))) {
            throw new IllegalArgumentException("Annual leave cap must be between 1 and 30 days.");
        }
        if (includeCasual && (casualCap != null && (casualCap < 1 || casualCap > 14))) {
            throw new IllegalArgumentException("Casual leave cap must be between 1 and 14 days.");
        }
        if (includeMedical && (medicalCap != null && (medicalCap < 1 || medicalCap > 14))) {
            throw new IllegalArgumentException("Medical leave cap must be between 1 and 14 days.");
        }

        int maxAnnual = annualCap != null && annualCap >= 0 ? annualCap : 7;
        int maxCasual = casualCap != null && casualCap >= 0 ? casualCap : 3;
        int maxMedical = medicalCap != null && medicalCap >= 0 ? medicalCap : 3;

        String submitter = (submittedBy != null && !submittedBy.isBlank()) ? submittedBy.trim() : "Head Office HR";
        String batchId = "CF-" + year + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        CarryForwardBatch batch = CarryForwardBatch.builder()
                .id(batchId)
                .year(year)
                .status("DRAFT")
                .submittedBy(submitter)
                .build();

        batch = batchRepository.save(batch);

        // Fetch employees and their leave balances
        List<Employee> allEmployees = employeeRepository.findAll();
        int totalCreatedEntries = 0;

        for (Employee emp : allEmployees) {
            Optional<LeaveBalance> lbOpt = leaveBalanceRepository.findByEmployeeIdAndLeaveYear(emp.getId(), year);

            int annualQuota = lbOpt.map(lb -> (lb.getAnnualLeaveQuota() != null && lb.getAnnualLeaveQuota() > 0) ? lb.getAnnualLeaveQuota() : 14).orElse(14);
            int annualUsed = lbOpt.map(lb -> lb.getAnnualLeaveUsed() != null ? lb.getAnnualLeaveUsed() : 0).orElse(0);
            int annualRem = Math.max(0, annualQuota - annualUsed);

            int casualQuota = lbOpt.map(lb -> (lb.getCasualLeaveQuota() != null && lb.getCasualLeaveQuota() > 0) ? lb.getCasualLeaveQuota() : 7).orElse(7);
            int casualUsed = lbOpt.map(lb -> lb.getCasualLeaveUsed() != null ? lb.getCasualLeaveUsed() : 0).orElse(0);
            int casualRem = Math.max(0, casualQuota - casualUsed);

            int medicalQuota = lbOpt.map(lb -> (lb.getMedicalLeaveQuota() != null && lb.getMedicalLeaveQuota() > 0) ? lb.getMedicalLeaveQuota() : 7).orElse(7);
            int medicalUsed = lbOpt.map(lb -> lb.getMedicalLeaveUsed() != null ? lb.getMedicalLeaveUsed() : 0).orElse(0);
            int medicalRem = Math.max(0, medicalQuota - medicalUsed);

            int cfAnnual = includeAnnual ? Math.min(annualRem, maxAnnual) : 0;
            int cfCasual = includeCasual ? Math.min(casualRem, maxCasual) : 0;
            int cfMedical = includeMedical ? Math.min(medicalRem, maxMedical) : 0;

            int totalDays = cfAnnual + cfCasual + cfMedical;

            if (totalDays > 0) {
                BigDecimal rate = DEFAULT_DAILY_RATE;
                BigDecimal calcAmount = rate.multiply(BigDecimal.valueOf(totalDays));

                List<String> breakdown = new ArrayList<>();
                if (cfAnnual > 0) breakdown.add("Annual: " + cfAnnual + "d");
                if (cfCasual > 0) breakdown.add("Casual: " + cfCasual + "d");
                if (cfMedical > 0) breakdown.add("Medical: " + cfMedical + "d");

                String remarks = "Auto-generated (" + String.join(", ", breakdown) + ")";

                CarryForwardEntry entry = CarryForwardEntry.builder()
                        .batch(batch)
                        .employee(emp)
                        .carriedForwardDays(totalDays)
                        .dailyRate(rate)
                        .calculatedAmount(calcAmount)
                        .isBranchVerified(false)
                        .auditStatus("PENDING_AUDIT")
                        .payrollApplied(false)
                        .remarks(remarks)
                        .build();
                entryRepository.save(entry);
                totalCreatedEntries++;
            }
        }

        if (totalCreatedEntries == 0) {
            batchRepository.delete(batch);
            throw new IllegalStateException("No employees with remaining eligible leave balances found to carry forward for year " + year + ".");
        }

        return getBatchDetails(batchId, null);
    }

    @Override
    @Transactional
    public void verifyBatchByBranch(String batchId, String branch, String verifiedBy) {
        if (batchId == null || batchId.isBlank()) {
            throw new IllegalArgumentException("Batch ID cannot be null or empty.");
        }

        CarryForwardBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found with ID: " + batchId));

        if ("HR_APPROVED".equals(batch.getStatus()) || "FINANCE_SYNCED".equals(batch.getStatus()) || "AUDITED".equals(batch.getStatus())) {
            throw new IllegalStateException("Cannot modify branch verification: Batch " + batchId + " is already in " + batch.getStatus() + " state.");
        }

        String targetBranch = (branch != null && !branch.isBlank()) ? branch.trim() : "Head Office";
        String verifier = (verifiedBy != null && !verifiedBy.isBlank()) ? verifiedBy.trim() : "Branch Officer";

        List<CarryForwardEntry> entries = entryRepository.findByBatchId(batchId);
        int matchedBranchEntries = 0;

        for (CarryForwardEntry e : entries) {
            String empBranch = (e.getEmployee() != null && e.getEmployee().getBranch() != null)
                    ? e.getEmployee().getBranch().trim() : "Head Office";

            if (empBranch.equalsIgnoreCase(targetBranch)) {
                e.setIsBranchVerified(true);
                e.setBranchVerifiedBy(verifier);
                e.setBranchVerifiedAt(LocalDateTime.now());
                entryRepository.save(e);
                matchedBranchEntries++;
            }
        }

        if (matchedBranchEntries == 0) {
            throw new IllegalArgumentException("No employee entries found for branch '" + targetBranch + "' in batch " + batchId + ".");
        }

        // Check if all entries are now verified
        boolean allBranchesVerified = entries.stream().allMatch(e -> Boolean.TRUE.equals(e.getIsBranchVerified()));
        if (allBranchesVerified) {
            batch.setStatus("BRANCH_VERIFIED");
            batchRepository.save(batch);
        }
    }

    @Override
    @Transactional
    public void approveBatch(String batchId, String approvedBy) {
        if (batchId == null || batchId.isBlank()) {
            throw new IllegalArgumentException("Batch ID cannot be null or empty.");
        }

        CarryForwardBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found with ID: " + batchId));

        if ("HR_APPROVED".equals(batch.getStatus()) || "FINANCE_SYNCED".equals(batch.getStatus()) || "AUDITED".equals(batch.getStatus())) {
            throw new IllegalStateException("Batch " + batchId + " is already approved or processed (Status: " + batch.getStatus() + ").");
        }

        List<CarryForwardEntry> entries = entryRepository.findByBatchId(batchId);
        if (entries.isEmpty()) {
            throw new IllegalStateException("Cannot approve batch " + batchId + " because it contains 0 employee entries.");
        }

        String approver = (approvedBy != null && !approvedBy.isBlank()) ? approvedBy.trim() : "Head Office HR";
        batch.setStatus("HR_APPROVED");
        batch.setApprovedBy(approver);
        batchRepository.save(batch);
    }

    @Override
    @Transactional
    public Map<String, Object> syncToFinanceApi(String batchId) {
        if (batchId == null || batchId.isBlank()) {
            throw new IllegalArgumentException("Batch ID cannot be null or empty.");
        }

        CarryForwardBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found with ID: " + batchId));

        if ("FINANCE_SYNCED".equals(batch.getStatus()) || "AUDITING".equals(batch.getStatus()) || "AUDITED".equals(batch.getStatus())) {
            throw new IllegalStateException("Batch " + batchId + " has already been dispatched to Finance (Reference: " + batch.getFinanceReferenceId() + ").");
        }

        List<CarryForwardEntry> entries = entryRepository.findByBatchId(batchId);
        if (entries.isEmpty()) {
            throw new IllegalStateException("Cannot transmit an empty carry forward batch to Finance API.");
        }

        String finRef = "FIN-CF-" + batch.getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        batch.setFinanceReferenceId(finRef);
        batch.setFinanceStatus("DISPATCHED");
        batch.setSentToFinanceAt(LocalDateTime.now());
        batch.setStatus("FINANCE_SYNCED");

        BigDecimal totalDisbursed = BigDecimal.ZERO;

        for (CarryForwardEntry e : entries) {
            if (e.getPaidAmount() == null) {
                e.setPaidAmount(e.getCalculatedAmount());
            }
            e.setAuditStatus("PENDING_AUDIT");
            totalDisbursed = totalDisbursed.add(e.getPaidAmount() != null ? e.getPaidAmount() : BigDecimal.ZERO);
            entryRepository.save(e);
        }

        batchRepository.save(batch);

        Map<String, Object> response = new HashMap<>();
        response.put("batchId", batchId);
        response.put("financeReferenceId", finRef);
        response.put("status", "FINANCE_SYNCED");
        response.put("totalEmployeesDisbursed", entries.size());
        response.put("totalAmountDisbursed", totalDisbursed);
        response.put("message", "Disbursement batch payload successfully transmitted to Finance API.");
        return response;
    }

    @Override
    @Transactional
    public CarryForwardEntryDto recordAuditAdjustment(Long entryId, Integer actualDays, BigDecimal actualAmount, String reason, String auditorName) {
        if (entryId == null) {
            throw new IllegalArgumentException("Entry ID cannot be null.");
        }

        CarryForwardEntry entry = entryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found with ID: " + entryId));

        CarryForwardBatch batch = entry.getBatch();
        if (batch != null && !"FINANCE_SYNCED".equals(batch.getStatus()) && !"AUDITING".equals(batch.getStatus()) && !"AUDITED".equals(batch.getStatus())) {
            throw new IllegalStateException("Audit adjustments can only be recorded on batches that have been disbursed by Finance.");
        }

        // Validate actual days
        if (actualDays == null || actualDays < 0 || actualDays > 45) {
            throw new IllegalArgumentException("Audited actual days must be a valid number between 0 and 45.");
        }

        // Validate actual amount
        if (actualAmount == null || actualAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Audited actual amount cannot be negative.");
        }

        // Validate reason
        if (reason == null || reason.trim().length() < 3) {
            throw new IllegalArgumentException("A clear audit adjustment justification (at least 3 characters) is required.");
        }

        String auditor = (auditorName != null && !auditorName.isBlank()) ? auditorName.trim() : "Head Office Auditor";

        entry.setActualDays(actualDays);
        entry.setActualAmount(actualAmount);
        entry.setAdjustmentReason(reason.trim());
        entry.setAuditedBy(auditor);
        entry.setAuditedAt(LocalDateTime.now());

        BigDecimal paid = entry.getPaidAmount() != null ? entry.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal adj = actualAmount.subtract(paid);
        entry.setAdjustmentAmount(adj);

        if (adj.compareTo(BigDecimal.ZERO) < 0) {
            entry.setAuditStatus("DISCREPANCY_OVERPAID");
        } else if (adj.compareTo(BigDecimal.ZERO) > 0) {
            entry.setAuditStatus("DISCREPANCY_UNDERPAID");
        } else {
            entry.setAuditStatus("MATCHED");
        }

        entry.setPayrollApplied(false);

        if (batch != null && "FINANCE_SYNCED".equals(batch.getStatus())) {
            batch.setStatus("AUDITING");
            batchRepository.save(batch);
        }

        entry = entryRepository.save(entry);

        String empBranch = (entry.getEmployee() != null && entry.getEmployee().getBranch() != null)
                ? entry.getEmployee().getBranch() : "Head Office";

        return CarryForwardEntryDto.builder()
                .id(entry.getId())
                .empId(entry.getEmployee() != null ? entry.getEmployee().getEmployeeCode() : "N/A")
                .name(entry.getEmployee() != null ? entry.getEmployee().getFullName() : "Unknown")
                .department(entry.getEmployee() != null ? entry.getEmployee().getDepartment() : "General")
                .branch(empBranch)
                .carriedForwardDays(entry.getCarriedForwardDays())
                .dailyRate(entry.getDailyRate())
                .calculatedAmount(entry.getCalculatedAmount())
                .paidAmount(entry.getPaidAmount())
                .actualDays(entry.getActualDays())
                .actualAmount(entry.getActualAmount())
                .adjustmentAmount(entry.getAdjustmentAmount())
                .auditStatus(entry.getAuditStatus())
                .adjustmentReason(entry.getAdjustmentReason())
                .payrollApplied(entry.getPayrollApplied())
                .auditedBy(entry.getAuditedBy())
                .auditedAt(entry.getAuditedAt())
                .build();
    }

    @Override
    @Transactional
    public void completeBatchAudit(String batchId, String auditorName) {
        if (batchId == null || batchId.isBlank()) {
            throw new IllegalArgumentException("Batch ID cannot be null or empty.");
        }

        CarryForwardBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found with ID: " + batchId));

        if (!"FINANCE_SYNCED".equals(batch.getStatus()) && !"AUDITING".equals(batch.getStatus())) {
            throw new IllegalStateException("Cannot finalize audit on batch with status: " + batch.getStatus() + ". Batch must be in FINANCE_SYNCED or AUDITING state.");
        }

        String auditor = (auditorName != null && !auditorName.isBlank()) ? auditorName.trim() : "Head Office Auditor";
        batch.setStatus("AUDITED");
        batch.setAuditedBy(auditor);
        batch.setAuditedAt(LocalDateTime.now());
        batchRepository.save(batch);
    }

    @Override
    public Map<String, Object> getAuditSummary(String batchId) {
        if (batchId == null || batchId.isBlank()) {
            throw new IllegalArgumentException("Batch ID cannot be null or empty.");
        }

        CarryForwardBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found with ID: " + batchId));

        List<CarryForwardEntry> entries = entryRepository.findByBatchId(batchId);

        long matchedCount = entries.stream().filter(e -> "MATCHED".equals(e.getAuditStatus())).count();
        long overpaidCount = entries.stream().filter(e -> "DISCREPANCY_OVERPAID".equals(e.getAuditStatus())).count();
        long underpaidCount = entries.stream().filter(e -> "DISCREPANCY_UNDERPAID".equals(e.getAuditStatus())).count();
        long pendingCount = entries.stream().filter(e -> e.getAuditStatus() == null || "PENDING_AUDIT".equals(e.getAuditStatus())).count();

        BigDecimal totalOverpaidAmount = entries.stream()
                .filter(e -> "DISCREPANCY_OVERPAID".equals(e.getAuditStatus()) && e.getAdjustmentAmount() != null)
                .map(e -> e.getAdjustmentAmount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUnderpaidAmount = entries.stream()
                .filter(e -> "DISCREPANCY_UNDERPAID".equals(e.getAuditStatus()) && e.getAdjustmentAmount() != null)
                .map(CarryForwardEntry::getAdjustmentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> summary = new HashMap<>();
        summary.put("batchId", batchId);
        summary.put("year", batch.getYear());
        summary.put("batchStatus", batch.getStatus());
        summary.put("totalEntries", entries.size());
        summary.put("matchedCount", matchedCount);
        summary.put("overpaidCount", overpaidCount);
        summary.put("underpaidCount", underpaidCount);
        summary.put("pendingCount", pendingCount);
        summary.put("totalOverpaidDeduction", totalOverpaidAmount);
        summary.put("totalUnderpaidReimbursement", totalUnderpaidAmount);
        return summary;
    }
}
