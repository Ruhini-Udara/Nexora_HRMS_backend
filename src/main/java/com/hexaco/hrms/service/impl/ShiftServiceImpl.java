package com.hexaco.hrms.service.impl;

import com.hexaco.hrms.dto.ShiftDto;
import com.hexaco.hrms.models.Shift;
import com.hexaco.hrms.repository.DesignationRepository;
import com.hexaco.hrms.repository.ShiftRepository;
import com.hexaco.hrms.service.ShiftService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final DesignationRepository designationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Shift getShiftById(Long id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shift not found with id: " + id));
    }

    @Override
    @Transactional
    public Shift createShift(ShiftDto dto) {
        if (shiftRepository.findByName(dto.getName()).isPresent()) {
            throw new RuntimeException("Shift with name '" + dto.getName() + "' already exists");
        }
        Shift shift = Shift.builder()
                .name(dto.getName())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .description(dto.getDescription())
                .build();
        return shiftRepository.save(shift);
    }

    @Override
    @Transactional
    public Shift updateShift(Long id, ShiftDto dto) {
        Shift existing = getShiftById(id);

        shiftRepository.findByName(dto.getName()).ifPresent(s -> {
            if (!s.getId().equals(id)) {
                throw new RuntimeException("Another shift with name '" + dto.getName() + "' already exists");
            }
        });

        existing.setName(dto.getName());
        existing.setStartTime(dto.getStartTime());
        existing.setEndTime(dto.getEndTime());
        existing.setDescription(dto.getDescription());

        return shiftRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteShift(Long id) {
        Shift existing = getShiftById(id);
        if (designationRepository.existsByShift_Id(id)) {
            throw new RuntimeException("Cannot delete shift: it is currently mapped to one or more designations");
        }
        shiftRepository.delete(existing);
    }
}
