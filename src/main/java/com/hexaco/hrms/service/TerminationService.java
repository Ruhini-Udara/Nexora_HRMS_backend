package com.hexaco.hrms.service;

import com.hexaco.hrms.dto.TerminationDto;
import java.util.List;

public interface TerminationService {
    TerminationDto createTermination(TerminationDto dto);
    List<TerminationDto> getAllTerminations();
    List<TerminationDto> getTerminationsByEmployeeId(Long employeeId);
    TerminationDto getTerminationById(Long id);
    TerminationDto updateTerminationStatus(Long id, String status, String remarks, String boardMeetingDate);
    TerminationDto updateTermination(Long id, TerminationDto dto);
}
