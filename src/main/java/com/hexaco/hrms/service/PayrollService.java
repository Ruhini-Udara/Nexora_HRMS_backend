package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.PayrollLeaveDto;
import com.hexaco.hrms.models.LeaveRequest;
import com.hexaco.hrms.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final LeaveRequestRepository leaveRequestRepository;

    public List<PayrollLeaveDto> getApprovedLeavesForPayroll(int month, int year) {
        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findApprovedLeavesByMonthAndYear(month, year);
        
        return approvedLeaves.stream().map(leave -> PayrollLeaveDto.builder()
                .leaveId(leave.getId())
                .employeeId(leave.getEmployee() != null ? leave.getEmployee().getId() : null)
                .employeeName(leave.getEmployee() != null ? leave.getEmployee().getFullName() : "Unknown")
                .epfNumber(leave.getEmployee() != null ? leave.getEmployee().getEpfNumber() : "Unknown")
                .leaveType(leave.getClass().getSimpleName())
                .fromDate(leave.getFromDate())
                .endDate(leave.getEndDate())
                .totalDays(leave.getTotalDays())
                .status(leave.getStatus())
                .build()).collect(Collectors.toList());
    }
}
