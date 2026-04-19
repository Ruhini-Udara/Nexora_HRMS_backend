package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.models.MaternityLeave;
import com.hexaco.hrms.models.OverseasLeave;
import com.hexaco.hrms.repository.MaternityLeaveRepository;
import com.hexaco.hrms.repository.OverseasLeaveRepository;
import com.hexaco.hrms.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final OverseasLeaveRepository overseasLeaveRepository;
    private final MaternityLeaveRepository maternityLeaveRepository;

    @Override
    public OverseasLeave submitOverseasLeave(OverseasLeave requestedLeave) {
        // Set default status to PENDING for new applications
        requestedLeave.setStatus("PENDING");
        return overseasLeaveRepository.save(requestedLeave);
    }

    @Override
    public Optional<OverseasLeave> getOverseasLeaveById(Long id) {
        return overseasLeaveRepository.findById(id);
    }

    @Override
    public List<OverseasLeave> getAllOverseasLeaves() {
        return overseasLeaveRepository.findAll();
    }

    @Override
    public MaternityLeave submitMaternityLeave(MaternityLeave requestedLeave) {
        // Set default status to PENDING for new applications
        requestedLeave.setStatus("PENDING");
        return maternityLeaveRepository.save(requestedLeave);
    }

    @Override
    public Optional<MaternityLeave> getMaternityLeaveById(Long id) {
        return maternityLeaveRepository.findById(id);
    }

    @Override
    public List<MaternityLeave> getAllMaternityLeaves() {
        return maternityLeaveRepository.findAll();
    }
}
