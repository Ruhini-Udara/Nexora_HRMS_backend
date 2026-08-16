package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.ShiftDto;
import com.hexaco.hrms.models.Shift;

import java.util.List;

public interface ShiftService {
    List<Shift> getAllShifts();
    Shift getShiftById(Long id);
    Shift createShift(ShiftDto dto);
    Shift updateShift(Long id, ShiftDto dto);
    void deleteShift(Long id);
}
